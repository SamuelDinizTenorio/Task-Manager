package br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) para a criação de uma nova tarefa.
 * Contém os dados necessários fornecidos pelo cliente para criar uma tarefa.
 *
 * @param title O título da tarefa. É obrigatório e deve ter entre 3 e 255 caracteres.
 * @param description A descrição detalhada da tarefa. É opcional.
 */
public record TaskCreateDTO(
        @NotBlank(message = "O título é obrigatório")
        @Size(min = 3, max = 255, message = "O título deve ter entre 3 e 255 caracteres")
        String title,
        String description
) {
}
