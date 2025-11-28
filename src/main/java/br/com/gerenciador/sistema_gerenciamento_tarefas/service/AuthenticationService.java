package br.com.gerenciador.sistema_gerenciamento_tarefas.service;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.User;
import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.UserRole;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.authentication.AuthenticationDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.authentication.RegisterDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception.UserAlreadyExistsException;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security.TokenService;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.validation.ValidationUtils;
import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthenticationService(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public String login(AuthenticationDTO data) {
        log.info("Request received to login user: {}", data.login());

        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        Authentication auth = this.authenticationManager.authenticate(usernamePassword);

        String token = tokenService.generateToken((User) auth.getPrincipal());
        log.info("User '{}' logged in successfully.", data.login());
        return token;
    }

    public User register(RegisterDTO data) {
        ValidationUtils.validateNotNull(data, "RegisterDTO");
        log.info("Request received to register user: {}", data.login());

        if (userRepository.findByLogin(data.login()).isPresent()) {
            log.warn("User with login '{}' already exists.", data.login());
            throw new UserAlreadyExistsException("User with this login already exists.");
        }

        String encryptedPassword = passwordEncoder.encode(data.password());
        User newUser = new User(data.login(), encryptedPassword, UserRole.USER);

        userRepository.save(newUser);
        log.info("User '{}' registered successfully with role {}.", newUser.getLogin(), newUser.getRole());
        return newUser;
    }
}
