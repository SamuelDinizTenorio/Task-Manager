package br.com.gerenciador.sistema_gerenciamento_tarefas.controller;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.User;
import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.UserRole;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.authentication.AuthenticationDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.authentication.RegisterDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.CustomAccessDeniedHandler;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.CustomAuthenticationEntryPoint;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.UserAlreadyExistsException;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security.AuthorizationService;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security.SecurityConfigurations;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security.SecurityFilter;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security.TokenService;
import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.UserRepository;
import br.com.gerenciador.sistema_gerenciamento_tarefas.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import({SecurityConfigurations.class, SecurityFilter.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private TokenService tokenService;
    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return 200 OK and a JWT token when login is successful")
    void login_shouldReturnOkAndToken_whenCredentialsAreValid() throws Exception {
        // Arrange
        var loginDTO = new AuthenticationDTO("user", "password123");
        String fakeToken = "fake.jwt.token";
        when(authenticationService.login(any(AuthenticationDTO.class))).thenReturn(fakeToken);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(fakeToken)));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized and error body when login fails with bad credentials")
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        // Arrange
        var loginDTO = new AuthenticationDTO("user", "wrongpassword");
        String errorMessage = "Invalid credentials";
        when(authenticationService.login(any(AuthenticationDTO.class)))
                .thenThrow(new BadCredentialsException(errorMessage));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.path", is("/auth/login")))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when login data is invalid (e.g., blank login)")
    void login_shouldReturnBadRequest_whenLoginIsInvalid() throws Exception {
        // Arrange
        var invalidLoginDTO = new AuthenticationDTO("", "password123");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.details[0].field", is("login")))
                .andExpect(jsonPath("$.details[0].message", is("O login não pode estar em branco.")));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when login password is blank")
    void login_shouldReturnBadRequest_whenPasswordIsBlank() throws Exception {
        // Arrange
        var invalidLoginDTO = new AuthenticationDTO("user", "");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.details[0].field", is("password")))
                .andExpect(jsonPath("$.details[0].message", is("A senha não pode estar em branco.")));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when using GET on a non-permitAll endpoint")
    void getRequest_shouldReturnUnauthorized_whenEndpointIsNotPermitted() throws Exception {
        // Arrange (No setup needed)

        // Act & Assert
        mockMvc.perform(get("/auth/login")) // Usando GET em vez de POST
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("Authentication required. Please provide a valid token.")))
                .andExpect(jsonPath("$.path", is("/auth/login")))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("Should return 405 Method Not Allowed when authenticated user uses GET on login endpoint")
    @WithMockUser // Simula um usuário autenticado (qualquer role)
    void login_shouldReturnMethodNotAllowed_whenAuthenticatedUserUsesGet() throws Exception {
        // Arrange (No setup needed)

        // Act & Assert
        mockMvc.perform(get("/auth/login")) // Usando GET em vez de POST
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(405)))
                .andExpect(jsonPath("$.error", is("Method Not Allowed")))
                .andExpect(jsonPath("$.message", containsString("Request method 'GET' is not supported")))
                .andExpect(jsonPath("$.path", is("/auth/login")))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("Should return 415 Unsupported Media Type when content type is not JSON")
    void login_shouldReturnUnsupportedMediaType_whenContentTypeIsWrong() throws Exception {
        // Arrange
        var loginDTO = new AuthenticationDTO("user", "password123");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.TEXT_PLAIN) // Content-Type errado
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(415)))
                .andExpect(jsonPath("$.error", is("Unsupported Media Type")))
                .andExpect(jsonPath("$.message", containsString("Content-Type 'text/plain;charset=UTF-8' is not supported")))
                .andExpect(jsonPath("$.path", is("/auth/login")))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when JSON body is malformed")
    void login_shouldReturnBadRequest_whenJsonIsMalformed() throws Exception {
        // Arrange
        String malformedJson = "{\"login\":\"user\", \"password\":\"pass\""; // JSON inválido (falta '}')

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Required request body is missing or malformed")))
                .andExpect(jsonPath("$.path", is("/auth/login")))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when an unexpected error occurs")
    void login_shouldReturnInternalServerError_whenUnexpectedExceptionIsThrown() throws Exception {
        // Arrange
        var loginDTO = new AuthenticationDTO("user", "password123");
        when(authenticationService.login(any(AuthenticationDTO.class)))
                .thenThrow(new RuntimeException("A generic, unexpected error occurred!"));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("An internal server error occurred.")))
                .andExpect(jsonPath("$.path", is("/auth/login")))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("Should return 201 Created when registration is successful")
    void register_shouldReturnCreated_whenDataIsValid() throws Exception {
        // Arrange
        var registerDTO = new RegisterDTO("newUser", "ValidPassword123!");
        var savedUser = new User(UUID.randomUUID(), registerDTO.login(), "encryptedPassword", UserRole.USER);
        when(authenticationService.register(any(RegisterDTO.class))).thenReturn(savedUser);

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("Should return 409 Conflict when user already exists")
    void register_shouldReturnConflict_whenUserAlreadyExists() throws Exception {
        // Arrange
        var registerDTO = new RegisterDTO("existingUser", "ValidPassword123!");
        when(authenticationService.register(any(RegisterDTO.class)))
                .thenThrow(new UserAlreadyExistsException("User with this login already exists."));

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", is("User with this login already exists.")))
                .andExpect(jsonPath("$.path", is("/auth/register")))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @DisplayName("Should return 400 Bad Request for invalid registration data")
    @ParameterizedTest
    @MethodSource("invalidRegistrationProvider")
    void register_shouldReturnBadRequest_forInvalidData(RegisterDTO invalidDTO, String expectedField, String expectedMessage) throws Exception {
        // Arrange (Provided by MethodSource)

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.details[0].field", is(expectedField)))
                .andExpect(jsonPath("$.details[0].message", containsString(expectedMessage)));
    }

    private static Stream<Arguments> invalidRegistrationProvider() {
        return Stream.of(
                Arguments.of(new RegisterDTO("newuser", "weak"), "password", "A senha deve ter no mínimo 8 caracteres"),
                Arguments.of(new RegisterDTO("a", "ValidPassword123!"), "login", "O login deve ter no mínimo 3 caracteres")
        );
    }

    @Test
    @DisplayName("Should return 400 Bad Request with multiple validation errors when multiple fields are invalid")
    void register_shouldReturnBadRequest_whenMultipleFieldsAreInvalid() throws Exception {
        // Arrange
        var invalidRegisterDTO = new RegisterDTO("a", "weak"); // Invalid login and password

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRegisterDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.details", hasSize(2)));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when registration login is blank")
    void register_shouldReturnBadRequest_whenLoginIsBlank() throws Exception {
        // Arrange
        var invalidRegisterDTO = new RegisterDTO("", "ValidPassword123!");

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRegisterDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.details", hasSize(2)))
                .andExpect(jsonPath("$.details[*].field", containsInAnyOrder("login", "login")))
                .andExpect(jsonPath("$.details[*].message", containsInAnyOrder(
                        "must not be blank",
                        "O login deve ter no mínimo 3 caracteres"
                )));
    }
}
