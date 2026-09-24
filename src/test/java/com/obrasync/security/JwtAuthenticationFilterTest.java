package com.obrasync.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    @Mock private JwtService jwtService;
    @Mock private ContainerRequestContext context;
    @Mock private Claims claims;

    @Test
    void tokenAusenteOuInvalidoRetorna401SemDetalhes() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(); filter.setJwtService(jwtService);
        filter.filter(context);
        ArgumentCaptor<Response> response = ArgumentCaptor.forClass(Response.class);
        verify(context).abortWith(response.capture()); assertEquals(401,response.getValue().getStatus());
        reset(context); when(context.getHeaderString("Authorization")).thenReturn("Bearer invalido");
        when(jwtService.validarToken("invalido")).thenThrow(new io.jsonwebtoken.MalformedJwtException("dado-sensivel"));
        filter.filter(context); verify(context).abortWith(response.capture());
        assertEquals(401,response.getValue().getStatus());
        org.junit.jupiter.api.Assertions.assertFalse(((com.obrasync.rest.dto.MensagemErroDTO) response.getValue().getEntity()).getMensagem().contains("dado-sensivel"));
    }

    @Test
    void contextoValidoUsaPerfilAtualDoBancoESecureNaoRecursa() {
        when(context.getHeaderString("Authorization")).thenReturn("Bearer token"); when(context.getMethod()).thenReturn("GET");
        when(jwtService.validarToken("token")).thenReturn(claims);
        com.obrasync.service.UsuarioService usuarios=mock(com.obrasync.service.UsuarioService.class);
        com.obrasync.model.Usuario usuario=new com.obrasync.model.Usuario("Fiscal","f@teste","hash",com.obrasync.model.Perfil.FISCAL);
        when(usuarios.buscarPorEmail(null)).thenReturn(usuario);
        javax.ws.rs.core.SecurityContext anterior=mock(javax.ws.rs.core.SecurityContext.class);
        when(context.getSecurityContext()).thenReturn(anterior);when(anterior.isSecure()).thenReturn(true);
        javax.servlet.http.HttpServletRequest request=mock(javax.servlet.http.HttpServletRequest.class);
        JwtAuthenticationFilter filter=new JwtAuthenticationFilter();filter.setJwtService(jwtService);filter.setUsuarioService(usuarios);filter.setRequest(request);
        filter.filter(context);
        ArgumentCaptor<javax.ws.rs.core.SecurityContext> capture=ArgumentCaptor.forClass(javax.ws.rs.core.SecurityContext.class);
        verify(context).setSecurityContext(capture.capture());verify(context,never()).abortWith(any());
        assertEquals("f@teste",capture.getValue().getUserPrincipal().getName());
        org.junit.jupiter.api.Assertions.assertTrue(capture.getValue().isSecure());
        org.junit.jupiter.api.Assertions.assertTrue(capture.getValue().isUserInRole("FISCAL"));
        assertEquals("Bearer",capture.getValue().getAuthenticationScheme());
        verify(request).setAttribute(AccessPolicy.JWT_USER,usuario);
    }

    @Test
    void fiscalNaoPodeExcluirVistoria() throws Exception {
        when(context.getHeaderString("Authorization")).thenReturn("Bearer token");
        when(context.getMethod()).thenReturn("DELETE");
        when(jwtService.validarToken("token")).thenReturn(claims);
        com.obrasync.service.UsuarioService usuarios = mock(com.obrasync.service.UsuarioService.class);
        when(usuarios.buscarPorEmail(null)).thenReturn(new com.obrasync.model.Usuario("Fiscal", "fiscal@teste", "hash", com.obrasync.model.Perfil.FISCAL));

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        filter.setJwtService(jwtService);
        filter.setUsuarioService(usuarios);
        filter.filter(context);

        ArgumentCaptor<Response> resposta = ArgumentCaptor.forClass(Response.class);
        verify(context).abortWith(resposta.capture());
        assertEquals(403, resposta.getValue().getStatus());
    }
}
