package br.com.gerenciador.sistema_gerenciamento_tarefas.controller;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.User;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.authentication.AuthenticationDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.authentication.AuthenticationResponseDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.dto.authentication.RegisterDTO;
import br.com.gerenciador.sistema_gerenciamento_tarefas.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Controller responsável pelos endpoints de autenticação e registro de usuários.
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Realiza a autenticação de um usuário e retorna um token JWT se as credenciais forem válidas.
     *
     * @param data O DTO contendo o login e a senha do usuário.
     * @return Um ResponseEntity com o token JWT encapsulado em um AuthenticationResponseDTO.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(@RequestBody @Valid AuthenticationDTO data) {
        log.info("Request received to login user: {}", data.login());
        String token = authenticationService.login(data);
        log.info("Login successful for user: {}", data.login());
        return ResponseEntity.ok(new AuthenticationResponseDTO(token));
    }

    /**
     * Registra um novo usuário no sistema.
     *
     * @param data O DTO contendo os dados para o registro do novo usuário.
     * @return Um ResponseEntity com status 201 Created e o cabeçalho Location contendo a URI do novo recurso.
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterDTO data) {
        log.info("Request received to register new user: {}", data.login());
        User savedUser = authenticationService.register(data);
        log.info("User {} registered successfully.", data.login());

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{id}")
                .buildAndExpand(savedUser.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
