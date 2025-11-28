package br.com.gerenciador.sistema_gerenciamento_tarefas.dto.user;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.User;
import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.UserRole;

import java.util.UUID;

/**
 * DTO para retornar dados públicos de um usuário.
 * Este DTO omite campos sensíveis como a senha.
 */
public record UserResponseDTO(
        UUID id,
        String login,
        UserRole role
) {
    /**
     * Construtor que converte uma entidade User em um UserResponseDTO.
     * @param user A entidade User a ser convertida.
     */
    public UserResponseDTO(User user) {
        this(user.getId(), user.getLogin(), user.getRole());
    }
}
