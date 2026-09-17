package com.obrasync.service;

import com.obrasync.model.StatusVistoria;
import com.obrasync.model.TipoVistoria;
import com.obrasync.model.Vistoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VistoriaServiceTest {

    private VistoriaService vistoriaService;

    @BeforeEach
    void setUp() {
        vistoriaService = new VistoriaService();
        vistoriaService.inicializarDadosIniciais();
    }

    @Test
    @DisplayName("Deve inicializar com vistorias de exemplo")
    void deveInicializarComDadosDeExemplo() {
        List<Vistoria> vistorias = vistoriaService.listarTodas();
        assertNotNull(vistorias);
        assertTrue(vistorias.size() >= 6, "Deveria ter carregado ao menos 6 vistorias de exemplo");
    }

    @Test
    @DisplayName("Deve salvar uma nova vistoria com sucesso")
    void deveSalvarNovaVistoria() {
        Vistoria nova = new Vistoria();
        nova.setObra("Residencial Aurora");
        nova.setResponsavel("Eng. Juliana Castro");
        nova.setTipo(TipoVistoria.ELETRICA);
        nova.setDataVistoria(LocalDate.now());
        nova.setStatus(StatusVistoria.APROVADA);
        nova.setLocalizacao("Bloco C - Subsolo");
        nova.setObservacoes("Subestação abrigada em conformidade.");

        Vistoria salva = vistoriaService.salvar(nova);

        assertNotNull(salva.getId());
        assertEquals("Residencial Aurora", salva.getObra());
        assertEquals(StatusVistoria.APROVADA, salva.getStatus());

        Vistoria encontrada = vistoriaService.buscarPorId(salva.getId());
        assertNotNull(encontrada);
        assertEquals("Residencial Aurora", encontrada.getObra());
    }

    @Test
    @DisplayName("Deve contabilizar corretamente status e taxas do dashboard")
    void deveContabilizarMetricas() {
        long total = vistoriaService.contarTotal();
        long aprovadas = vistoriaService.contarAprovadas();
        long pendentes = vistoriaService.contarPendentes();
        long reprovadas = vistoriaService.contarReprovadas();

        assertTrue(total > 0);
        assertTrue(aprovadas > 0);
        assertTrue(pendentes > 0);
        assertTrue(reprovadas > 0);

        double taxa = vistoriaService.calcularTaxaAprovacao();
        assertTrue(taxa > 0.0 && taxa <= 100.0);
    }

    @Test
    @DisplayName("Deve excluir vistoria com sucesso")
    void deveExcluirVistoria() {
        List<Vistoria> iniciais = vistoriaService.listarTodas();
        Vistoria paraExcluir = iniciais.get(0);
        Long id = paraExcluir.getId();

        vistoriaService.excluir(id);

        assertNull(vistoriaService.buscarPorId(id));
    }
}
