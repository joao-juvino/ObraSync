package com.obrasync.filter;
import javax.servlet.*;
import javax.servlet.http.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
class AuthorizationFilterTest {
    @Test @DisplayName("Páginas e imagens de evidência exigem sessão; login e API têm acesso próprio")
    void rotas() throws Exception {
        for (String path : new String[]{"/vistorias.xhtml","/evidencias/imagem","/login.xhtml","/api/vistorias","/api-docs/index.html","/javax.faces.resource/theme.css"}) {
            HttpServletRequest req=mock(HttpServletRequest.class); HttpServletResponse resp=mock(HttpServletResponse.class); FilterChain chain=mock(FilterChain.class);
            when(req.getContextPath()).thenReturn("/obrasync"); when(req.getRequestURI()).thenReturn("/obrasync"+path);
            new AuthorizationFilter().doFilter(req,resp,chain);
            if (path.equals("/vistorias.xhtml")||path.equals("/evidencias/imagem")) {
                verify(resp).sendRedirect("/obrasync/login.xhtml"); verifyNoInteractions(chain);
            } else verify(chain).doFilter(req,resp);
        }
    }
    @Test @DisplayName("Sessão autenticada acessa vistorias")
    void autenticado() throws Exception {
        HttpServletRequest req=mock(HttpServletRequest.class); HttpServletResponse resp=mock(HttpServletResponse.class); FilterChain chain=mock(FilterChain.class);
        when(req.getContextPath()).thenReturn("/obrasync"); when(req.getRequestURI()).thenReturn("/obrasync/vistorias.xhtml");
        HttpSession session=mock(HttpSession.class); when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(new com.obrasync.model.Usuario());
        new AuthorizationFilter().doFilter(req,resp,chain); verify(chain).doFilter(req,resp); verifyNoInteractions(resp);
    }
}
