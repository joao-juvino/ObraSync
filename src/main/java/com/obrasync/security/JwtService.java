package com.obrasync.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import javax.ejb.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Servico EJB Singleton responsavel por emitir e validar tokens JWT (HMAC-SHA256).
 *
 * A chave vem de OBRASYNC_JWT_SECRET (mínimo 32 bytes). A ausência impede o deploy.
 */
@Singleton
@javax.ejb.Startup
public class JwtService {

    private static final Logger LOG = Logger.getLogger(JwtService.class.getName());

    /** Duracao padrao do token: 8 horas em milissegundos */
    private static final long EXPIRACAO_MS = 8 * 60 * 60 * 1000L;

    /** Chave HMAC-SHA256 configurada no ambiente. */
    private final SecretKey chave;

    public JwtService() {
        this(System.getenv("OBRASYNC_JWT_SECRET"));
    }

    public JwtService(String segredo) {
        if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("OBRASYNC_JWT_SECRET deve conter ao menos 32 bytes.");
        }
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gera um token JWT assinado com as informacoes do usuario autenticado.
     *
     * @param email   identificador unico do usuario (subject do token)
     * @param nome    nome completo para exibicao no cliente
     * @param perfil  perfil de acesso (ADMIN ou ENGENHEIRO)
     * @return token JWT serializado como String
     */
    public String gerarToken(String email, String nome, String perfil) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + EXPIRACAO_MS);

        return Jwts.builder()
                .setSubject(email)
                .claim("nome", nome)
                .claim("perfil", perfil)
                .setIssuedAt(agora)
                .setExpiration(expiracao)
                .signWith(chave)
                .compact();
    }

    /**
     * Valida e extrai as claims de um token JWT.
     *
     * @param token token JWT recebido no header Authorization
     * @return Claims extraidas se valido
     * @throws JwtException se o token for invalido, expirado ou adulterado
     */
    public Claims validarToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(chave)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extrai o email (subject) de um token sem lancar excecao — retorna null se invalido.
     */
    public String extrairEmail(String token) {
        try {
            return validarToken(token).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            LOG.fine("Token inválido.");
            return null;
        }
    }

    /**
     * Verifica rapidamente se um token e valido (sem lancar excecao).
     */
    public boolean isTokenValido(String token) {
        try {
            validarToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
