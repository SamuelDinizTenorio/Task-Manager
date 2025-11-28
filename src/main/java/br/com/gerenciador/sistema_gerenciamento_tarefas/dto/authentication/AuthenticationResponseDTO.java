package br.com.gerenciador.sistema_gerenciamento_tarefas.dto.authentication;

/**
 * DTO (Data Transfer Object) para a resposta de uma autenticação bem-sucedida.
 * Contém o token JWT que o cliente deve usar para autenticar requisições subsequentes.
 *
 * @param token O token JWT gerado.
 */
public record AuthenticationResponseDTO(String token) {
}
