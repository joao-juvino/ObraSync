package com.obrasync.service;

import com.obrasync.model.Obra;
import com.obrasync.model.Perfil;
import com.obrasync.model.StatusVistoria;
import com.obrasync.model.TipoVistoria;
import com.obrasync.model.Usuario;
import com.obrasync.model.Vistoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VistoriaServiceTest {

    private VistoriaService vistoriaService;

    @Mock private EntityManager em;
    @Mock private TypedQuery<Vistoria> vistoriaQuery;
    @Mock private TypedQuery<Long> contagemQuery;

    @BeforeEach
    void setUp() {
        vistoriaService = new VistoriaService();
        vistoriaService.setEntityManager(em);
    }

    @Test
    @DisplayName("Deve listar vistorias carregando obra e responsável na mesma consulta")
    void deveListarVistoriasComRelacionamentos() {
        Vistoria vistoria = vistoriaPersistida(10L);
        when(em.createQuery(contains("JOIN FETCH v.obra"), eq(Vistoria.class))).thenReturn(vistoriaQuery);
        when(vistoriaQuery.getResultList()).thenReturn(Collections.singletonList(vistoria));

        List<Vistoria> resultado = vistoriaService.listarTodas();

        assertEquals(Collections.singletonList(vistoria), resultado);
        verify(em).createQuery(contains("JOIN FETCH v.responsavel"), eq(Vistoria.class));
    }

    @Test
    @DisplayName("Deve buscar vistoria por ID carregando os relacionamentos")
    void deveBuscarPorIdComRelacionamentos() {
        Vistoria vistoria = vistoriaPersistida(10L);
        when(em.createQuery(contains("WHERE v.id = :id"), eq(Vistoria.class))).thenReturn(vistoriaQuery);
        when(vistoriaQuery.setParameter("id", 10L)).thenReturn(vistoriaQuery);
        when(vistoriaQuery.getSingleResult()).thenReturn(vistoria);

        assertSame(vistoria, vistoriaService.buscarPorId(10L));
        verify(vistoriaQuery).setParameter("id", 10L);
    }

    @Test
    @DisplayName("Deve retornar null ao buscar ID inexistente")
    void deveRetornarNullParaIdInexistente() {
        when(em.createQuery(anyString(), eq(Vistoria.class))).thenReturn(vistoriaQuery);
        when(vistoriaQuery.setParameter("id", 99L)).thenReturn(vistoriaQuery);
        when(vistoriaQuery.getSingleResult()).thenThrow(new NoResultException());

        assertNull(vistoriaService.buscarPorId(99L));
    }

    @Test
    @DisplayName("Deve persistir nova vistoria usando referências gerenciadas de obra e responsável")
    void devePersistirNovaVistoriaComRelacionamentosGerenciados() {
        Vistoria nova = vistoriaPersistida(null);
        Obra obraGerenciada = new Obra();
        Usuario usuarioGerenciado = new Usuario();
        when(em.getReference(Obra.class, 1L)).thenReturn(obraGerenciada);
        when(em.getReference(Usuario.class, 2L)).thenReturn(usuarioGerenciado);

        Vistoria resultado = vistoriaService.salvar(nova);

        assertSame(nova, resultado);
        assertSame(obraGerenciada, nova.getObra());
        assertSame(usuarioGerenciado, nova.getResponsavel());
        verify(em).persist(nova);
    }

    @Test
    @DisplayName("Deve atualizar vistoria existente por merge")
    void deveAtualizarVistoriaExistente() {
        Vistoria existente = vistoriaPersistida(10L);
        Vistoria gerenciada = vistoriaPersistida(10L);
        when(em.getReference(Obra.class, 1L)).thenReturn(new Obra());
        when(em.getReference(Usuario.class, 2L)).thenReturn(new Usuario());
        when(em.merge(existente)).thenReturn(gerenciada);

        assertSame(gerenciada, vistoriaService.salvar(existente));
        verify(em).merge(existente);
    }

    @Test
    @DisplayName("Deve rejeitar vistoria sem relacionamentos persistidos")
    void deveRejeitarVistoriaSemRelacionamentosPersistidos() {
        Vistoria invalida = new Vistoria();
        invalida.setObra(new Obra("Obra sem ID"));
        invalida.setResponsavel(new Usuario("Responsável sem ID"));

        assertThrows(IllegalArgumentException.class, () -> vistoriaService.salvar(invalida));
        verifyNoInteractions(em);
    }

    @Test
    @DisplayName("Deve excluir somente a vistoria encontrada")
    void deveExcluirVistoriaEncontrada() {
        Vistoria vistoria = vistoriaPersistida(10L);
        when(em.find(Vistoria.class, 10L)).thenReturn(vistoria);

        vistoriaService.excluir(10L);

        verify(em).remove(vistoria);
    }

    @Test
    @DisplayName("Deve calcular métricas por consultas agregadas")
    void deveCalcularMetricasPorConsultasAgregadas() {
        when(em.createQuery(contains("COUNT(v)"), eq(Long.class))).thenReturn(contagemQuery);
        when(contagemQuery.getSingleResult()).thenReturn(8L);
        when(contagemQuery.setParameter(eq("status"), any(StatusVistoria.class))).thenReturn(contagemQuery);

        assertEquals(8L, vistoriaService.contarTotal());
        assertEquals(8L, vistoriaService.contarAprovadas());
        assertEquals(100.0, vistoriaService.calcularTaxaAprovacao());
        verify(contagemQuery, atLeastOnce()).setParameter("status", StatusVistoria.APROVADA);
    }

    private Vistoria vistoriaPersistida(Long id) {
        Obra obra = new Obra(1L, "Residencial Aurora", "Rua A", LocalDate.now(), LocalDate.now().plusDays(1));
        Usuario usuario = new Usuario(2L, "Eng. Juliana", "juliana@obrasync.com", "hash", Perfil.ENGENHEIRO);
        return new Vistoria(id, obra, usuario, TipoVistoria.ELETRICA, LocalDate.now(),
                StatusVistoria.APROVADA, "Bloco A", "Conforme");
    }
}
