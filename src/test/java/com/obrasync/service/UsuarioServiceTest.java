package com.obrasync.service;

import com.obrasync.model.Perfil;
import com.obrasync.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private UsuarioService usuarioService;

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Usuario> query;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService();
        usuarioService.setEntityManager(em);
    }

    @Test
    @DisplayName("Deve persistir novo usuário quando ID for nulo")
    void devePersistirNovoUsuario() {
        Usuario usuario = new Usuario("Carlos Silva", "carlos@obrasync.com", "$2a$12$eXampleHashBcrypt", Perfil.ENGENHEIRO);

        Usuario resultado = usuarioService.salvar(usuario);

        verify(em, times(1)).persist(usuario);
        assertNotNull(resultado);
        assertEquals("carlos@obrasync.com", resultado.getEmail());
    }

    @Test
    @DisplayName("Deve atualizar usuário existente quando ID estiver presente")
    void deveAtualizarUsuarioExistente() {
        Usuario usuario = new Usuario(20L, "Carlos Silva", "carlos@obrasync.com", "$2a$12$eXampleHashBcrypt", Perfil.ADMIN);
        when(em.merge(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.salvar(usuario);

        verify(em, times(1)).merge(usuario);
        assertNotNull(resultado);
        assertEquals(20L, resultado.getId());
    }

    @Test
    @DisplayName("Deve buscar usuário por email")
    void deveBuscarPorEmail() {
        Usuario usuario = new Usuario(1L, "Mariana Ramos", "mariana@obrasync.com", "hash", Perfil.ENGENHEIRO);

        when(em.createQuery(anyString(), eq(Usuario.class))).thenReturn(query);
        when(query.setParameter(eq("email"), eq("mariana@obrasync.com"))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(usuario);

        Usuario resultado = usuarioService.buscarPorEmail("mariana@obrasync.com");

        assertNotNull(resultado);
        assertEquals("Mariana Ramos", resultado.getNome());
    }

    @Test
    @DisplayName("Deve retornar null quando busca por email não encontrar resultado")
    void deveRetornarNullSeEmailNaoEncontrado() {
        when(em.createQuery(anyString(), eq(Usuario.class))).thenReturn(query);
        when(query.setParameter(eq("email"), eq("inexistente@obrasync.com"))).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());

        Usuario resultado = usuarioService.buscarPorEmail("inexistente@obrasync.com");

        assertNull(resultado);
    }

    @Test
    @DisplayName("Deve listar usuários filtrando por perfil")
    void deveListarPorPerfil() {
        when(em.createQuery(anyString(), eq(Usuario.class))).thenReturn(query);
        when(query.setParameter(eq("perfil"), eq(Perfil.ENGENHEIRO))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(new Usuario("Eng. Teste")));

        List<Usuario> engenheiros = usuarioService.listarPorPerfil(Perfil.ENGENHEIRO);

        assertNotNull(engenheiros);
        assertEquals(1, engenheiros.size());
        assertEquals("Eng. Teste", engenheiros.get(0).getNome());
    }

    @Test
    @DisplayName("Deve excluir usuário existente")
    void deveExcluirUsuario() {
        Usuario usuario = new Usuario(1L, "User Test", "user@test.com", "pass", Perfil.ENGENHEIRO);
        when(em.find(eq(Usuario.class), eq(1L))).thenReturn(usuario);

        usuarioService.excluir(1L);

        verify(em, times(1)).remove(usuario);
    }
}
