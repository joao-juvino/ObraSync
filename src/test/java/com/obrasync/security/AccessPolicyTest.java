package com.obrasync.security;

import com.obrasync.model.*;
import javax.servlet.http.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccessPolicyTest {
    AccessPolicy policy;
    HttpServletRequest request;
    @BeforeEach void preparar() {
        policy = new AccessPolicy(); request = mock(HttpServletRequest.class); policy.setRequest(request);
    }
    Usuario usuario(Perfil perfil) { return new Usuario(2L, "Teste", "teste@exemplo", "hash", perfil); }
    @Test @DisplayName("Sem sessão ou JWT, ações são negadas")
    void anonimo() {
        assertThrows(AcessoNegadoException.class, policy::consultar);
        assertThrows(AcessoNegadoException.class, policy::administrar);
        assertThrows(AcessoNegadoException.class, policy::fiscalizar);
        assertThrows(AcessoNegadoException.class, () -> policy.editar(new Vistoria()));
    }
    @Test @DisplayName("ADMIN pode administrar e fiscalizar via sessão JSF")
    void adminSessao() {
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(usuario(Perfil.ADMIN));
        assertDoesNotThrow(policy::consultar); assertDoesNotThrow(policy::administrar);
        assertDoesNotThrow(policy::fiscalizar); assertTrue(policy.podeEditar(new Vistoria()));
        assertTrue(policy.isPodeCriar()); assertTrue(policy.isPodeAlterarStatus()); assertTrue(policy.isPodeExcluir());
    }
    @Test @DisplayName("Engenheiro edita somente vistorias sob sua responsabilidade")
    void engenheiroProprietario() {
        Usuario engenheiro = usuario(Perfil.ENGENHEIRO);
        when(request.getAttribute(AccessPolicy.JWT_USER)).thenReturn(engenheiro);
        Vistoria vistoria = new Vistoria(); vistoria.setResponsavel(engenheiro);
        assertDoesNotThrow(() -> policy.editar(vistoria));
        vistoria.setResponsavel(new Usuario(3L, "Outro", "outro@exemplo", "hash", Perfil.ENGENHEIRO));
        assertThrows(AcessoNegadoException.class, () -> policy.editar(vistoria));
        assertThrows(AcessoNegadoException.class, policy::administrar);
        assertThrows(AcessoNegadoException.class, policy::fiscalizar);
        assertTrue(policy.isEngenheiro()); assertFalse(policy.isPodeAlterarStatus()); assertFalse(policy.isPodeExcluir());
    }
    @Test @DisplayName("FISCAL consulta e altera status, mas não edita dados ou exclui")
    void fiscal() {
        when(request.getAttribute(AccessPolicy.JWT_USER)).thenReturn(usuario(Perfil.FISCAL));
        assertDoesNotThrow(policy::consultar); assertDoesNotThrow(policy::fiscalizar);
        assertFalse(policy.isPodeCriar()); assertThrows(AcessoNegadoException.class, policy::administrar);
        assertThrows(AcessoNegadoException.class, () -> policy.editar(new Vistoria()));
        assertTrue(policy.isFiscal()); assertTrue(policy.isPodeAlterarStatus()); assertFalse(policy.isPodeExcluir());
    }
}
