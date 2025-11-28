package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.User;
import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.UserRole;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe {@link TokenService}.
 * Foco: Testar a geração e validação de tokens JWT.
 */
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    private final String TEST_SECRET = "my-test-secret-key-that-is-long-enough-for-hmac256";
    private final String TEST_ISSUER = "test-issuer";

    @BeforeEach
    void setUp() {
        // Instancia o serviço manualmente, passando os valores de teste para o construtor
        tokenService = new TokenService(TEST_SECRET, TEST_ISSUER);
    }

    // --- Testes para generateToken ---

    @Test
    @DisplayName("generateToken should create a valid token with the correct subject")
    void generateToken_shouldCreateValidToken_withCorrectSubject() {
        // Arrange
        User user = new User(UUID.randomUUID(), "testuser", "password", UserRole.USER);

        // Act
        String token = tokenService.generateToken(user);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verifica o token decodificando-o
        Algorithm algorithm = Algorithm.HMAC256(TEST_SECRET);
        JWT.require(algorithm)
                .withIssuer(TEST_ISSUER)
                .withSubject(user.getLogin())
                .build()
                .verify(token); // Se não lançar exceção, o token é válido e o subject está correto
    }

    @Test
    @DisplayName("generateToken should throw IllegalArgumentException if secret is invalid")
    void generateToken_shouldThrowIllegalArgumentException_ifSecretIsInvalid() {
        // Arrange
        User user = new User(UUID.randomUUID(), "testuser", "password", UserRole.USER);
        // Cria uma instância local do serviço com um secret inválido
        TokenService tokenServiceWithInvalidSecret = new TokenService("", TEST_ISSUER);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            tokenServiceWithInvalidSecret.generateToken(user);
        });
    }

    @Test
    @DisplayName("generateToken should throw IllegalArgumentException when user is null")
    void generateToken_shouldThrowIllegalArgumentException_whenUserIsNull() {
        // Arrange, Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            tokenService.generateToken(null);
        });
        assertEquals("User cannot be null.", exception.getMessage());
    }

    // --- Testes para validateToken ---

    @Test
    @DisplayName("validateToken should return login when token is valid")
    void validateToken_shouldReturnLogin_whenTokenIsValid() {
        // Arrange
        User user = new User(UUID.randomUUID(), "validuser", "password", UserRole.USER);
        String validToken = tokenService.generateToken(user); // Gera um token válido

        // Act
        String login = tokenService.validateToken(validToken);

        // Assert
        assertNotNull(login);
        assertEquals(user.getLogin(), login);
    }

    @Test
    @DisplayName("validateToken should return empty string when token is invalid")
    void validateToken_shouldReturnEmptyString_whenTokenIsInvalid() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        String login = tokenService.validateToken(invalidToken);

        // Assert
        assertNotNull(login);
        assertTrue(login.isEmpty());
    }

    @Test
    @DisplayName("validateToken should throw IllegalArgumentException when token is null")
    void validateToken_shouldThrowIllegalArgumentException_whenTokenIsNull() {
        // Arrange, Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            tokenService.validateToken(null);
        });
        assertEquals("The token cannot be null.", exception.getMessage());
    }

    @Test
    @DisplayName("validateToken should return empty string when token is expired")
    void validateToken_shouldReturnEmptyString_whenTokenIsExpired() {
        // Arrange
        User user = new User(UUID.randomUUID(), "expireduser", "password", UserRole.USER);
        Algorithm algorithm = Algorithm.HMAC256(TEST_SECRET);

        // Gera um token que já expirou
        String expiredToken = JWT.create()
                .withIssuer(TEST_ISSUER)
                .withSubject(user.getLogin())
                .withExpiresAt(Instant.now().minusSeconds(1)) // Expira 1 segundo atrás
                .sign(algorithm);

        // Act
        String login = tokenService.validateToken(expiredToken);

        // Assert
        assertNotNull(login);
        assertTrue(login.isEmpty());
    }

    @Test
    @DisplayName("validateToken should return empty string when token has wrong issuer")
    void validateToken_shouldReturnEmptyString_whenTokenHasWrongIssuer() {
        // Arrange
        User user = new User(UUID.randomUUID(), "user", "password", UserRole.USER);
        Algorithm algorithm = Algorithm.HMAC256(TEST_SECRET);

        // Gera um token com issuer diferente
        String tokenWithWrongIssuer = JWT.create()
                .withIssuer("wrong-issuer")
                .withSubject(user.getLogin())
                .withExpiresAt(generateFutureExpirationDate())
                .sign(algorithm);

        // Act
        String login = tokenService.validateToken(tokenWithWrongIssuer);

        // Assert
        assertNotNull(login);
        assertTrue(login.isEmpty());
    }

    // Método auxiliar para gerar uma data de expiração no futuro
    private Instant generateFutureExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
