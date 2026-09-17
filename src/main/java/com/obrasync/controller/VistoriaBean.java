package com.obrasync.controller;

import com.obrasync.model.StatusVistoria;
import com.obrasync.model.TipoVistoria;
import com.obrasync.model.Vistoria;
import com.obrasync.service.VistoriaService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

/**
 * Controller CDI responsável pelo gerenciamento da tela de Vistorias.
 * Utiliza @ViewScoped do JSF 2.2+ (javax.faces.view.ViewScoped) para manter o estado
 * durante as operações assíncronas com PrimeFaces.
 */
@Named("vistoriaBean")
@ViewScoped
public class VistoriaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private VistoriaService vistoriaService;

    private Vistoria vistoria;
    private List<Vistoria> vistorias;
    private List<Vistoria> vistoriasFiltradas;

    @PostConstruct
    public void inicializar() {
        carregarVistorias();
        prepararNovo();
    }

    /**
     * Carrega a lista completa de vistorias a partir do serviço.
     */
    public void carregarVistorias() {
        this.vistorias = vistoriaService.listarTodas();
    }

    /**
     * Prepara uma nova instância limpa de Vistoria para abertura do modal de cadastro.
     */
    public void prepararNovo() {
        this.vistoria = new Vistoria();
    }

    /**
     * Prepara uma vistoria existente para edição no modal, clonando os dados
     * para preservar o estado da tabela até confirmação do salvamento.
     *
     * @param vistoriaSelecionada Vistoria a ser editada
     */
    public void prepararEdicao(Vistoria vistoriaSelecionada) {
        if (vistoriaSelecionada != null) {
            this.vistoria = vistoriaSelecionada.clone();
        }
    }

    /**
     * Salva ou atualiza a vistoria atual, recarrega a listagem e emite mensagem via FacesContext.
     */
    public void salvar() {
        try {
            boolean isNovo = (this.vistoria.getId() == null);
            vistoriaService.salvar(this.vistoria);

            carregarVistorias();

            String resumo = isNovo ? "Vistoria Cadastrada" : "Vistoria Atualizada";
            String detalhe = String.format("Vistoria da obra '%s' registrada com status '%s'.",
                    this.vistoria.getObra(), this.vistoria.getStatus().getDescricao());

            adicionarMensagem(FacesMessage.SEVERITY_INFO, resumo, detalhe);

            prepararNovo();
        } catch (Exception e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro ao Salvar",
                    "Ocorreu um erro ao salvar a vistoria: " + e.getMessage());
        }
    }

    /**
     * Exclui uma vistoria registrada e envia mensagem de confirmação.
     *
     * @param vistoriaParaExcluir Vistoria a ser removida
     */
    public void excluir(Vistoria vistoriaParaExcluir) {
        if (vistoriaParaExcluir != null && vistoriaParaExcluir.getId() != null) {
            vistoriaService.excluir(vistoriaParaExcluir.getId());
            carregarVistorias();

            adicionarMensagem(FacesMessage.SEVERITY_WARN, "Vistoria Excluída",
                    "Vistoria #" + vistoriaParaExcluir.getId() + " da obra '" + vistoriaParaExcluir.getObra() + "' foi removida com sucesso.");
        }
    }

    /**
     * Publica mensagem no FacesContext se o contexto JSF estiver disponível no container.
     */
    protected void adicionarMensagem(FacesMessage.Severity severidade, String resumo, String detalhe) {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                context.addMessage(null, new FacesMessage(severidade, resumo, detalhe));
            }
        } catch (Throwable t) {
            // Permite execução de testes unitários isolados fora do container JSF
        }
    }

    public void setVistoriaService(VistoriaService vistoriaService) {
        this.vistoriaService = vistoriaService;
    }

    // Métodos para obtenção de métricas de Dashboard
    public long getTotalVistorias() {
        return vistoriaService.contarTotal();
    }

    public long getAprovadasCount() {
        return vistoriaService.contarAprovadas();
    }

    public long getPendentesCount() {
        return vistoriaService.contarPendentes();
    }

    public long getReprovadasCount() {
        return vistoriaService.contarReprovadas();
    }

    public double getTaxaAprovacao() {
        return vistoriaService.calcularTaxaAprovacao();
    }

    // Métodos utilitários para SelectOneMenu e filtros
    public StatusVistoria[] getStatusList() {
        return StatusVistoria.values();
    }

    public TipoVistoria[] getTiposList() {
        return TipoVistoria.values();
    }

    // Getters e Setters
    public Vistoria getVistoria() {
        return vistoria;
    }

    public void setVistoria(Vistoria vistoria) {
        this.vistoria = vistoria;
    }

    public List<Vistoria> getVistorias() {
        return vistorias;
    }

    public void setVistorias(List<Vistoria> vistorias) {
        this.vistorias = vistorias;
    }

    public List<Vistoria> getVistoriasFiltradas() {
        return vistoriasFiltradas;
    }

    public void setVistoriasFiltradas(List<Vistoria> vistoriasFiltradas) {
        this.vistoriasFiltradas = vistoriasFiltradas;
    }
}
