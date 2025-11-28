package br.com.gerenciador.sistema_gerenciamento_tarefas.controller;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.User;
import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.UserRole;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.user.UserResponseDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.user.UserUpdateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.*;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security.SecurityConfigurations;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security.SecurityFilter;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security.TokenService;
import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.UserRepository;
import br.com.gerenciador.sistema_gerenciamento_tarefas.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfigurations.class, SecurityFilter.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class, UserControllerTest.TestSecurityConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDetailsService userDetailsService;

    private User testUser;
    private User adminUser;

    static class TestSecurityConfig {
        @Bean
        @Primary
        public UserDetailsService userDetailsService() {
            User user = new User(UUID.fromString("a0b1c2d3-e4f5-6789-0123-456789abcdef"), "currentuser", "password", UserRole.USER);
            User admin = new User(UUID.fromString("fedcba98-7654-3210-fedc-ba9876543210"), "admin", "password", UserRole.ADMIN);
            UserDetailsService mock = mock(UserDetailsService.class);
            when(mock.loadUserByUsername("currentuser")).thenReturn(user);
            when(mock.loadUserByUsername("admin")).thenReturn(admin);
            return mock;
        }
    }

    @BeforeEach
    void setup() {
        testUser = new User(UUID.fromString("a0b1c2d3-e4f5-6789-0123-456789abcdef"), "currentuser", "password", UserRole.USER);
        adminUser = new User(UUID.fromString("fedcba98-7654-3210-fedc-ba9876543210"), "admin", "password", UserRole.ADMIN);
    }

    // --- Testes para getAllUsers ---

    @Test
    @DisplayName("getAllUsers should return 200 OK and a page of users for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturnOkAndPageOfUsers_forAdmin() throws Exception {
        // Arrange
        var userResponse = new UserResponseDTO(UUID.randomUUID(), "testuser", UserRole.USER);
        Page<UserResponseDTO> userPage = new PageImpl<>(List.of(userResponse));
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        // Act & Assert
        mockMvc.perform(get("/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].login", is("testuser")));
    }

    @Test
    @DisplayName("getAllUsers should return 403 Forbidden for USER")
    @WithMockUser(roles = "USER")
    void getAllUsers_shouldReturnForbidden_forUser() throws Exception {
        // Arrange, Act & Assert
        mockMvc.perform(get("/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", is("Access denied. You do not have permission to access this resource.")))
                .andExpect(jsonPath("$.path", is("/users")))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    // --- Testes para getUserById ---

    @Test
    @DisplayName("getUserById should return 200 OK and user data for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void getUserById_shouldReturnOkAndUserData_forAdmin() throws Exception {
        // Arrange
        var userId = UUID.randomUUID();
        var userResponse = new UserResponseDTO(userId, "testuser", UserRole.USER);
        when(userService.getUserById(userId)).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(get("/users/{id}", userId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.toString())))
                .andExpect(jsonPath("$.login", is("testuser")));
    }

    @Test
    @DisplayName("getUserById should return 404 Not Found when user does not exist for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void getUserById_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        // Arrange
        var userId = UUID.randomUUID();
        String errorMessage = "User not found";
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException(errorMessage));

        // Act & Assert
        mockMvc.perform(get("/users/{id}", userId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.path", is("/users/" + userId)))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("getUserById should return 403 Forbidden for USER")
    @WithMockUser(roles = "USER")
    void getUserById_shouldReturnForbidden_forUser() throws Exception {
        // Arrange
        var userId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(get("/users/{id}", userId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", is("Access denied. You do not have permission to access this resource.")))
                .andExpect(jsonPath("$.path", is("/users/" + userId)))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    // --- Testes para getCurrentUser ---

    @Test
    @DisplayName("getCurrentUser should return 200 OK and current user data for authenticated user")
    @WithUserDetails("currentuser")
    void getCurrentUser_shouldReturnOkAndCurrentUserData() throws Exception {
        // Arrange
        var userResponse = new UserResponseDTO(testUser);
        when(userService.getCurrentUser(any(User.class))).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(get("/users/me").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId().toString())))
                .andExpect(jsonPath("$.login", is("currentuser")));
    }

    @Test
    @DisplayName("getCurrentUser should return 401 Unauthorized for unauthenticated user")
    void getCurrentUser_shouldReturnUnauthorized_forUnauthenticated() throws Exception {
        // Arrange, Act & Assert
        mockMvc.perform(get("/users/me").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("Authentication required. Please provide a valid token.")))
                .andExpect(jsonPath("$.path", is("/users/me")))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    // --- Testes para updateUser ---

    @Test
    @DisplayName("updateUser should return 200 OK when user updates their own profile")
    @WithUserDetails("currentuser")
    void updateUser_shouldReturnOk_whenUserUpdatesSelf() throws Exception {
        // Arrange
        var updateDTO = new UserUpdateDTO("newLogin", "ValidPass123!");
        var responseDTO = new UserResponseDTO(testUser.getId(), "newLogin", UserRole.USER);

        when(userService.updateUser(eq(testUser.getId()), any(UserUpdateDTO.class), any(User.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(patch("/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login", is("newLogin")));
    }

    @Test
    @DisplayName("updateUser should return 200 OK when ADMIN updates another user's profile")
    @WithUserDetails("admin")
    void updateUser_shouldReturnOk_whenAdminUpdatesAnotherUser() throws Exception {
        // Arrange
        UUID userIdToUpdate = UUID.randomUUID();
        var updateDTO = new UserUpdateDTO("newLoginForUser", null);
        var responseDTO = new UserResponseDTO(userIdToUpdate, "newLoginForUser", UserRole.USER);

        when(userService.updateUser(eq(userIdToUpdate), any(UserUpdateDTO.class), any(User.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(patch("/users/{id}", userIdToUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login", is("newLoginForUser")));
    }

    @Test
    @DisplayName("updateUser should return 403 Forbidden when user tries to update another user's profile")
    @WithUserDetails("currentuser")
    void updateUser_shouldReturnForbidden_whenUserUpdatesAnother() throws Exception {
        // Arrange
        UUID anotherUserId = UUID.randomUUID();
        var updateDTO = new UserUpdateDTO("anyLogin", null);
        String errorMessage = "You are not authorized to update this user's profile.";

        when(userService.updateUser(eq(anotherUserId), any(UserUpdateDTO.class), any(User.class)))
                .thenThrow(new SecurityException(errorMessage));

        // Act & Assert
        mockMvc.perform(patch("/users/{id}", anotherUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is(errorMessage)));
    }

    @Test
    @DisplayName("updateUser should return 409 Conflict when new login already exists")
    @WithUserDetails("currentuser")
    void updateUser_shouldReturnConflict_whenLoginAlreadyExists() throws Exception {
        // Arrange
        var updateDTO = new UserUpdateDTO("existingLogin", null);
        String errorMessage = "Login 'existingLogin' is already in use.";

        when(userService.updateUser(eq(testUser.getId()), any(UserUpdateDTO.class), any(User.class)))
                .thenThrow(new UserAlreadyExistsException(errorMessage));

        // Act & Assert
        mockMvc.perform(patch("/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is(errorMessage)));
    }

    @Test
    @DisplayName("updateUser should return 404 Not Found when user does not exist")
    @WithUserDetails("admin")
    void updateUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        var updateDTO = new UserUpdateDTO("anyLogin", null);
        String errorMessage = "User not found with ID: " + nonExistentId;

        when(userService.updateUser(eq(nonExistentId), any(UserUpdateDTO.class), any(User.class)))
                .thenThrow(new UserNotFoundException(errorMessage));

        // Act & Assert
        mockMvc.perform(patch("/users/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(errorMessage)));
    }

    @Test
    @DisplayName("updateUser should return 400 Bad Request when login is invalid")
    @WithUserDetails("currentuser")
    void updateUser_shouldReturnBadRequest_whenLoginIsInvalid() throws Exception {
        // Arrange
        var updateDTO = new UserUpdateDTO("a", null); // Login too short

        // Act & Assert
        mockMvc.perform(patch("/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.details[0].field", is("login")))
                .andExpect(jsonPath("$.details[0].message", is("O login deve ter no mínimo 3 caracteres")));
    }

    // --- Testes para updateUserRole ---

    @Test
    @DisplayName("updateUserRole should return 200 OK and updated user for ADMIN")
    @WithUserDetails("admin")
    void updateUserRole_shouldReturnOkAndUpdatedUser_forAdmin() throws Exception {
        // Arrange
        var userId = UUID.randomUUID();
        var updatedUserResponse = new UserResponseDTO(userId, "testuser", UserRole.ADMIN);
        when(userService.updateUserRole(eq(userId), eq(UserRole.ADMIN), any(User.class))).thenReturn(updatedUserResponse);

        // Act & Assert
        mockMvc.perform(patch("/users/{id}/role", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.toString())))
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    @DisplayName("updateUserRole should return 403 Forbidden for USER")
    @WithMockUser(roles = "USER")
    void updateUserRole_shouldReturnForbidden_forUser() throws Exception {
        // Arrange
        var userId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(patch("/users/{id}/role", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UserRole.ADMIN)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", is("Access denied. You do not have permission to access this resource.")))
                .andExpect(jsonPath("$.path", is("/users/" + userId + "/role")))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("updateUserRole should return 404 Not Found when user does not exist")
    @WithUserDetails("admin")
    void updateUserRole_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        // Arrange
        var nonExistentUserId = UUID.randomUUID();
        String errorMessage = "User not found";
        when(userService.updateUserRole(eq(nonExistentUserId), any(UserRole.class), any(User.class)))
                .thenThrow(new UserNotFoundException(errorMessage));

        // Act & Assert
        mockMvc.perform(patch("/users/{id}/role", nonExistentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UserRole.ADMIN)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.path", is("/users/" + nonExistentUserId + "/role")))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("updateUserRole should return 403 Forbidden when ADMIN tries to demote the last ADMIN")
    @WithUserDetails("admin")
    void updateUserRole_shouldReturnForbidden_whenDemotingLastAdmin() throws Exception {
        // Arrange
        var userId = UUID.randomUUID();
        String errorMessage = "Cannot demote the last ADMIN user.";
        when(userService.updateUserRole(eq(userId), eq(UserRole.USER), any(User.class)))
                .thenThrow(new LastAdminDemotionNotAllowedException(errorMessage));

        // Act & Assert
        mockMvc.perform(patch("/users/{id}/role", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UserRole.USER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.path", is("/users/" + userId + "/role")))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("updateUserRole should return 403 Forbidden when ADMIN tries to demote self")
    @WithUserDetails("admin")
    void updateUserRole_shouldReturnForbidden_whenAdminDemotesSelf() throws Exception {
        // Arrange
        var adminId = adminUser.getId();
        String errorMessage = "An ADMIN user cannot change their own role.";
        when(userService.updateUserRole(eq(adminId), eq(UserRole.USER), any(User.class)))
                .thenThrow(new SelfRoleChangeNotAllowedException(errorMessage));

        // Act & Assert
        mockMvc.perform(patch("/users/{id}/role", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UserRole.USER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.path", is("/users/" + adminId + "/role")))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    // --- Testes para deleteUser ---

    @Test
    @DisplayName("deleteUser should return 204 No Content for ADMIN")
    @WithUserDetails("admin")
    void deleteUser_shouldReturnNoContent_forAdmin() throws Exception {
        // Arrange
        var userIdToDelete = UUID.randomUUID();
        doNothing().when(userService).deleteUser(eq(userIdToDelete), any(User.class));

        // Act & Assert
        mockMvc.perform(delete("/users/{id}", userIdToDelete))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deleteUser should return 403 Forbidden for USER")
    @WithMockUser(roles = "USER")
    void deleteUser_shouldReturnForbidden_forUser() throws Exception {
        // Arrange
        var userIdToDelete = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(delete("/users/{id}", userIdToDelete))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", is("Access denied. You do not have permission to access this resource.")))
                .andExpect(jsonPath("$.path", is("/users/" + userIdToDelete)))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("deleteUser should return 404 Not Found when user does not exist")
    @WithUserDetails("admin")
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        // Arrange
        var nonExistentUserId = UUID.randomUUID();
        String errorMessage = "User not found";
        doThrow(new UserNotFoundException(errorMessage)).when(userService).deleteUser(eq(nonExistentUserId), any(User.class));

        // Act & Assert
        mockMvc.perform(delete("/users/{id}", nonExistentUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.path", is("/users/" + nonExistentUserId)))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("deleteUser should return 403 Forbidden when ADMIN tries to delete self")
    @WithUserDetails("admin")
    void deleteUser_shouldReturnForbidden_whenAdminDeletesSelf() throws Exception {
        // Arrange
        var adminId = adminUser.getId();
        String errorMessage = "A user cannot delete themselves.";
        doThrow(new SelfDeletionNotAllowedException(errorMessage))
                .when(userService).deleteUser(eq(adminId), any(User.class));

        // Act & Assert
        mockMvc.perform(delete("/users/{id}", adminId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.path", is("/users/" + adminId)))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("deleteUser should return 403 Forbidden when deleting the last ADMIN")
    @WithUserDetails("admin")
    void deleteUser_shouldReturnForbidden_whenDeletingLastAdmin() throws Exception {
        // Arrange
        var lastAdminId = UUID.randomUUID();
        String errorMessage = "Cannot delete the last ADMIN user.";
        doThrow(new LastAdminDeletionNotAllowedException(errorMessage))
                .when(userService).deleteUser(eq(lastAdminId), any(User.class));

        // Act & Assert
        mockMvc.perform(delete("/users/{id}", lastAdminId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.path", is("/users/" + lastAdminId)))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    // --- Testes de Casos de Borda ---

    @Test
    @DisplayName("Should return 405 Method Not Allowed when using wrong HTTP method")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnMethodNotAllowed_whenWrongHttpMethod() throws Exception {
        // Arrange & Act & Assert
        String requestPath = "/users/" + UUID.randomUUID();
        String errorMessage = "Request method 'POST' is not supported";
        mockMvc.perform(post(requestPath))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(405)))
                .andExpect(jsonPath("$.error", is("Method Not Allowed")))
                .andExpect(jsonPath("$.message", containsString(errorMessage)))
                .andExpect(jsonPath("$.path", is(requestPath)))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("Should return 415 Unsupported Media Type when content type is not JSON")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnUnsupportedMediaType_whenWrongContentType() throws Exception {
        // Arrange & Act & Assert
        String requestPath = "/users/" + UUID.randomUUID() + "/role";
        String errorMessage = "Content-Type 'text/plain;charset=UTF-8' is not supported";
        mockMvc.perform(patch(requestPath)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("ADMIN"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(415)))
                .andExpect(jsonPath("$.error", is("Unsupported Media Type")))
                .andExpect(jsonPath("$.message", containsString(errorMessage)))
                .andExpect(jsonPath("$.path", is(requestPath)))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when JSON body is malformed")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequest_whenJsonIsMalformed() throws Exception {
        // Arrange
        String requestPath = "/users/" + UUID.randomUUID() + "/role";
        String malformedJson = "\"ADMIN"; // JSON inválido
        String errorMessage = "Required request body is missing or malformed";

        // Act & Assert
        mockMvc.perform(patch(requestPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString(errorMessage)))
                .andExpect(jsonPath("$.path", is(requestPath)))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when an unexpected error occurs")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnInternalServerError_whenUnexpectedException() throws Exception {
        // Arrange
        String requestPath = "/users";
        when(userService.getAllUsers(any(Pageable.class))).thenThrow(new RuntimeException("Unexpected error"));
        String errorMessage = "An internal server error occurred.";

        // Act & Assert
        mockMvc.perform(get(requestPath)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.path", is(requestPath)))
                .andExpect(jsonPath("$.details").doesNotExist());
    }
}
