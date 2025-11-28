package br.com.gerenciador.sistema_gerenciamento_tarefas.dto.authentication;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Data Transfer Object) para o processo de autenticação (login).
 * Contém as credenciais necessárias para que um usuário se autentique no sistema.
 *
 * @param login O login do usuário.
 * @param password A senha do usuário.
 */
public record AuthenticationDTO(
        @NotBlank(message = "O login não pode estar em branco.")
        String login,

        @NotBlank(message = "A senha não pode estar em branco.")
        String password
) {
}
