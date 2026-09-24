package com.obrasync.report;

import com.obrasync.model.Vistoria;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

import javax.enterprise.context.ApplicationScoped;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço responsável pela compilação, preenchimento e exportação de relatórios
 * e laudos técnicos em PDF utilizando a biblioteca JasperReports.
 */
@ApplicationScoped
public class RelatorioService implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String CAMINHO_JRXML_PADRAO = "/reports/laudo_vistoria.jrxml";
    private final Map<String, JasperReport> cacheRelatoriosCompilados = new ConcurrentHashMap<>();
    @javax.inject.Inject private com.obrasync.service.EvidenciaService evidencias;
    @javax.inject.Inject private com.obrasync.service.EvidenciaStorageService storage;

    /**
     * Gera o laudo técnico da vistoria em formato binário PDF.
     *
     * @param vistoria Dados da vistoria a ser impressa
     * @return Array de bytes com o documento PDF gerado
     * @throws JRException Em caso de falha na compilação ou geração do relatório
     */
    public byte[] gerarLaudoVistoriaPdf(Vistoria vistoria) throws JRException {
        if (vistoria == null) {
            throw new IllegalArgumentException("A vistoria não pode ser nula para geração do laudo.");
        }

        JasperReport jasperReport = obterRelatorioCompilado(CAMINHO_JRXML_PADRAO);

        // Parâmetros do relatório
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("DATA_EMISSAO", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        parametros.put("SISTEMA", "ObraSync - Sistema de Gestão de Obras");

        // Mapeamento dos campos para JRMapCollectionDataSource
        Map<String, Object> registro = new HashMap<>();
        registro.put("id", vistoria.getId() != null ? vistoria.getId() : 0L);
        registro.put("obra", vistoria.getObra() != null ? vistoria.getObra().getNome() : "Não informada");
        registro.put("responsavel", vistoria.getResponsavel() != null ? vistoria.getResponsavel().getNome() : "Não informado");
        registro.put("localizacao", vistoria.getLocalizacao() != null ? vistoria.getLocalizacao() : "Não especificada");
        registro.put("observacoes", vistoria.getObservacoes() != null ? vistoria.getObservacoes() : "");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        registro.put("dataVistoriaFormatada", vistoria.getDataVistoria() != null ? vistoria.getDataVistoria().format(dtf) : "N/D");
        registro.put("tipoDescricao", vistoria.getTipo() != null ? vistoria.getTipo().getDescricao() : "N/D");
        registro.put("statusDescricao", vistoria.getStatus() != null ? vistoria.getStatus().getDescricao() : "N/D");

        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(Collections.singletonList(registro));

        // Preenchimento e exportação para PDF
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);
        java.util.List<Map<String, ?>> imagens = new java.util.ArrayList<>();
        if (vistoria.getId() != null) {
            for (com.obrasync.model.EvidenciaVistoria evidencia : evidencias.listar(vistoria.getId())) {
                Map<String, Object> linha = new HashMap<>();
                linha.put("nome", evidencia.getNomeArquivo());
                linha.put("descricao", evidencia.getDescricao());
                try {
                    byte[] bytes = storage.ler(evidencia.getCaminhoOuIdentificador());
                    storage.validar(bytes, evidencia.getTipoMime());
                    linha.put("imagem", javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(bytes)));
                } catch (java.io.IOException | RuntimeException e) {
                    linha.put("descricao", "Imagem indisponível ou inválida.");
                }
                imagens.add(linha);
            }
        }
        if (!imagens.isEmpty()) {
            JasperPrint anexo = JasperFillManager.fillReport(obterRelatorioCompilado("/reports/evidencias.jrxml"), parametros,
                    new JRMapCollectionDataSource(imagens));
            for (JRPrintPage pagina : anexo.getPages()) jasperPrint.addPage(pagina);
        }
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    /**
     * Obtém o relatório compilado a partir do cache ou compila a partir do recurso JRXML.
     */
    private JasperReport obterRelatorioCompilado(String caminhoRecurso) throws JRException {
        if (cacheRelatoriosCompilados.containsKey(caminhoRecurso)) {
            return cacheRelatoriosCompilados.get(caminhoRecurso);
        }

        InputStream jrxmlStream = getClass().getResourceAsStream(caminhoRecurso);
        if (jrxmlStream == null) {
            jrxmlStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(caminhoRecurso.startsWith("/") ? caminhoRecurso.substring(1) : caminhoRecurso);
        }

        if (jrxmlStream == null) {
            throw new JRException("Template de relatório não encontrado no classpath: " + caminhoRecurso);
        }

        try (InputStream template = jrxmlStream) {
            JasperReport jasperReport = JasperCompileManager.compileReport(template);
            cacheRelatoriosCompilados.put(caminhoRecurso, jasperReport);
            return jasperReport;
        } catch (java.io.IOException e) { throw new JRException("Falha ao ler template.", e); }
    }
    public void setEvidencias(com.obrasync.service.EvidenciaService evidencias) { this.evidencias = evidencias; }
    public void setStorage(com.obrasync.service.EvidenciaStorageService storage) { this.storage = storage; }
}
