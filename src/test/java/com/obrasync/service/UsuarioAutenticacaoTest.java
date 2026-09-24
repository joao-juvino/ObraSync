package com.obrasync.service;
import com.obrasync.model.*;
import com.obrasync.security.*;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import javax.persistence.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class UsuarioAutenticacaoTest {
    @Test @DisplayName("Cadastro gera BCrypt e login normaliza e-mail sem aceitar senha incorreta")
    @SuppressWarnings("unchecked") void bcrypt() {
        EntityManager em = mock(EntityManager.class); UsuarioService service = new UsuarioService(); service.setEntityManager(em);
        service.setAcesso(mock(AccessPolicy.class));
        Usuario u = new Usuario("Demo"," TESTE@EXEMPLO.COM ","Senha-demo-123",Perfil.ADMIN);
        service.salvar(u); assertNotEquals("Senha-demo-123",u.getSenha()); assertTrue(BCrypt.checkpw("Senha-demo-123",u.getSenha()));
        TypedQuery<Usuario> query = mock(TypedQuery.class,RETURNS_SELF);
        when(em.createQuery(anyString(),eq(Usuario.class))).thenReturn(query); when(query.getSingleResult()).thenReturn(u);
        assertSame(u, service.autenticar(" TESTE@EXEMPLO.COM ","Senha-demo-123")); verify(query).setParameter("email","teste@exemplo.com");
        assertNull(service.autenticar("teste@exemplo.com","errada")); assertNull(service.autenticar(null,"x"));
        u.setSenha("inválida"); assertNull(service.autenticar("teste@exemplo.com","x"));
        assertFalse(u.toString().contains("inválida"));
    }
    @Test @DisplayName("Hash demonstrativo da migration V7 corresponde à senha local documentada")
    void seed() throws Exception {
        String sql = java.nio.file.Files.readString(java.nio.file.Paths.get("src/main/resources/db/migration/V7__fix_demo_user_passwords.sql"));
        String hash = sql.substring(sql.indexOf("$2a$"),sql.indexOf("$2a$")+60);
        assertTrue(BCrypt.checkpw("admin123",hash));
    }
}
