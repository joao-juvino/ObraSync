package com.obrasync.report;

import com.obrasync.model.StatusVistoria;
import com.obrasync.model.TipoVistoria;
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
    @DisplayName("Deve gerar laudo em PDF válido com cabeçalho PDF")
    void deveGerarLaudoPdfValido() throws Exception {
        Vistoria vistoria = new Vistoria();
        vistoria.setId(101L);
        vistoria.setObra("Edifício Metropolis Business");
        vistoria.setResponsavel("Eng. Juliana Castro");
        vistoria.setTipo(TipoVistoria.ESTRUTURAL);
        vistoria.setDataVistoria(LocalDate.now());
        vistoria.setStatus(StatusVistoria.APROVADA);
        vistoria.setLocalizacao("Bloco Central - 10º Andar");
        vistoria.setObservacoes("Estrutura de concreto armado em perfeita conformidade com as normas ABNT NBR 6118.");

        byte[] pdfBytes = relatorioService.gerarLaudoVistoriaPdf(vistoria);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "O PDF gerado não deve ser vazio");

        // Verifica a assinatura mágica de arquivo PDF: "%PDF-"
        String cabecalhoPdf = new String(pdfBytes, 0, Math.min(pdfBytes.length, 5));
        assertEquals("%PDF-", cabecalhoPdf, "O arquivo gerado deve iniciar com a assinatura de arquivo PDF %PDF-");
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando a vistoria for nula")
    void deveLancarExcecaoQuandoVistoriaNula() {
        assertThrows(IllegalArgumentException.class, () -> {
            relatorioService.gerarLaudoVistoriaPdf(null);
        });
    }
}
