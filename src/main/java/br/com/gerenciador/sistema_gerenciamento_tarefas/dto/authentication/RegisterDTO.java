package br.com.gerenciador.sistema_gerenciamento_tarefas.dto.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) para o registro de um novo usuário.
 * Contém os dados necessários para criar uma nova conta de usuário.
 *
 * @param login O login do novo usuário. Deve ser único.
 * @param password A senha do novo usuário. Deve atender aos critérios de senha forte.
 */
public record RegisterDTO(
        @NotBlank
        @Size(min = 3, message = "O login deve ter no mínimo 3 caracteres")
        String login,

        @NotBlank
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
                message = "A senha deve ter no mínimo 8 caracteres, incluindo pelo menos uma letra maiúscula, uma minúscula, um número e um caractere especial.")
        String password
) {
}
