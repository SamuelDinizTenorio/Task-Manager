package br.com.gerenciador.sistema_gerenciamento_tarefas.repository;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.User;
import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para a interface {@link UserRepository}.
 * Foco: Testar as consultas customizadas e o mapeamento da entidade User.
 */
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    // --- Testes para findByLogin ---

    @Test
    @DisplayName("findByLogin should return user when user exists")
    void findByLogin_shouldReturnUser_whenUserExists() {
        // Arrange
        User user = new User("testuser", "password", UserRole.USER);
        entityManager.persistAndFlush(user);

        // Act
        Optional<UserDetails> foundUser = userRepository.findByLogin("testuser");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    @DisplayName("findByLogin should return empty when user does not exist")
    void findByLogin_shouldReturnEmpty_whenUserDoesNotExist() {
        // Arrange (nenhum usuário persistido)

        // Act
        Optional<UserDetails> foundUser = userRepository.findByLogin("nonexistentuser");

        // Assert
        assertFalse(foundUser.isPresent());
    }

    // --- Testes para countByRole ---

    @Test
    @DisplayName("countByRole should return correct count for a given role")
    void countByRole_shouldReturnCorrectCount() {
        // Arrange
        entityManager.persistAndFlush(new User("admin1", "p", UserRole.ADMIN));
        entityManager.persistAndFlush(new User("admin2", "p", UserRole.ADMIN));
        entityManager.persistAndFlush(new User("user1", "p", UserRole.USER));

        // Act
        long adminCount = userRepository.countByRole(UserRole.ADMIN);
        long userCount = userRepository.countByRole(UserRole.USER);

        // Assert
        assertEquals(2, adminCount);
        assertEquals(1, userCount);
    }

    @Test
    @DisplayName("countByRole should return zero when no users have the role")
    void countByRole_shouldReturnZero_whenNoUsersHaveRole() {
        // Arrange
        entityManager.persistAndFlush(new User("user1", "p", UserRole.USER));
        entityManager.persistAndFlush(new User("user2", "p", UserRole.USER));

        // Act
        long adminCount = userRepository.countByRole(UserRole.ADMIN);

        // Assert
        assertEquals(0, adminCount);
    }

    // --- Testes para findFirstByRole ---

    @Test
    @DisplayName("findFirstByRole should return a user when at least one user with the role exists")
    void findFirstByRole_shouldReturnUser_whenUserWithRoleExists() {
        // Arrange
        entityManager.persistAndFlush(new User("user1", "p", UserRole.USER));
        entityManager.persistAndFlush(new User("admin1", "p", UserRole.ADMIN));
        entityManager.persistAndFlush(new User("user2", "p", UserRole.USER));

        // Act
        Optional<User> foundAdmin = userRepository.findFirstByRole(UserRole.ADMIN);
        Optional<User> foundUser = userRepository.findFirstByRole(UserRole.USER);

        // Assert
        assertTrue(foundAdmin.isPresent());
        assertEquals(UserRole.ADMIN, foundAdmin.get().getRole());

        assertTrue(foundUser.isPresent());
        assertEquals(UserRole.USER, foundUser.get().getRole());
    }

    @Test
    @DisplayName("findFirstByRole should return empty when no user with the role exists")
    void findFirstByRole_shouldReturnEmpty_whenNoUserWithRoleExists() {
        // Arrange
        entityManager.persistAndFlush(new User("user1", "p", UserRole.USER));
        entityManager.persistAndFlush(new User("user2", "p", UserRole.USER));

        // Act
        Optional<User> foundAdmin = userRepository.findFirstByRole(UserRole.ADMIN);

        // Assert
        assertFalse(foundAdmin.isPresent());
    }
}
