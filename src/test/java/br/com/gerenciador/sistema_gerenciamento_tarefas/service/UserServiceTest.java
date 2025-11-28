package br.com.gerenciador.sistema_gerenciamento_tarefas.service;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.User;
import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.UserRole;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.user.UserResponseDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.user.UserUpdateDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.*;
import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe {@link UserService}.
 * Foco: Testar a lógica de negócio de gerenciamento de usuários em isolamento.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // --- Testes para o método getAllUsers ---

    @Test
    @DisplayName("getAllUsers should return a page of users")
    void getAllUsers_shouldReturnPageOfUsers() {
        // Arrange
        var user = new User(UUID.randomUUID(), "testuser", "password", UserRole.USER);
        Page<User> userPage = new PageImpl<>(List.of(user));
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // Act
        Page<UserResponseDTO> result = userService.getAllUsers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).login());
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("getAllUsers should return an empty page when no users exist")
    void getAllUsers_shouldReturnEmptyPage_whenNoUsersExist() {
        // Arrange
        Page<User> emptyPage = Page.empty();
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<UserResponseDTO> result = userService.getAllUsers(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("getAllUsers should propagate exceptions from the repository")
    void getAllUsers_shouldPropagateRepositoryExceptions() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String errorMessage = "Database connection failed";
        when(userRepository.findAll(pageable)).thenThrow(new RuntimeException(errorMessage));

        // Act & Assert
        var exception = assertThrows(RuntimeException.class, () -> {
            userService.getAllUsers(pageable);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("getAllUsers should throw IllegalArgumentException when pageable is null")
    void getAllUsers_shouldThrowIllegalArgumentException_whenPageableIsNull() {
        // Arrange, Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getAllUsers(null);
        });

        assertEquals("Pageable object cannot be null.", exception.getMessage());
        verify(userRepository, never()).findAll(any(Pageable.class)); // Garante que o repositório NUNCA é chamado
    }

    // --- Testes para o método getUserById ---

    @Test
    @DisplayName("getUserById should return a user when ID exists")
    void getUserById_shouldReturnUser_whenIdExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        var user = new User(userId, "testuser", "password", UserRole.USER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        UserResponseDTO result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals("testuser", result.login());
        assertEquals(UserRole.USER, result.role());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("getUserById should throw UserNotFoundException when ID does not exist")
    void getUserById_shouldThrowUserNotFoundException_whenIdDoesNotExist() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        String errorMessage = "User not found with ID: " + nonExistentId;
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(nonExistentId);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("getUserById should throw IllegalArgumentException when ID is null")
    void getUserById_shouldThrowIllegalArgumentException_whenIdIsNull() {
        // Arrange, Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserById(null);
        });
        assertEquals("User ID cannot be null.", exception.getMessage());
        verify(userRepository, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("getUserById should propagate exceptions from the repository")
    void getUserById_shouldPropagateRepositoryExceptions() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String errorMessage = "Database connection failed";
        when(userRepository.findById(userId)).thenThrow(new RuntimeException(errorMessage));

        // Act & Assert
        var exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserById(userId);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    // --- Testes para o método updateUserRole ---

    @Test
    @DisplayName("updateUserRole should update user role when data is valid")
    void updateUserRole_shouldUpdateRole_whenDataIsValid() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User userToUpdate = new User(userId, "userToUpdate", "password", UserRole.USER);
        User currentUser = new User(UUID.randomUUID(), "adminUser", "password", UserRole.ADMIN);
        UserRole newRole = UserRole.ADMIN;

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));

        // Act
        UserResponseDTO result = userService.updateUserRole(userId, newRole, currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals(newRole, result.role());
        assertEquals(newRole, userToUpdate.getRole()); // Verify dirty checking
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class)); // Ensure save is not called
    }

    @Test
    @DisplayName("updateUserRole should throw UserNotFoundException when user ID does not exist")
    void updateUserRole_shouldThrowUserNotFoundException_whenIdDoesNotExist() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        User currentUser = new User(UUID.randomUUID(), "adminUser", "password", UserRole.ADMIN);
        UserRole newRole = UserRole.ADMIN;
        String errorMessage = "User not found with ID: " + nonExistentId;

        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUserRole(nonExistentId, newRole, currentUser);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, times(1)).findById(nonExistentId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserRole should throw IllegalArgumentException when user ID is null")
    void updateUserRole_shouldThrowIllegalArgumentException_whenIdIsNull() {
        // Arrange
        User currentUser = new User(UUID.randomUUID(), "adminUser", "password", UserRole.ADMIN);
        UserRole newRole = UserRole.ADMIN;
        String errorMessage = "User ID cannot be null.";

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserRole(null, newRole, currentUser);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserRole should throw IllegalArgumentException when new role is null")
    void updateUserRole_shouldThrowIllegalArgumentException_whenNewRoleIsNull() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User currentUser = new User(UUID.randomUUID(), "adminUser", "password", UserRole.ADMIN);
        String errorMessage = "New role cannot be null.";

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserRole(userId, null, currentUser);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserRole should throw IllegalArgumentException when current user is null")
    void updateUserRole_shouldThrowIllegalArgumentException_whenCurrentUserIsNull() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserRole newRole = UserRole.ADMIN;
        String errorMessage = "Current user cannot be null.";

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserRole(userId, newRole, null);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserRole should throw SelfRoleChangeNotAllowedException when ADMIN changes own role")
    void updateUserRole_shouldThrowSelfRoleChangeNotAllowedException_whenAdminChangesOwnRole() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        User adminUser = new User(adminId, "adminUser", "password", UserRole.ADMIN);
        UserRole newRole = UserRole.USER; // Trying to demote self
        String errorMessage = "An ADMIN user cannot change their own role.";

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));

        // Act & Assert
        var exception = assertThrows(SelfRoleChangeNotAllowedException.class, () -> {
            userService.updateUserRole(adminId, newRole, adminUser);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, times(1)).findById(adminId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserRole should throw LastAdminDemotionNotAllowedException when demoting last ADMIN")
    void updateUserRole_shouldThrowLastAdminDemotionNotAllowedException_whenDemotingLastAdmin() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        User adminUser = new User(adminId, "adminUser", "password", UserRole.ADMIN);
        User currentUser = new User(UUID.randomUUID(), "superAdmin", "password", UserRole.ADMIN); // Another admin
        UserRole newRole = UserRole.USER; // Trying to demote

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L); // Only one admin exists
        String errorMessage = "Cannot demote the last ADMIN user in the system.";

        // Act & Assert
        var exception = assertThrows(LastAdminDemotionNotAllowedException.class, () -> {
            userService.updateUserRole(adminId, newRole, currentUser);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, times(1)).findById(adminId);
        verify(userRepository, times(1)).countByRole(UserRole.ADMIN);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserRole should propagate exceptions from the repository")
    void updateUserRole_shouldPropagateRepositoryExceptions() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User currentUser = new User(UUID.randomUUID(), "adminUser", "password", UserRole.ADMIN);
        UserRole newRole = UserRole.USER;
        String errorMessage = "Database connection failed";

        when(userRepository.findById(userId)).thenThrow(new RuntimeException(errorMessage));

        // Act & Assert
        var exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUserRole(userId, newRole, currentUser);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    // --- Testes para o método updateUser ---

    @Test
    @DisplayName("updateUser should update login and password when data is valid and user is self")
    void updateUser_shouldUpdateLoginAndPassword_whenDataIsValidAndUserIsSelf() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User userToUpdate = new User(userId, "oldLogin", "oldPassword", UserRole.USER);
        UserUpdateDTO updateDTO = new UserUpdateDTO("newLogin", "newPassword");
        String encodedPassword = "encodedNewPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));
        when(userRepository.findByLogin("newLogin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newPassword")).thenReturn(encodedPassword);

        // Act
        UserResponseDTO result = userService.updateUser(userId, updateDTO, userToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals("newLogin", result.login());
        assertEquals("newLogin", userToUpdate.getLogin());
        assertEquals(encodedPassword, userToUpdate.getPassword());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByLogin("newLogin");
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser should update login and password when data is valid and current user is ADMIN")
    void updateUser_shouldUpdateLoginAndPassword_whenDataIsValidAndCurrentUserIsAdmin() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User userToUpdate = new User(userId, "oldLogin", "oldPassword", UserRole.USER);
        UserUpdateDTO updateDTO = new UserUpdateDTO("newLogin", "newPassword");
        User currentUser = new User(UUID.randomUUID(), "adminUser", "password", UserRole.ADMIN);
        String encodedPassword = "encodedNewPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));
        when(userRepository.findByLogin("newLogin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newPassword")).thenReturn(encodedPassword);

        // Act
        UserResponseDTO result = userService.updateUser(userId, updateDTO, currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals("newLogin", result.login());
        assertEquals("newLogin", userToUpdate.getLogin());
        assertEquals(encodedPassword, userToUpdate.getPassword());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByLogin("newLogin");
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser should throw UserNotFoundException when user ID does not exist")
    void updateUser_shouldThrowUserNotFoundException_whenIdDoesNotExist() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        User userToUpdate = new User(nonExistentId, "oldLogin", "oldPassword", UserRole.USER);
        UserUpdateDTO updateDTO = new UserUpdateDTO("newLogin", "newPassword");
        String errorMessage = "User not found with ID: " + nonExistentId;

        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(nonExistentId, updateDTO, userToUpdate);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, times(1)).findById(nonExistentId);
        verify(userRepository, never()).findByLogin(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser should throw IllegalArgumentException when user ID is null")
    void updateUser_shouldThrowIllegalArgumentException_whenIdIsNull() {
        // Arrange
        User userToUpdate = new User(UUID.randomUUID(), "oldLogin", "oldPassword", UserRole.USER);
        UserUpdateDTO updateDTO = new UserUpdateDTO("newLogin", "newPassword");
        String errorMessage = "User ID cannot be null.";

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(null, updateDTO, userToUpdate);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).findByLogin(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser should throw IllegalArgumentException when DTO is null")
    void updateUser_shouldThrowIllegalArgumentException_whenDtoIsNull() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User userToUpdate = new User(userId, "oldLogin", "oldPassword", UserRole.USER);
        String errorMessage = "UserUpdateDTO cannot be null.";

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(userId, null, userToUpdate);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).findByLogin(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser should throw IllegalArgumentException when current user is null")
    void updateUser_shouldThrowIllegalArgumentException_whenCurrentUserIsNull() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserUpdateDTO updateDTO = new UserUpdateDTO("newLogin", "newPassword");
        String errorMessage = "Current user cannot be null.";

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(userId, updateDTO, null);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).findByLogin(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser should throw SecurityException when user updates another profile without ADMIN role")
    void updateUser_shouldThrowSecurityException_whenUserUpdatesAnotherProfileWithoutAdminRole() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User userToUpdate = new User(userId, "oldLogin", "oldPassword", UserRole.USER);
        UserUpdateDTO updateDTO = new UserUpdateDTO("newLogin", "newPassword");
        User currentUser = new User(UUID.randomUUID(), "anotherUser", "password", UserRole.USER); // Not self, not ADMIN
        String errorMessage = "You are not authorized to update this user's profile.";

        // Act & Assert
        var exception = assertThrows(SecurityException.class, () -> {
            userService.updateUser(userId, updateDTO, currentUser);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).findByLogin(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser should throw UserAlreadyExistsException when new login already exists")
    void updateUser_shouldThrowUserAlreadyExistsException_whenNewLoginAlreadyExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User userToUpdate = new User(userId, "oldLogin", "oldPassword", UserRole.USER);
        UserUpdateDTO updateDTO = new UserUpdateDTO("existingLogin", "newPassword");
        User existingUserWithLogin = new User(UUID.randomUUID(), "existingLogin", "somePassword", UserRole.USER);
        String errorMessage = "Login 'existingLogin' is already in use.";

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));
        when(userRepository.findByLogin("existingLogin")).thenReturn(Optional.of(existingUserWithLogin));

        // Act & Assert
        var exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.updateUser(userId, updateDTO, userToUpdate);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByLogin("existingLogin");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser should propagate exceptions from the repository")
    void updateUser_shouldPropagateRepositoryExceptions() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User userToUpdate = new User(userId, "oldLogin", "oldPassword", UserRole.USER);
        UserUpdateDTO updateDTO = new UserUpdateDTO("newLogin", "newPassword");
        String errorMessage = "Database connection failed";

        when(userRepository.findById(userId)).thenThrow(new RuntimeException(errorMessage));

        // Act & Assert
        var exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(userId, updateDTO, userToUpdate);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).findByLogin(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // --- Testes para o método deleteUser ---

    @Test
    @DisplayName("deleteUser should delete the user when ID exists")
    void deleteUser_shouldDeleteUser_whenIdExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User userToDelete = new User(userId, "userToDelete", "password", UserRole.USER);
        User currentUser = new User(UUID.randomUUID(), "adminUser", "password", UserRole.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));
        doNothing().when(userRepository).delete(userToDelete);

        // Act
        userService.deleteUser(userId, currentUser);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(userToDelete);
    }

    @Test
    @DisplayName("deleteUser should throw UserNotFoundException when user ID does not exist")
    void deleteUser_shouldThrowUserNotFoundException_whenIdDoesNotExist() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        User currentUser = new User(UUID.randomUUID(), "adminUser", "password", UserRole.ADMIN);
        String errorMessage = "User not found with ID: " + nonExistentId;

        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(nonExistentId, currentUser);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, times(1)).findById(nonExistentId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("deleteUser should throw IllegalArgumentException when user ID is null")
    void deleteUser_shouldThrowIllegalArgumentException_whenIdIsNull() {
        // Arrange
        User currentUser = new User(UUID.randomUUID(), "adminUser", "password", UserRole.ADMIN);
        String errorMessage = "User ID cannot be null.";

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(null, currentUser);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("deleteUser should throw IllegalArgumentException when current user is null")
    void deleteUser_shouldThrowIllegalArgumentException_whenCurrentUserIsNull() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String errorMessage = "Current user cannot be null.";

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(userId, null);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("deleteUser should throw SelfDeletionNotAllowedException when user deletes self")
    void deleteUser_shouldThrowSelfDeletionNotAllowedException_whenUserDeletesSelf() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User userToDelete = new User(userId, "userToDelete", "password", UserRole.USER);
        String errorMessage = "A user cannot delete themselves.";

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));

        // Act & Assert
        var exception = assertThrows(SelfDeletionNotAllowedException.class, () -> {
            userService.deleteUser(userId, userToDelete);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("deleteUser should throw LastAdminDeletionNotAllowedException when deleting last ADMIN")
    void deleteUser_shouldThrowLastAdminDeletionNotAllowedException_whenDeletingLastAdmin() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        User adminToDelete = new User(adminId, "adminToDelete", "password", UserRole.ADMIN);
        User currentUser = new User(UUID.randomUUID(), "superAdmin", "password", UserRole.ADMIN);
        String errorMessage = "Cannot delete the last ADMIN user in the system.";

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminToDelete));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        // Act & Assert
        var exception = assertThrows(LastAdminDeletionNotAllowedException.class, () -> {
            userService.deleteUser(adminId, currentUser);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, times(1)).findById(adminId);
        verify(userRepository, times(1)).countByRole(UserRole.ADMIN);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("deleteUser should propagate exceptions from the repository")
    void deleteUser_shouldPropagateRepositoryExceptions() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User userToDelete = new User(userId, "userToDelete", "password", UserRole.USER);
        User currentUser = new User(UUID.randomUUID(), "adminUser", "password", UserRole.ADMIN);
        String errorMessage = "Database delete failed";

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));
        doThrow(new RuntimeException(errorMessage)).when(userRepository).delete(userToDelete);

        // Act & Assert
        var exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(userId, currentUser);
        });
        assertEquals(errorMessage, exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(userToDelete);
    }

    // --- Testes para o método getCurrentUser ---

    @Test
    @DisplayName("getCurrentUser should return UserResponseDTO when user is valid")
    void getCurrentUser_shouldReturnUserResponseDTO_whenUserIsValid() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "testuser", "password", UserRole.USER);

        // Act
        UserResponseDTO result = userService.getCurrentUser(user);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals("testuser", result.login());
        assertEquals(UserRole.USER, result.role());
    }

    @Test
    @DisplayName("getCurrentUser should throw IllegalArgumentException when user is null")
    void getCurrentUser_shouldThrowIllegalArgumentException_whenUserIsNull() {
        // Arrange, Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getCurrentUser(null);
        });
        assertEquals("User cannot be null.", exception.getMessage());
    }
}
