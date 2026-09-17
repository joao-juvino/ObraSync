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
        registro.put("obra", vistoria.getObra() != null ? vistoria.getObra() : "Não informada");
        registro.put("responsavel", vistoria.getResponsavel() != null ? vistoria.getResponsavel() : "Não informado");
        registro.put("localizacao", vistoria.getLocalizacao() != null ? vistoria.getLocalizacao() : "Não especificada");
        registro.put("observacoes", vistoria.getObservacoes() != null ? vistoria.getObservacoes() : "");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        registro.put("dataVistoriaFormatada", vistoria.getDataVistoria() != null ? vistoria.getDataVistoria().format(dtf) : "N/D");
        registro.put("tipoDescricao", vistoria.getTipo() != null ? vistoria.getTipo().getDescricao() : "N/D");
        registro.put("statusDescricao", vistoria.getStatus() != null ? vistoria.getStatus().getDescricao() : "N/D");

        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(Collections.singletonList(registro));

        // Preenchimento e exportação para PDF
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);
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

        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
        cacheRelatoriosCompilados.put(caminhoRecurso, jasperReport);
        return jasperReport;
    }
}
