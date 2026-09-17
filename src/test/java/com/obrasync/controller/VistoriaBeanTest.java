package com.obrasync.controller;

import com.obrasync.model.StatusVistoria;
import com.obrasync.model.TipoVistoria;
import com.obrasync.model.Vistoria;
import com.obrasync.service.VistoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import com.obrasync.report.RelatorioService;

class VistoriaBeanTest {

    private VistoriaBean bean;
    private VistoriaService service;
    private RelatorioService relatorioService;

    @BeforeEach
    void setUp() {
        service = new VistoriaService();
        service.inicializarDadosIniciais();

        relatorioService = new RelatorioService();

        bean = new VistoriaBean();
        bean.setVistoriaService(service);
        bean.setRelatorioService(relatorioService);
        bean.inicializar();
    }

    @Test
    @DisplayName("Deve inicializar o bean com listagem e formulário novo")
    void deveInicializarBean() {
        assertNotNull(bean.getVistorias());
        assertFalse(bean.getVistorias().isEmpty());
        assertNotNull(bean.getVistoria());
        assertNull(bean.getVistoria().getId());
        assertEquals(StatusVistoria.PENDENTE, bean.getVistoria().getStatus());
    }

    @Test
    @DisplayName("Deve preparar novo cadastro e preparar edição preservando dados")
    void devePrepararNovoEEdicao() {
        bean.prepararNovo();
        assertNull(bean.getVistoria().getId());

        List<Vistoria> lista = bean.getVistorias();
        Vistoria selecionada = lista.get(0);

        bean.prepararEdicao(selecionada);
        assertEquals(selecionada.getId(), bean.getVistoria().getId());
        assertEquals(selecionada.getObra(), bean.getVistoria().getObra());

        // Altera o clone no bean sem alterar imediatamente o item da lista
        bean.getVistoria().setObra("Obra Modificada no Modal");
        assertNotEquals(bean.getVistoria().getObra(), selecionada.getObra());
    }

    @Test
    @DisplayName("Deve salvar vistoria e atualizar listagem")
    void deveSalvarVistoria() {
        int totalAntes = bean.getVistorias().size();

        bean.prepararNovo();
        bean.getVistoria().setObra("Condomínio Esmeralda");
        bean.getVistoria().setResponsavel("Eng. Patrícia Lima");
        bean.getVistoria().setTipo(TipoVistoria.ACABAMENTO);
        bean.getVistoria().setStatus(StatusVistoria.APROVADA);
        bean.getVistoria().setDataVistoria(LocalDate.now());

        bean.salvar();

        assertEquals(totalAntes + 1, bean.getVistorias().size());
        assertNull(bean.getVistoria().getId(), "Após salvar deve resetar para nova vistoria");
    }

    @Test
    @DisplayName("Deve excluir vistoria e atualizar listagem")
    void deveExcluirVistoria() {
        int totalAntes = bean.getVistorias().size();
        Vistoria paraExcluir = bean.getVistorias().get(0);

        bean.excluir(paraExcluir);

        assertEquals(totalAntes - 1, bean.getVistorias().size());
    }

    @Test
    @DisplayName("Deve expor métricas e listas de enums")
    void deveExporMetricasEEnums() {
        assertTrue(bean.getTotalVistorias() > 0);
        assertTrue(bean.getAprovadasCount() > 0);
        assertTrue(bean.getPendentesCount() > 0);
        assertEquals(StatusVistoria.values().length, bean.getStatusList().length);
        assertEquals(TipoVistoria.values().length, bean.getTiposList().length);
    }

    @Test
    @DisplayName("Deve executar método baixarLaudoPdf de forma segura quando fora do container web")
    void deveExecutarBaixarLaudoPdf() {
        Vistoria vistoria = bean.getVistorias().get(0);
        assertDoesNotThrow(() -> bean.baixarLaudoPdf(vistoria));
        assertDoesNotThrow(() -> bean.baixarLaudoPdf(null));
    }
}
