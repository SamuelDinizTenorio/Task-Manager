package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.User;
import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.UserRole;
import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe {@link SecurityFilter}.
 * Foco: Testar a lógica de extração e validação do token JWT e a configuração do contexto de segurança.
 */
@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityFilter securityFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        // Limpa o contexto de segurança antes de cada teste
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Garante que o contexto de segurança seja limpo após cada teste
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal should set authentication when token is valid")
    void doFilterInternal_shouldSetAuthentication_whenTokenIsValid() throws ServletException, IOException {
        // Arrange
        String token = "valid.jwt.token";
        String login = "testuser";
        User user = new User(UUID.randomUUID(), login, "password", UserRole.USER);

        request.addHeader("Authorization", "Bearer " + token);
        when(tokenService.validateToken(token)).thenReturn(login);
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(login, SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal should do nothing when token is missing")
    void doFilterInternal_shouldDoNothing_whenTokenIsMissing() throws ServletException, IOException {
        // Arrange (sem header "Authorization")

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal should do nothing when token is invalid")
    void doFilterInternal_shouldDoNothing_whenTokenIsInvalid() throws ServletException, IOException {
        // Arrange
        String token = "invalid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);
        when(tokenService.validateToken(token)).thenReturn(""); // Token inválido retorna string vazia

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userRepository, never()).findByLogin(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal should throw exception when user from token is not found")
    void doFilterInternal_shouldThrowException_whenUserNotFound() {
        // Arrange
        String token = "valid.token.for.nonexistent.user";
        String login = "nonexistentuser";
        request.addHeader("Authorization", "Bearer " + token);
        when(tokenService.validateToken(token)).thenReturn(login);
        when(userRepository.findByLogin(login)).thenReturn(Optional.empty()); // Usuário não encontrado

        // Act & Assert
        var exception = assertThrows(RuntimeException.class, () -> {
            securityFilter.doFilterInternal(request, response, filterChain);
        });

        assertEquals("User not found from token subject", exception.getMessage());
        assertNull(SecurityContextHolder.getContext().getAuthentication()); // Contexto não deve ser setado
        verifyNoInteractions(filterChain); // filterChain.doFilter não deve ser chamado após a exceção
    }
}
