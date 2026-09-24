package com.obrasync.service;

import com.obrasync.model.*;
import com.obrasync.security.*;
import javax.persistence.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import java.time.LocalDate;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VistoriaFiltrosTest {
    @Test @DisplayName("Filtros parametrizados são aplicados à página e à contagem no banco")
    @SuppressWarnings("unchecked") void filtros() {
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Vistoria> dados = mock(TypedQuery.class, RETURNS_SELF);
        TypedQuery<Long> total = mock(TypedQuery.class, RETURNS_SELF);
        when(em.createQuery(anyString(), eq(Vistoria.class))).thenReturn(dados);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(total);
        when(dados.getResultList()).thenReturn(Collections.emptyList()); when(total.getSingleResult()).thenReturn(50L);
        VistoriaService service = new VistoriaService(); service.setEntityManager(em);
        LocalDate inicio = LocalDate.of(2026,1,1), fim = inicio.plusMonths(1);
        PaginaResultado<Vistoria> pagina = service.pesquisar(StatusVistoria.PENDENTE, TipoVistoria.ELETRICA,"AURORA","Juliana",inicio,fim,2,20);
        assertEquals(50,pagina.getTotal()); verify(dados).setFirstResult(40); verify(dados).setMaxResults(20);
        for (TypedQuery<?> query : new TypedQuery[]{dados,total}) {
            verify(query).setParameter("status",StatusVistoria.PENDENTE); verify(query).setParameter("tipo",TipoVistoria.ELETRICA);
            verify(query).setParameter("obra","%aurora%"); verify(query).setParameter("responsavel","%juliana%");
            verify(query).setParameter("dataInicial",inicio); verify(query).setParameter("dataFinal",fim);
        }
        ArgumentCaptor<String> jpql = ArgumentCaptor.forClass(String.class);
        verify(em).createQuery(jpql.capture(), eq(Vistoria.class));
        assertTrue(jpql.getValue().contains("JOIN FETCH")); assertFalse(jpql.getValue().contains("AURORA"));
    }
    @Test @DisplayName("Paginação inválida, overflow e período invertido são rejeitados antes da consulta")
    void invalidos() {
        VistoriaService service = new VistoriaService(); EntityManager em = mock(EntityManager.class); service.setEntityManager(em);
        for (int[] page : new int[][]{{-1,20},{0,0},{0,101},{Integer.MAX_VALUE,100}})
            assertThrows(IllegalArgumentException.class, () -> service.pesquisar(null,null,null,null,null,null,page[0],page[1]));
        assertThrows(IllegalArgumentException.class, () -> service.pesquisar(null,null,null,null,LocalDate.now(),LocalDate.now().minusDays(1),0,20));
        verifyNoInteractions(em);
    }
    @Test @DisplayName("Fiscalização altera apenas status e é protegida por política")
    @SuppressWarnings("unchecked") void status() {
        VistoriaService service = new VistoriaService(); EntityManager em = mock(EntityManager.class); AccessPolicy policy = mock(AccessPolicy.class);
        service.setEntityManager(em); service.setAcesso(policy);
        TypedQuery<Vistoria> query = mock(TypedQuery.class,RETURNS_SELF); when(em.createQuery(anyString(),eq(Vistoria.class))).thenReturn(query);
        Vistoria v = new Vistoria(); v.setLocalizacao("Bloco A"); when(query.getSingleResult()).thenReturn(v);
        assertSame(v, service.atualizarStatus(1L,StatusVistoria.APROVADA)); assertEquals(StatusVistoria.APROVADA,v.getStatus());
        assertEquals("Bloco A",v.getLocalizacao()); verify(policy).fiscalizar();
        doThrow(new AcessoNegadoException()).when(policy).fiscalizar();
        assertThrows(AcessoNegadoException.class, () -> service.atualizarStatus(1L,StatusVistoria.REPROVADA));
        assertEquals(StatusVistoria.APROVADA,v.getStatus());
    }
}
