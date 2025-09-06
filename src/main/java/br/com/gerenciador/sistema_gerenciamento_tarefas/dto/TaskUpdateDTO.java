// src/main/java/br/com/gerenciador/sistema_gerenciamento_tarefas/dto/TaskUpdateDTO.java

package br.com.gerenciador.sistema_gerenciamento_tarefas.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TaskUpdateDTO(
        @Pattern(regexp = "^(?!\\s*$).+", message = "O título não pode estar em branco")
        @Size(max = 255, message = "O título deve ter no máximo 255 caracteres")
        String title,
        @Pattern(regexp = "^(?!\\s*$).+", message = "A descrição não pode estar em branco")
        String description,
        Boolean completed
) {
}