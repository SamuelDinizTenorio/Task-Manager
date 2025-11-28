package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando um usuário tenta alterar a própria role de forma não permitida.
 */
@ResponseStatus(HttpStatus.FORBIDDEN) // 403 Forbidden
public class SelfRoleChangeNotAllowedException extends RuntimeException {
    public SelfRoleChangeNotAllowedException(String message) {
        super(message);
    }
}
