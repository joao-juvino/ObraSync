package com.obrasync.security;

import com.obrasync.model.Usuario;
import com.obrasync.rest.dto.MensagemErroDTO;
import com.obrasync.service.UsuarioService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Provider;
import java.security.Principal;
import java.util.Arrays;

@Provider @Secured @Priority(Priorities.AUTHENTICATION)
public class JwtAuthenticationFilter implements ContainerRequestFilter {
    @Inject private JwtService jwtService;
    @Inject private UsuarioService usuarios;
    @Context private ResourceInfo resourceInfo;
    @Context private HttpServletRequest request;

    @Override public void filter(ContainerRequestContext context) {
        String header = context.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ") || header.substring(7).isBlank()) {
            abortar(context, 401, "Token Bearer obrigatório.");
            return;
        }
        try {
            Claims claims = jwtService.validarToken(header.substring(7).trim());
            Usuario usuario = usuarios.buscarPorEmail(claims.getSubject());
            if (usuario == null || usuario.getPerfil() == null) {
                abortar(context, 401, "Usuário não disponível."); return;
            }
            String perfil = usuario.getPerfil().name();
            RolesPermitidos roles = resourceInfo == null || resourceInfo.getResourceMethod() == null ? null : resourceInfo.getResourceMethod().getAnnotation(RolesPermitidos.class);
            if (roles == null && resourceInfo != null && resourceInfo.getResourceClass() != null) roles = resourceInfo.getResourceClass().getAnnotation(RolesPermitidos.class);
            boolean permitido = roles != null ? Arrays.asList(roles.value()).contains(perfil)
                    : "GET".equals(context.getMethod()) || "HEAD".equals(context.getMethod())
                    || ("DELETE".equals(context.getMethod()) ? "ADMIN".equals(perfil)
                    : "ADMIN".equals(perfil) || "ENGENHEIRO".equals(perfil));
            if (!permitido) { abortar(context, 403, "Perfil sem permissão para este recurso."); return; }
            final boolean secure = context.getSecurityContext() != null && context.getSecurityContext().isSecure();
            request.setAttribute(AccessPolicy.JWT_USER, usuario);
            context.setSecurityContext(new SecurityContext() {
                public Principal getUserPrincipal() { return usuario::getEmail; }
                public boolean isUserInRole(String role) { return perfil.equals(role); }
                public boolean isSecure() { return secure; }
                public String getAuthenticationScheme() { return "Bearer"; }
            });
        } catch (JwtException | IllegalArgumentException e) {
            abortar(context, 401, "Token inválido ou expirado.");
        }
    }
    private void abortar(ContainerRequestContext context, int status, String mensagem) {
        Response.ResponseBuilder response = Response.status(status).type(MediaType.APPLICATION_JSON)
                .entity(new MensagemErroDTO(status, mensagem));
        if (status == 401) response.header("WWW-Authenticate", "Bearer realm=\"ObraSync\"");
        context.abortWith(response.build());
    }
    public void setJwtService(JwtService service) { jwtService = service; }
    public void setUsuarioService(UsuarioService service) { usuarios = service; }
    public void setResourceInfo(ResourceInfo info) { resourceInfo = info; }
    public void setRequest(HttpServletRequest request) { this.request = request; }
}
