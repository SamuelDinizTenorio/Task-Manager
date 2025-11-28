package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.validation;

/**
 * Classe de utilitário para funções de validação comuns.
 * Esta classe não pode ser instanciada e contém apenas métodos estáticos.
 */
public final class ValidationUtils {

    /**
     * Construtor privado para prevenir a instanciação da classe.
     */
    private ValidationUtils() {
        // Previne a instanciação
    }

    /**
     * Valida se um objeto genérico não é nulo.
     *
     * @param object O objeto a ser validado.
     * @param name O nome do objeto para ser usado na mensagem de exceção.
     * @param <T> O tipo do objeto.
     * @throws IllegalArgumentException se o objeto for nulo.
     */
    public static <T> void validateNotNull(T object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(name + " cannot be null.");
        }
    }

    /**
     * Valida se uma string não é nula ou em branco.
     *
     * @param value O valor da string a ser validada.
     * @param fieldName O nome do campo para ser usado na mensagem de exceção.
     * @throws IllegalArgumentException se o valor for nulo ou em branco.
     */
    public static void validateStringNotNullOrBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank.");
        }
    }
}
