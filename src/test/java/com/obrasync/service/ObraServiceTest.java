package com.obrasync.service;

import com.obrasync.model.Obra;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObraServiceTest {

    private ObraService obraService;

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Obra> query;

    @BeforeEach
    void setUp() {
        obraService = new ObraService();
        obraService.setEntityManager(em);
        obraService.setAcesso(mock(com.obrasync.security.AccessPolicy.class));
    }

    @Test
    @DisplayName("Deve persistir nova obra quando ID for nulo")
    void devePersistirNovaObra() {
        Obra obra = new Obra();
        obra.setNome("Residencial Bella Vista");
        obra.setEndereco("Av. Central, 500");
        obra.setDataInicio(LocalDate.now());

        Obra resultado = obraService.salvar(obra);

        verify(em, times(1)).persist(obra);
        assertNotNull(resultado);
        assertEquals("Residencial Bella Vista", resultado.getNome());
    }

    @Test
    @DisplayName("Deve atualizar obra existente quando ID estiver presente")
    void deveAtualizarObraExistente() {
        Obra obra = new Obra();
        obra.setId(10L);
        obra.setNome("Residencial Bella Vista Atualizado");

        when(em.merge(any(Obra.class))).thenReturn(obra);

        Obra resultado = obraService.salvar(obra);

        verify(em, times(1)).merge(obra);
        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
    }

    @Test
    @DisplayName("Deve buscar obra por ID")
    void deveBuscarPorId() {
        Obra obra = new Obra(5L, "Torre Imperial", "Rua das Flores, 120", LocalDate.now(), LocalDate.now().plusMonths(12));
        when(em.find(eq(Obra.class), eq(5L))).thenReturn(obra);

        Obra resultado = obraService.buscarPorId(5L);

        assertNotNull(resultado);
        assertEquals("Torre Imperial", resultado.getNome());
    }

    @Test
    @DisplayName("Deve listar todas as obras ordenadas por nome")
    void deveListarTodasAsObras() {
        when(em.createQuery(anyString(), eq(Obra.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(new Obra("Obra A")));

        List<Obra> resultado = obraService.listarTodas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Obra A", resultado.get(0).getNome());
    }

    @Test
    @DisplayName("Deve excluir obra quando encontrada")
    void deveExcluirObra() {
        Obra obra = new Obra(1L, "Obra Teste", null, null, null);
        when(em.find(eq(Obra.class), eq(1L))).thenReturn(obra);
        when(em.createQuery(anyString(), eq(Obra.class))).thenReturn(query);
        when(query.setParameter("id", 1L)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(obra);

        obraService.excluir(1L);

        verify(em, times(1)).remove(obra);
    }
}
