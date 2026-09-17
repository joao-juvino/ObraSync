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
    void fiscalNaoPodeExcluirVistoria() throws Exception {
        when(context.getHeaderString("Authorization")).thenReturn("Bearer token");
        when(context.getMethod()).thenReturn("DELETE");
        when(jwtService.validarToken("token")).thenReturn(claims);
        when(claims.get("nome", String.class)).thenReturn("Fiscal Teste");
        when(claims.get("perfil", String.class)).thenReturn("FISCAL");

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        filter.setJwtService(jwtService);
        filter.filter(context);

        ArgumentCaptor<Response> resposta = ArgumentCaptor.forClass(Response.class);
        verify(context).abortWith(resposta.capture());
        assertEquals(403, resposta.getValue().getStatus());
    }
}
