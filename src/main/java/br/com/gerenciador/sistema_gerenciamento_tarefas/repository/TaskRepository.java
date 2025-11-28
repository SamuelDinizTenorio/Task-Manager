package br.com.gerenciador.sistema_gerenciamento_tarefas.repository;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório para a entidade Task.
 * Fornece métodos CRUD (Create, Read, Update, Delete) e de paginação
 * para operações com tarefas no banco de dados.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {
}
