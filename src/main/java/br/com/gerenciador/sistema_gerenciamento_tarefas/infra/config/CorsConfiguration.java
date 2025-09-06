package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a configuração a todos os endpoints da API
                .allowedOrigins(allowedOrigins) // Lê as origens do application.properties
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD") // Permite estes métodos HTTP
                .allowedHeaders("Content-Type", "Authorization") // Permite estes cabeçalhos
                .allowCredentials(true); // Permite o envio de credenciais (como cookies)
    }
}
