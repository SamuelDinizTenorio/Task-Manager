package br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user;

import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.UserAlreadyExistsException;
import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para a entidade {@link User}.
 * Foco: Testar a lógica de negócio encapsulada na própria entidade.
 */
@ExtendWith(MockitoExtension.class)
class UserTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(UUID.randomUUID(), "oldLogin", "oldPassword", UserRole.USER);
    }

    // --- Testes para changeLogin ---

    @Test
    @DisplayName("changeLogin should update login when new login is available")
    void changeLogin_shouldUpdateLogin_whenNewLoginIsAvailable() {
        // Arrange
        String newLogin = "newLogin";
        when(userRepository.findByLogin(newLogin)).thenReturn(Optional.empty());

        // Act
        user.changeLogin(newLogin, userRepository);

        // Assert
        assertEquals(newLogin, user.getLogin());
    }

    @Test
    @DisplayName("changeLogin should throw UserAlreadyExistsException when new login is taken")
    void changeLogin_shouldThrowUserAlreadyExistsException_whenNewLoginIsTaken() {
        // Arrange
        String newLogin = "existingLogin";
        when(userRepository.findByLogin(newLogin)).thenReturn(Optional.of(new User()));

        // Act & Assert
        var exception = assertThrows(UserAlreadyExistsException.class, () -> {
            user.changeLogin(newLogin, userRepository);
        });
        assertEquals("Login '" + newLogin + "' is already in use.", exception.getMessage());
        assertEquals("oldLogin", user.getLogin()); // Garante que o login não foi alterado
    }

    @Test
    @DisplayName("changeLogin should throw IllegalArgumentException when new login is null")
    void changeLogin_shouldThrowIllegalArgumentException_whenNewLoginIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            user.changeLogin(null, userRepository);
        });
        assertEquals("oldLogin", user.getLogin());
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("changeLogin should throw IllegalArgumentException when new login is blank")
    void changeLogin_shouldThrowIllegalArgumentException_whenNewLoginIsBlank() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            user.changeLogin("  ", userRepository);
        });
        assertEquals("oldLogin", user.getLogin());
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("changeLogin should do nothing when new login is the same as the old one")
    void changeLogin_shouldDoNothing_whenNewLoginIsSameAsOld() {
        // Arrange
        String sameLogin = "oldLogin";

        // Act
        user.changeLogin(sameLogin, userRepository);

        // Assert
        assertEquals(sameLogin, user.getLogin());
        // Nenhuma interação com o repositório é esperada
        verifyNoInteractions(userRepository);
    }

    // --- Testes para changePassword ---

    @Test
    @DisplayName("changePassword should update password with encoded value")
    void changePassword_shouldUpdatePassword_withEncodedValue() {
        // Arrange
        String newPassword = "newPassword123";
        String encodedPassword = "encodedNewPassword";
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        // Act
        user.changePassword(newPassword, passwordEncoder);

        // Assert
        assertEquals(encodedPassword, user.getPassword());
    }

    @Test
    @DisplayName("changePassword should throw IllegalArgumentException when new password is null")
    void changePassword_shouldThrowIllegalArgumentException_whenNewPasswordIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            user.changePassword(null, passwordEncoder);
        });
        assertEquals("oldPassword", user.getPassword());
    }

    @Test
    @DisplayName("changePassword should throw IllegalArgumentException when new password is blank")
    void changePassword_shouldThrowIllegalArgumentException_whenNewPasswordIsBlank() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            user.changePassword("  ", passwordEncoder);
        });
        assertEquals("oldPassword", user.getPassword());
    }

    // --- Testes para assignRole ---

    @Test
    @DisplayName("assignRole should change the user's role")
    void assignRole_shouldChangeUserRole() {
        // Arrange
        UserRole newRole = UserRole.ADMIN;

        // Act
        user.assignRole(newRole);

        // Assert
        assertEquals(newRole, user.getRole());
    }
}
