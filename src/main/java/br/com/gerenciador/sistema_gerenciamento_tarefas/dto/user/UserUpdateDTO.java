package br.com.gerenciador.sistema_gerenciamento_tarefas.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para atualização de informações de um usuário.
 * Permite a atualização parcial de campos.
 *
 * @param login O novo login do usuário. Deve ter no mínimo 3 caracteres se fornecido.
 * @param password A nova senha do usuário. Deve atender aos critérios de senha forte se fornecida.
 */
public record UserUpdateDTO(
        @Size(min = 3, message = "O login deve ter no mínimo 3 caracteres")
        String login,

        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
                message = "A senha deve ter no mínimo 8 caracteres, incluindo pelo menos uma letra maiúscula, uma minúscula, um número e um caractere especial.")
        String password
) {
}
