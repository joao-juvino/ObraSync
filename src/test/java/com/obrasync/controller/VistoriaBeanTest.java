package com.obrasync.controller;

import com.obrasync.model.Obra;
import com.obrasync.model.Perfil;
import com.obrasync.model.StatusVistoria;
import com.obrasync.model.TipoVistoria;
import com.obrasync.model.Usuario;
import com.obrasync.model.Vistoria;
import com.obrasync.report.RelatorioService;
import com.obrasync.service.VistoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VistoriaBeanTest {

    private VistoriaBean bean;
    @Mock private VistoriaService service;
    @Mock private RelatorioService relatorioService;

    @BeforeEach
    void setUp() {
        when(service.listarTodas()).thenReturn(Collections.singletonList(vistoria(1L)));

        bean = new VistoriaBean();
        bean.setVistoriaService(service);
        bean.setRelatorioService(relatorioService);
        bean.inicializar();
    }

    @Test
    @DisplayName("Deve inicializar o bean com listagem do serviço JPA e formulário novo")
    void deveInicializarBean() {
        assertEquals(1, bean.getVistorias().size());
        assertNull(bean.getVistoria().getId());
        assertEquals(StatusVistoria.PENDENTE, bean.getVistoria().getStatus());
    }

    @Test
    @DisplayName("Deve preparar edição com cópia independente da vistoria listada")
    void devePrepararEdicaoComCopiaIndependente() {
        Vistoria selecionada = bean.getVistorias().get(0);

        bean.prepararEdicao(selecionada);
        bean.getVistoria().getObra().setNome("Obra alterada no formulário");

        assertEquals(1L, bean.getVistoria().getId());
        assertEquals("Residencial Aurora", selecionada.getObra().getNome());
    }

    @Test
    @DisplayName("Deve salvar e recarregar a listagem")
    void deveSalvarVistoria() {
        bean.prepararEdicao(vistoria(1L));

        bean.salvar();

        verify(service).salvar(any(Vistoria.class));
        verify(service, times(2)).listarTodas();
        assertNull(bean.getVistoria().getId());
    }

    @Test
    @DisplayName("Deve excluir vistoria persistida e recarregar a listagem")
    void deveExcluirVistoria() {
        bean.excluir(vistoria(1L));

        verify(service).excluir(1L);
        verify(service, times(2)).listarTodas();
    }

    @Test
    @DisplayName("Deve expor métricas e enums do dashboard")
    void deveExporMetricasEEnums() {
        when(service.contarTotal()).thenReturn(1L);
        when(service.contarAprovadas()).thenReturn(1L);

        assertEquals(1L, bean.getTotalVistorias());
        assertEquals(1L, bean.getAprovadasCount());
        assertEquals(StatusVistoria.values().length, bean.getStatusList().length);
        assertEquals(TipoVistoria.values().length, bean.getTiposList().length);
    }

    private Vistoria vistoria(Long id) {
        Obra obra = new Obra(1L, "Residencial Aurora", "Rua A", LocalDate.now(), LocalDate.now().plusDays(1));
        Usuario usuario = new Usuario(2L, "Eng. Juliana", "juliana@obrasync.com", "hash", Perfil.ENGENHEIRO);
        return new Vistoria(id, obra, usuario, TipoVistoria.ESTRUTURAL, LocalDate.now(),
                StatusVistoria.APROVADA, "Bloco A", "Conforme");
    }
}
