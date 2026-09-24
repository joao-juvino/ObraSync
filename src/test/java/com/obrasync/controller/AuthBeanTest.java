package com.obrasync.controller;

import com.obrasync.model.*;
import com.obrasync.service.UsuarioService;
import javax.faces.context.*;
import javax.servlet.http.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthBeanTest {
    abstract static class ContextSetter extends FacesContext {
        static void usar(FacesContext context) { setCurrentInstance(context); }
    }
    FacesContext context; ExternalContext external; HttpServletRequest request; HttpSession session;
    UsuarioService service; AuthBean bean;
    @BeforeEach void preparar() {
        context = mock(FacesContext.class); external = mock(ExternalContext.class);
        request = mock(HttpServletRequest.class); session = mock(HttpSession.class);
        when(context.getExternalContext()).thenReturn(external); when(external.getRequest()).thenReturn(request);
        when(external.getSession(true)).thenReturn(session); when(external.getSession(false)).thenReturn(session);
        ContextSetter.usar(context); service = mock(UsuarioService.class); bean = new AuthBean(); bean.setUsuarioService(service);
    }
    @AfterEach void limpar() { ContextSetter.usar(null); }
    @Test @DisplayName("Login troca ID da sessão, limpa senha e logout invalida sessão")
    void loginLogout() {
        Usuario u = new Usuario("Admin","admin@teste","hash",Perfil.ADMIN);
        when(service.autenticar("admin@teste","senha")).thenReturn(u);
        bean.setEmail(" admin@teste "); bean.setSenha("senha");
        assertEquals("/vistorias.xhtml?faces-redirect=true",bean.login());
        verify(request).changeSessionId(); verify(session).setAttribute("usuarioLogado",u);
        assertTrue(bean.isAutenticado()); assertNull(bean.getSenha()); assertSame(u,bean.getUsuarioLogado());
        assertEquals("/login.xhtml?faces-redirect=true",bean.logout()); verify(session).invalidate();
        assertFalse(bean.isAutenticado()); assertNull(bean.getEmail());
    }
    @Test @DisplayName("Login inválido não cria usuário autenticado e limpa senha")
    void invalido() {
        assertNull(bean.login()); verifyNoInteractions(service);
        bean.setEmail("a@b"); bean.setSenha("errada"); assertNull(bean.login());
        assertFalse(bean.isAutenticado()); assertNull(bean.getSenha()); verify(request,never()).changeSessionId();
    }
}
