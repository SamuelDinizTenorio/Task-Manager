package br.com.gerenciador.sistema_gerenciamento_tarefas.dto.task;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.task.Task;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para retornar dados de uma tarefa para o cliente.
 * Representa a visão pública de uma tarefa.
 *
 * @param id O ID único da tarefa.
 * @param title O título da tarefa.
 * @param description A descrição da tarefa.
 * @param creationDate A data e hora em que a tarefa foi criada.
 * @param completed O estado de conclusão da tarefa.
 */
public record TaskResponseDTO(
        Long id,
        String title,
        String description,
        LocalDateTime creationDate,
        Boolean completed
) {
    /**
     * Construtor que converte uma entidade Task em um TaskResponseDTO.
     * @param task A entidade Task a ser convertida.
     */
    public TaskResponseDTO(Task task) {
        this(task.getId(), task.getTitle(), task.getDescription(), task.getCreationDate(), task.getCompleted());
    }
}
