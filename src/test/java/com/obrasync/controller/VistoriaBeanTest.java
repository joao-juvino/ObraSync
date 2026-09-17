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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VistoriaBeanTest {

    private VistoriaBean bean;
    private VistoriaService service;
    private RelatorioService relatorioService;

    // Helpers reutilizados nos testes
    private Obra obraFake() {
        Obra o = new Obra();
        o.setId(1L);
        o.setNome("Obra Teste");
        o.setEndereco("Rua Teste, 100");
        o.setDataInicio(LocalDate.now().minusMonths(6));
        o.setDataPrevisaoFim(LocalDate.now().plusMonths(6));
        return o;
    }

    private Usuario usuarioFake() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("Eng. Teste");
        u.setEmail("engenheiro@obrasync.com");
        u.setSenha("$2a$10$hashqualquer");
        u.setPerfil(Perfil.ENGENHEIRO);
        return u;
    }

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
    @DisplayName("Deve inicializar o bean com listagem e formulario novo")
    void deveInicializarBean() {
        assertNotNull(bean.getVistorias());
        assertFalse(bean.getVistorias().isEmpty());
        assertNotNull(bean.getVistoria());
        assertNull(bean.getVistoria().getId());
        assertEquals(StatusVistoria.PENDENTE, bean.getVistoria().getStatus());
    }

    @Test
    @DisplayName("Deve preparar novo cadastro e preparar edicao preservando dados")
    void devePrepararNovoEEdicao() {
        bean.prepararNovo();
        assertNull(bean.getVistoria().getId());

        List<Vistoria> lista = bean.getVistorias();
        Vistoria selecionada = lista.get(0);

        bean.prepararEdicao(selecionada);
        assertEquals(selecionada.getId(), bean.getVistoria().getId());
        assertEquals(selecionada.getObra(), bean.getVistoria().getObra());

        // Altera o clone no bean sem alterar imediatamente o item da lista
        Obra obraModificada = obraFake();
        obraModificada.setNome("Obra Modificada no Modal");
        bean.getVistoria().setObra(obraModificada);
        assertNotEquals(bean.getVistoria().getObra(), selecionada.getObra());
    }

    @Test
    @DisplayName("Deve salvar vistoria e atualizar listagem")
    void deveSalvarVistoria() {
        int totalAntes = bean.getVistorias().size();

        bean.prepararNovo();
        bean.getVistoria().setObra(obraFake());
        bean.getVistoria().setResponsavel(usuarioFake());
        bean.getVistoria().setTipo(TipoVistoria.ACABAMENTO);
        bean.getVistoria().setStatus(StatusVistoria.APROVADA);
        bean.getVistoria().setDataVistoria(LocalDate.now());

        bean.salvar();

        assertEquals(totalAntes + 1, bean.getVistorias().size());
        assertNull(bean.getVistoria().getId(), "Apos salvar deve resetar para nova vistoria");
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
    @DisplayName("Deve expor metricas e listas de enums")
    void deveExporMetricasEEnums() {
        assertTrue(bean.getTotalVistorias() > 0);
        assertTrue(bean.getAprovadasCount() > 0);
        assertTrue(bean.getPendentesCount() > 0);
        assertEquals(StatusVistoria.values().length, bean.getStatusList().length);
        assertEquals(TipoVistoria.values().length, bean.getTiposList().length);
    }

    @Test
    @DisplayName("Deve executar metodo baixarLaudoPdf de forma segura quando fora do container web")
    void deveExecutarBaixarLaudoPdf() {
        Vistoria vistoria = bean.getVistorias().get(0);
        assertDoesNotThrow(() -> bean.baixarLaudoPdf(vistoria));
        assertDoesNotThrow(() -> bean.baixarLaudoPdf(null));
    }
}