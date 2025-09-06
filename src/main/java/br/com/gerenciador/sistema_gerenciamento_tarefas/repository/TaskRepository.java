package br.com.gerenciador.sistema_gerenciamento_tarefas.repository;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findAll(Pageable pageable);
    Optional<Task> findById(Long id);
}
