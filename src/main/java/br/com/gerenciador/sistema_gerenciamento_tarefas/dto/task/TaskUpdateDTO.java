package br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) para a atualização de uma tarefa existente.
 * Todos os campos são opcionais; apenas os campos fornecidos serão atualizados.
 *
 * @param title O novo título da tarefa.
 * @param description A nova descrição da tarefa.
 * @param completed O novo estado de conclusão da tarefa (true para concluída, false para pendente).
 */
public record TaskUpdateDTO(
        @Pattern(regexp = "^(?!\\s*$).+", message = "O título não pode estar em branco")
        @Size(max = 255, message = "O título deve ter no máximo 255 caracteres")
        String title,
        @Pattern(regexp = "^(?!\\s*$).+", message = "A descrição não pode estar em branco")
        String description,
        Boolean completed
) {
}
