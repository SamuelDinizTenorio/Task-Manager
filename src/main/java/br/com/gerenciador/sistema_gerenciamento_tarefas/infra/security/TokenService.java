package br.com.gerenciador.sistema_gerenciamento_tarefas.infra.security;

import br.com.gerenciador.sistema_gerenciamento_tarefas.domain.user.User;
import br.com.gerenciador.sistema_gerenciamento_tarefas.infra.validation.ValidationUtils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Serviço responsável pela geração e validação de tokens JWT (JSON Web Token).
 * Utiliza a biblioteca Auth0 JWT para criar e verificar tokens de autenticação.
 */
@Service
public class TokenService {

    private final String secret;
    private final String issuer;

    public TokenService(@Value("${api.security.token.secret}") String secret,
                        @Value("${api.security.token.issuer}") String issuer) {
        this.secret = secret;
        this.issuer = issuer;
    }

    /**
     * Gera um token JWT para o usuário fornecido.
     * O token inclui o login do usuário como 'subject' e tem uma validade de 2 horas.
     *
     * @param user O objeto User para o qual o token será gerado.
     * @return Uma string contendo o token JWT gerado.
     * @throws JWTCreationException Se ocorrer um erro durante a criação do token (ex: chave secreta inválida).
     */
    public String generateToken(User user) {
        ValidationUtils.validateNotNull(user, "User");
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            String token = JWT.create()
                    .withIssuer(issuer)
                    .withSubject(user.getLogin())
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);

            return token;
        } catch (JWTCreationException exception) {
            // Esta exceção é capturada pelo GlobalExceptionHandler
            throw new JWTCreationException("Erro ao gerar token JWT", exception);
        }
    }

    /**
     * Valida um token JWT e retorna o 'subject' (login do usuário) se o token for válido.
     * Se o token for inválido (expirado, assinatura incorreta, etc.), retorna uma string vazia.
     *
     * @param token A string do token JWT a ser validada.
     * @return O login do usuário se o token for válido, ou uma string vazia caso contrário.
     */
    public String validateToken(String token) {
        ValidationUtils.validateNotNull(token, "The token");
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            // Se a verificação falhar (token inválido, expirado, etc.), retorna uma string vazia.
            return "";
        }
    }

    /**
     * Gera a data de expiração para o token JWT, que é de 2 horas a partir do momento atual.
     *
     * @return Um objeto Instant representando a data e hora de expiração.
     */
    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
