package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.allOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(useDefaultFilters = false)
@Import({CorsConfiguration.class, CorsConfigurationTest.TestController.class})
@TestPropertySource(properties = "app.cors.allowed-origins=http://allowed-origin.com")
class CorsConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    // Um controller simples apenas para termos um endpoint para testar.
    @RestController
    static class TestController {
        @GetMapping("/test-endpoint")
        public ResponseEntity<String> testEndpoint() {
            return ResponseEntity.ok("OK");
        }
    }

    @Test
    @DisplayName("Deve retornar headers de CORS para uma origem permitida")
    @WithMockUser
    void whenRequestFromAllowedOrigin_thenReturnsCorrectCorsHeaders() throws Exception {
        String allowedOrigin = "http://allowed-origin.com";

        mockMvc.perform(get("/test-endpoint")
                        .header("Origin", allowedOrigin))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", allowedOrigin))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Não deve retornar header 'Access-Control-Allow-Origin' para uma origem não permitida")
    @WithMockUser
    void whenRequestFromDisallowedOrigin_thenDoesNotReturnCorsHeader() throws Exception {
        String disallowedOrigin = "http://disallowed-origin.com";

        mockMvc.perform(get("/test-endpoint")
                        .header("Origin", disallowedOrigin))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Deve responder corretamente a uma requisição de preflight (OPTIONS)")
    @WithMockUser
    void whenPreflightRequestFromAllowedOrigin_thenReturnsCorrectCorsHeaders() throws Exception {
        String allowedOrigin = "http://allowed-origin.com";

        mockMvc.perform(options("/test-endpoint")
                        .header("Origin", allowedOrigin)
                        .header("Access-Control-Request-Method", "PUT")
                        .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", allowedOrigin))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,PATCH,OPTIONS,HEAD"))
                .andExpect(header().string("Access-Control-Allow-Headers", allOf(
                        containsString("Content-Type"),
                        containsString("Authorization")
                )))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
