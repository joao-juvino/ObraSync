package com.obrasync.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.security.Principal;

/**
 * Filtro JAX-RS que intercepta requisicoes anotadas com @Secured.
 *
 * Fluxo de validacao:
 *   1. Extrai o header "Authorization: Bearer <token>"
 *   2. Valida a assinatura e expiracao do JWT via JwtService
 *   3. Se valido: injeta o SecurityContext com as claims do usuario
 *   4. Se invalido: aborta com HTTP 401 Unauthorized
 */
@Provider
@Secured
public class JwtAuthenticationFilter implements ContainerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    @Inject
    private JwtService jwtService;
    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            abortar(requestContext, "Authorization header ausente ou formato invalido. Use: Bearer <token>");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        if (token.isEmpty()) {
            abortar(requestContext, "Token JWT nao informado.");
            return;
        }

        try {
            Claims claims = jwtService.validarToken(token);
            String email  = claims.getSubject();
            String nome   = claims.get("nome",   String.class);
            String perfil = claims.get("perfil", String.class);

            if (!metodoPermitido(requestContext.getMethod(), perfil)) {
                ctx403(requestContext, "Perfil sem permissão para este recurso.");
                return;
            }

            RolesPermitidos roles = resourceInfo != null && resourceInfo.getResourceMethod() != null
                    ? resourceInfo.getResourceMethod().getAnnotation(RolesPermitidos.class) : null;
            if (roles == null && resourceInfo != null && resourceInfo.getResourceClass() != null) {
                roles = resourceInfo.getResourceClass().getAnnotation(RolesPermitidos.class);
            }
            if (roles != null && !java.util.Arrays.stream(roles.value()).anyMatch(r -> r.equalsIgnoreCase(perfil))) {
                ctx403(requestContext, "Perfil sem permissão para este recurso.");
                return;
            }

            // Injeta SecurityContext para que os recursos possam consultar o usuario autenticado
            requestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return () -> email;
                }

                @Override
                public boolean isUserInRole(String role) {
                    return role != null && role.equalsIgnoreCase(perfil);
                }

                @Override
                public boolean isSecure() {
                    return requestContext.getSecurityContext().isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return "Bearer";
                }
            });

        } catch (JwtException e) {
            abortar(requestContext, "Token JWT invalido ou expirado: " + e.getMessage());
        }
    }

    private void abortar(ContainerRequestContext ctx, String mensagem) {
        ctx.abortWith(Response
                .status(Response.Status.UNAUTHORIZED)
                .entity("{\"erro\":\"" + mensagem + "\"}")
                .header("WWW-Authenticate", "Bearer realm=\"ObraSync\"")
                .header("Content-Type", "application/json")
                .build());
    }

    private void ctx403(ContainerRequestContext ctx, String mensagem) {
        ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                .entity("{\"status\":403,\"mensagem\":\"" + mensagem + "\"}")
                .header("Content-Type", "application/json").build());
    }

    private boolean metodoPermitido(String metodo, String perfil) {
        if ("GET".equals(metodo)) return "ADMIN".equals(perfil) || "ENGENHEIRO".equals(perfil) || "FISCAL".equals(perfil);
        if ("DELETE".equals(metodo)) return "ADMIN".equals(perfil);
        return "ADMIN".equals(perfil) || "ENGENHEIRO".equals(perfil);
    }

    public void setJwtService(JwtService service) { this.jwtService = service; }
    public void setResourceInfo(ResourceInfo info) { this.resourceInfo = info; }
}
