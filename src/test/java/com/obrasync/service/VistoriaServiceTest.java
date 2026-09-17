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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VistoriaServiceTest {

    private VistoriaService vistoriaService;

    private Obra obraFake(String nome) {
        Obra o = new Obra();
        o.setNome(nome);
        o.setEndereco("Av. Paulista, 1000");
        o.setDataInicio(LocalDate.now().minusMonths(4));
        o.setDataPrevisaoFim(LocalDate.now().plusMonths(8));
        return o;
    }

    private Usuario usuarioFake(String nome) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(nome.toLowerCase().replace(" ", ".") + "@obrasync.com");
        u.setSenha("$2a$10$hashqualquer");
        u.setPerfil(Perfil.ENGENHEIRO);
        return u;
    }

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
        nova.setObra(obraFake("Residencial Aurora"));
        nova.setResponsavel(usuarioFake("Eng. Juliana Castro"));
        nova.setTipo(TipoVistoria.ELETRICA);
        nova.setDataVistoria(LocalDate.now());
        nova.setStatus(StatusVistoria.APROVADA);
        nova.setLocalizacao("Bloco C - Subsolo");
        nova.setObservacoes("Subestacao abrigada em conformidade.");

        Vistoria salva = vistoriaService.salvar(nova);

        assertNotNull(salva.getId());
        assertEquals("Residencial Aurora", salva.getNomeObra());
        assertEquals(StatusVistoria.APROVADA, salva.getStatus());

        Vistoria encontrada = vistoriaService.buscarPorId(salva.getId());
        assertNotNull(encontrada);
        assertEquals("Residencial Aurora", encontrada.getNomeObra());
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