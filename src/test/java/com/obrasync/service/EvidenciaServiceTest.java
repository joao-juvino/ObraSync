package com.obrasync.service;

import com.obrasync.model.*;
import com.obrasync.security.*;
import javax.persistence.*;
import javax.transaction.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EvidenciaServiceTest {
    EntityManager em; EvidenciaStorageService storage; AccessPolicy acesso;
    TransactionSynchronizationRegistry transacoes; EvidenciaService service; Vistoria vistoria;
    @BeforeEach void preparar() {
        em = mock(EntityManager.class); storage = mock(EvidenciaStorageService.class);
        acesso = mock(AccessPolicy.class); transacoes = mock(TransactionSynchronizationRegistry.class);
        service = new EvidenciaService(); service.setEntityManager(em); service.setStorage(storage);
        service.setAcesso(acesso); service.setTransacoes(transacoes);
        vistoria = new Vistoria(); vistoria.setId(1L);
    }
    EvidenciaVistoria evidencia() {
        EvidenciaVistoria e = new EvidenciaVistoria(); e.setId(2L); e.setVistoria(vistoria);
        e.setCaminhoOuIdentificador("uuid.png"); return e;
    }
    @Test @DisplayName("Upload persiste metadados e desfaz arquivo se banco fizer rollback")
    void uploadRollback() {
        when(em.find(Vistoria.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(vistoria);
        when(storage.salvar(any(), eq("image/png"))).thenReturn("uuid.png");
        EvidenciaVistoria e = service.salvar(1L, new byte[]{1,2}, "image/png", "../../foto.png", "Inspeção");
        assertEquals(2, e.getTamanho()); assertFalse(e.getNomeArquivo().contains("/"));
        assertSame(vistoria, e.getVistoria()); verify(em).persist(e); verify(em).flush(); verify(acesso).editar(vistoria);
        ArgumentCaptor<Synchronization> sync = ArgumentCaptor.forClass(Synchronization.class);
        verify(transacoes).registerInterposedSynchronization(sync.capture());
        sync.getValue().beforeCompletion(); sync.getValue().afterCompletion(Status.STATUS_ROLLEDBACK);
        verify(storage).excluir("uuid.png");
    }
    @Test @DisplayName("Vistoria inexistente ou sem permissão não grava arquivos")
    void inexistenteSemPermissao() {
        assertEquals(404, assertThrows(EvidenciaException.class, () -> service.salvar(99L,new byte[]{1},"image/png","foto",null)).getStatus());
        when(em.find(Vistoria.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(vistoria);
        doThrow(new AcessoNegadoException()).when(acesso).editar(vistoria);
        assertThrows(AcessoNegadoException.class, () -> service.salvar(1L,new byte[]{1},"image/png","foto",null));
        verifyNoInteractions(storage);
    }
    @Test @DisplayName("Evidência de outra vistoria retorna 404")
    void relacao() {
        when(em.find(EvidenciaVistoria.class, 2L)).thenReturn(evidencia());
        assertEquals(404, assertThrows(EvidenciaException.class, () -> service.buscar(3L, 2L)).getStatus());
        assertEquals(404, assertThrows(EvidenciaException.class, () -> service.buscar(1L, 99L)).getStatus());
    }
    @Test @DisplayName("Exclusão remove metadado e só apaga arquivo definitivamente após commit")
    void exclusao() {
        EvidenciaVistoria e = evidencia(); when(em.find(EvidenciaVistoria.class, 2L)).thenReturn(e);
        service.excluir(1L, 2L);
        verify(storage).mover("uuid.png", "uuid.png.deleted"); verify(em).remove(e);
        ArgumentCaptor<Synchronization> sync = ArgumentCaptor.forClass(Synchronization.class);
        verify(transacoes).registerInterposedSynchronization(sync.capture());
        sync.getValue().afterCompletion(Status.STATUS_ROLLEDBACK);
        verify(storage).mover("uuid.png.deleted", "uuid.png");
        sync.getValue().afterCompletion(Status.STATUS_COMMITTED);
        verify(storage).excluir("uuid.png.deleted");
    }
    @Test @DisplayName("Lista evidências e lê conteúdo somente da vistoria indicada")
    @SuppressWarnings("unchecked") void leitura() {
        when(em.find(Vistoria.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(vistoria);
        TypedQuery<EvidenciaVistoria> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(EvidenciaVistoria.class))).thenReturn(query);
        when(query.setParameter("id", 1L)).thenReturn(query);
        EvidenciaVistoria e = evidencia(); when(query.getResultList()).thenReturn(Collections.singletonList(e));
        when(em.find(EvidenciaVistoria.class, 2L)).thenReturn(e);
        when(storage.ler("uuid.png")).thenReturn(new byte[]{1,2});
        assertEquals(1, service.listar(1L).size()); assertArrayEquals(new byte[]{1,2}, service.conteudo(1L,2L));
        service.removerArquivosDaVistoria(1L); verify(acesso).administrar();
    }
    @Test @DisplayName("Falha imediata de persistência limpa arquivo recém-enviado")
    void falhaPersistencia() {
        when(em.find(Vistoria.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(vistoria);
        when(storage.salvar(any(), any())).thenReturn("uuid.png");
        doThrow(new PersistenceException()).when(em).flush();
        assertThrows(PersistenceException.class, () -> service.salvar(1L,new byte[]{1},"image/png",null,null));
        verify(storage).excluir("uuid.png");
    }
}
