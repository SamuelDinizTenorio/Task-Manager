package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração global de CORS (Cross-Origin Resource Sharing) para a aplicação.
 * Permite que recursos de um domínio sejam acessados por requisições de outro domínio,
 * seguindo as políticas definidas.
 */
@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    /**
     * Origens permitidas para requisições CORS.
     * O valor é injetado a partir das propriedades da aplicação (ex: application.properties).
     */
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * Configura as regras de CORS para a aplicação.
     * @param registry O registro de CORS para adicionar as configurações.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a configuração a todos os endpoints da API
                .allowedOrigins(allowedOrigins) // Lê as origens permitidas do application.properties
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD") // Permite estes métodos HTTP
                .allowedHeaders("Content-Type", "Authorization") // Permite estes cabeçalhos
                .allowCredentials(true); // Permite o envio de credenciais (como cookies)
    }
}
