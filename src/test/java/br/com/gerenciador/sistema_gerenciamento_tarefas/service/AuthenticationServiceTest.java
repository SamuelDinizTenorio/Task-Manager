package br.com.gerenciador.sistema_gerenciamento_tarefas.service;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.User;
import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.UserRole;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.authentication.AuthenticationDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.authentication.RegisterDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.UserAlreadyExistsException;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security.TokenService;
import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe {@link AuthenticationService}.
 * Foco: Testar a lógica de negócio de login e registro em isolamento.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthenticationService authenticationService;

    // --- Testes para o método login ---

    @Test
    @DisplayName("login should return a valid token and call AuthenticationManager with correct credentials")
    void login_shouldReturnToken_andCallAuthenticationManagerWithCorrectCredentials() {
        // Arrange
        var loginDTO = new AuthenticationDTO("user", "password");
        var user = new User("user", "encodedPassword", UserRole.USER);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null);
        String expectedToken = "fake.jwt.token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenService.generateToken(user)).thenReturn(expectedToken);

        // Act
        String actualToken = authenticationService.login(loginDTO);

        // Assert
        assertNotNull(actualToken);
        assertEquals(expectedToken, actualToken);

        // Captura e verifica o argumento passado para o authenticationManager
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        UsernamePasswordAuthenticationToken capturedToken = captor.getValue();

        assertEquals(loginDTO.login(), capturedToken.getName());
        assertEquals(loginDTO.password(), capturedToken.getCredentials());

        verify(tokenService, times(1)).generateToken(user);
    }

    @Test
    @DisplayName("login should throw BadCredentialsException when credentials are invalid")
    void login_shouldThrowBadCredentialsException_whenCredentialsAreInvalid() {
        // Arrange
        var loginDTO = new AuthenticationDTO("user", "wrongpassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.login(loginDTO);
        });
        verify(tokenService, never()).generateToken(any(User.class));
    }

    // --- Testes para o método register ---

    @Test
    @DisplayName("register should save user with correct data when login is new")
    void register_shouldSaveUserWithCorrectData_whenLoginIsNew() {
        // Arrange
        var registerDTO = new RegisterDTO("newUser", "password123");
        String encodedPassword = "encodedPassword";

        when(userRepository.findByLogin(registerDTO.login())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerDTO.password())).thenReturn(encodedPassword);

        // Act
        authenticationService.register(registerDTO);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNotNull(capturedUser);
        assertEquals(registerDTO.login(), capturedUser.getLogin());
        assertEquals(encodedPassword, capturedUser.getPassword());
        assertEquals(UserRole.USER, capturedUser.getRole());
    }

    @Test
    @DisplayName("register should throw UserAlreadyExistsException when login already exists")
    void register_shouldThrowException_whenLoginAlreadyExists() {
        // Arrange
        var registerDTO = new RegisterDTO("existingUser", "password123");
        Optional<UserDetails> existingUser = Optional.of(new User("existingUser", "anypassword", UserRole.USER));
        when(userRepository.findByLogin(registerDTO.login())).thenReturn(existingUser);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            authenticationService.register(registerDTO);
        });
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("register should throw IllegalArgumentException when DTO is null")
    void register_shouldThrowException_whenDtoIsNull() {
        // Arrange, Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.register(null);
        });

        assertEquals("RegisterDTO cannot be null.", exception.getMessage());

        verify(userRepository, never()).findByLogin(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }
}
