package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security;

import br.com.gerenciador.sistema_gerenciamento_tarefas.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de segurança que intercepta todas as requisições para validar o token JWT.
 * Este filtro é executado uma vez por requisição e é responsável por autenticar o usuário
 * se um token válido for encontrado no cabeçalho de autorização.
 */
@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Autowired
    public SecurityFilter(TokenService tokenService,
                          UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    /**
     * Lógica principal do filtro. Executado para cada requisição.
     *
     * @param request  O objeto da requisição HTTP.
     * @param response O objeto da resposta HTTP.
     * @param filterChain O objeto para invocar o próximo filtro na cadeia.
     * @throws ServletException Se ocorrer um erro no servlet.
     * @throws IOException Se ocorrer um erro de I/O.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = recoverToken(request);

        if (token != null) {
            var login = tokenService.validateToken(token);
            if (login != null && !login.isEmpty()) {
                UserDetails user = userRepository.findByLogin(login)
                        .orElseThrow(() -> new RuntimeException("User not found from token subject")); // Lança exceção se o usuário do token não existir mais

                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                // Define o usuário como autenticado no contexto de segurança do Spring
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Continua a execução da cadeia de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o token JWT do cabeçalho 'Authorization' da requisição.
     *
     * @param request O objeto da requisição HTTP.
     * @return A string do token sem o prefixo "Bearer ", ou null se o cabeçalho não existir.
     */
    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.replace("Bearer ", "");
    }
}
