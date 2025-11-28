package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando se tenta deletar o último usuário administrador do sistema.
 */
@ResponseStatus(HttpStatus.FORBIDDEN) // 403 Forbidden
public class LastAdminDeletionNotAllowedException extends RuntimeException {
    public LastAdminDeletionNotAllowedException(String message) {
        super(message);
    }
}
