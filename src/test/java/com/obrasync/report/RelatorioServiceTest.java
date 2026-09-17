package com.obrasync.report;

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

import static org.junit.jupiter.api.Assertions.*;

class RelatorioServiceTest {

    private RelatorioService relatorioService;

    @BeforeEach
    void setUp() {
        relatorioService = new RelatorioService();
    }

    @Test
    @DisplayName("Deve gerar laudo em PDF valido com cabecalho PDF")
    void deveGerarLaudoPdfValido() throws Exception {
        Obra obra = new Obra();
        obra.setNome("Edificio Metropolis Business");
        obra.setEndereco("Av. Paulista, 500");
        obra.setDataInicio(LocalDate.now().minusMonths(12));
        obra.setDataPrevisaoFim(LocalDate.now().plusMonths(12));

        Usuario responsavel = new Usuario();
        responsavel.setNome("Eng. Juliana Castro");
        responsavel.setEmail("juliana.castro@obrasync.com");
        responsavel.setSenha("$2a$10$hashqualquer");
        responsavel.setPerfil(Perfil.ENGENHEIRO);

        Vistoria vistoria = new Vistoria();
        vistoria.setId(101L);
        vistoria.setObra(obra);
        vistoria.setResponsavel(responsavel);
        vistoria.setTipo(TipoVistoria.ESTRUTURAL);
        vistoria.setDataVistoria(LocalDate.now());
        vistoria.setStatus(StatusVistoria.APROVADA);
        vistoria.setLocalizacao("Bloco Central - 10 Andar");
        vistoria.setObservacoes("Estrutura de concreto armado em perfeita conformidade com as normas ABNT NBR 6118.");

        byte[] pdfBytes = relatorioService.gerarLaudoVistoriaPdf(vistoria);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "O PDF gerado nao deve ser vazio");

        // Verifica a assinatura magica de arquivo PDF: "%PDF-"
        String cabecalhoPdf = new String(pdfBytes, 0, Math.min(pdfBytes.length, 5));
        assertEquals("%PDF-", cabecalhoPdf, "O arquivo gerado deve iniciar com a assinatura de arquivo PDF %PDF-");
    }

    @Test
    @DisplayName("Deve lancar IllegalArgumentException quando a vistoria for nula")
    void deveLancarExcecaoQuandoVistoriaNula() {
        assertThrows(IllegalArgumentException.class, () -> {
            relatorioService.gerarLaudoVistoriaPdf(null);
        });
    }
}