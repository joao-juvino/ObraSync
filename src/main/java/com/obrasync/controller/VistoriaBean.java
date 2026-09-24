package com.obrasync.controller;

import com.obrasync.model.StatusVistoria;
import com.obrasync.model.TipoVistoria;
import com.obrasync.model.Vistoria;
import com.obrasync.model.Obra;
import com.obrasync.model.Usuario;
import com.obrasync.model.Perfil;
import com.obrasync.service.VistoriaService;
import com.obrasync.service.ObraService;
import com.obrasync.service.UsuarioService;
import com.obrasync.security.AccessPolicy;

import com.obrasync.report.RelatorioService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primefaces.PrimeFaces;

/**
 * Controller CDI responsável pelo gerenciamento da tela de Vistorias.
 * Utiliza @ViewScoped do JSF 2.2+ (javax.faces.view.ViewScoped) para manter o estado
 * durante as operações assíncronas com PrimeFaces.
 */
@Named("vistoriaBean")
@ViewScoped
public class VistoriaBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(VistoriaBean.class.getName());

    @Inject
    private VistoriaService vistoriaService;

    @Inject
    private RelatorioService relatorioService;

    @Inject
    private ObraService obraService;

    @Inject
    private UsuarioService usuarioService;

    @Inject
    private AccessPolicy accessPolicy;

    private Vistoria vistoria;
    private List<Vistoria> vistorias;
    private List<Vistoria> vistoriasFiltradas;
    private Long obraId;
    private Long responsavelId;

    @PostConstruct
    public void inicializar() {
        carregarVistorias();
        prepararNovo();
    }

    /**
     * Carrega a lista completa de vistorias a partir do serviço.
     */
    public void carregarVistorias() {
        this.vistorias = vistoriaService.listarVisiveis();
    }

    /**
     * Prepara uma nova instância limpa de Vistoria para abertura do modal de cadastro.
     */
    public void prepararNovo() {
        this.vistoria = new Vistoria();
        this.obraId = null;
        this.responsavelId = null;
        if (accessPolicy != null && accessPolicy.isEngenheiro()) {
            this.vistoria.setResponsavel(accessPolicy.usuario());
            this.responsavelId = accessPolicy.usuario().getId();
        }
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
            this.obraId = vistoriaSelecionada.getObra() == null ? null : vistoriaSelecionada.getObra().getId();
            this.responsavelId = vistoriaSelecionada.getResponsavel() == null
                    ? null : vistoriaSelecionada.getResponsavel().getId();
        }
    }

    /**
     * Salva ou atualiza a vistoria atual, recarrega a listagem e emite mensagem via FacesContext.
     */
    public void salvar() {
        try {
            boolean isNovo = (this.vistoria.getId() == null);
            associarSelecoesDoFormulario();
            vistoriaService.salvar(this.vistoria);

            carregarVistorias();

            String resumo = isNovo ? "Vistoria Cadastrada" : "Vistoria Atualizada";
            String detalhe = String.format("Vistoria da obra '%s' registrada com status '%s'.",
                    this.vistoria.getNomeObra(), this.vistoria.getStatus().getDescricao());

            adicionarMensagem(FacesMessage.SEVERITY_INFO, resumo, detalhe);
            callbackSalvamento(true);

            prepararNovo();
        } catch (com.obrasync.security.AcessoNegadoException e) {
            marcarFalha();
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Acesso negado",
                    "Seu perfil não pode salvar esta vistoria ou alterar o responsável informado.");
        } catch (IllegalArgumentException e) {
            marcarFalha();
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Dados inválidos", e.getMessage());
        } catch (Exception e) {
            marcarFalha();
            LOGGER.log(Level.SEVERE, "Falha inesperada ao salvar vistoria", e);
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro ao Salvar",
                    "Não foi possível concluir o cadastro. Consulte o log do servidor com o código VISTORIA-SAVE.");
        }
    }

    private void associarSelecoesDoFormulario() {
        if (accessPolicy != null && accessPolicy.isEngenheiro()) {
            Usuario autenticado = accessPolicy.usuario();
            responsavelId = autenticado == null ? null : autenticado.getId();
        }
        Obra obraSelecionada = obraService.buscarPorId(obraId);
        Usuario responsavelSelecionado = usuarioService.buscarPorId(responsavelId);
        if (obraSelecionada == null) throw new IllegalArgumentException("Selecione uma obra válida.");
        if (responsavelSelecionado == null || responsavelSelecionado.getPerfil() != Perfil.ENGENHEIRO)
            throw new IllegalArgumentException("Selecione um engenheiro responsável válido.");
        vistoria.setObra(obraSelecionada);
        vistoria.setResponsavel(responsavelSelecionado);
    }

    private void marcarFalha() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) context.validationFailed();
        callbackSalvamento(false);
    }

    private void callbackSalvamento(boolean salvou) {
        try {
            PrimeFaces.current().ajax().addCallbackParam("salvou", salvou);
        } catch (Throwable ignored) {
            // Permite testes unitários sem um ciclo JSF/PrimeFaces ativo.
        }
    }

    /**
     * Exclui uma vistoria registrada e envia mensagem de confirmação.
     *
     * @param vistoriaParaExcluir Vistoria a ser removida
     */
    public void excluir(Vistoria vistoriaParaExcluir) {
        try {
        if (vistoriaParaExcluir != null && vistoriaParaExcluir.getId() != null) {
            vistoriaService.excluir(vistoriaParaExcluir.getId());
            carregarVistorias();

            adicionarMensagem(FacesMessage.SEVERITY_WARN, "Vistoria Excluída",
                    "Vistoria #" + vistoriaParaExcluir.getId() + " da obra '" + vistoriaParaExcluir.getNomeObra() + "' foi removida com sucesso.");
        }
        } catch (RuntimeException e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Exclusão não realizada", "Verifique sua permissão e tente novamente.");
        }
    }

    public void alterarStatus(Vistoria selecionada) {
        try {
            vistoriaService.atualizarStatus(selecionada.getId(), selecionada.getStatus());
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Status atualizado", "Situação da vistoria registrada.");
        } catch (RuntimeException e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Status não atualizado", "Operação não permitida.");
        }
        carregarVistorias();
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

    public void setRelatorioService(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    public void setObraService(ObraService obraService) {
        this.obraService = obraService;
    }

    public void setUsuarioService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public void setAccessPolicy(AccessPolicy accessPolicy) {
        this.accessPolicy = accessPolicy;
    }

    /**
     * Dispara o download do laudo técnico da vistoria em formato PDF diretamente no navegador.
     * Configura o HttpServletResponse com Content-Type application/pdf e cabeçalho de anexo.
     *
     * @param vistoria Vistoria selecionada para emissão
     */
    public void baixarLaudoPdf(Vistoria vistoria) {
        if (vistoria == null) {
            adicionarMensagem(FacesMessage.SEVERITY_WARN, "Aviso", "Selecione uma vistoria válida para emitir o laudo.");
            return;
        }

        try {
            byte[] pdfBytes = relatorioService.gerarLaudoVistoriaPdf(vistoria);

            FacesContext facesContext = null;
            try {
                facesContext = FacesContext.getCurrentInstance();
            } catch (Throwable ignored) {
                // Permite execução de testes unitários isolados fora do container JSF
            }

            if (facesContext != null && facesContext.getExternalContext() != null) {
                HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

                response.reset();
                response.setContentType("application/pdf");
                response.setContentLength(pdfBytes.length);
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"laudo-vistoria-" + (vistoria.getId() != null ? vistoria.getId() : "temp") + ".pdf\"");

                ServletOutputStream outputStream = response.getOutputStream();
                outputStream.write(pdfBytes);
                outputStream.flush();

                facesContext.responseComplete();
            }
        } catch (Throwable e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Erro na Emissão do Laudo",
                    "Não foi possível gerar o laudo. Tente novamente.");
        }
    }

    // Métodos para obtenção de métricas de Dashboard
    public long getTotalVistorias() {
        return vistorias == null ? 0 : vistorias.size();
    }

    public long getAprovadasCount() {
        return contarVisiveisPorStatus(StatusVistoria.APROVADA);
    }

    public long getPendentesCount() {
        return contarVisiveisPorStatus(StatusVistoria.PENDENTE);
    }

    public long getReprovadasCount() {
        return contarVisiveisPorStatus(StatusVistoria.REPROVADA);
    }

    public long getEmAndamentoCount() { return contarVisiveisPorStatus(StatusVistoria.EM_ANDAMENTO); }

    public double getTaxaAprovacao() {
        long total = getTotalVistorias();
        return total == 0 ? 0.0 : (getAprovadasCount() * 100.0) / total;
    }

    private long contarVisiveisPorStatus(StatusVistoria status) {
        return vistorias == null ? 0 : vistorias.stream().filter(v -> v.getStatus() == status).count();
    }

    // Métodos utilitários para SelectOneMenu e filtros
    public StatusVistoria[] getStatusList() {
        return StatusVistoria.values();
    }

    public TipoVistoria[] getTiposList() {
        return TipoVistoria.values();
    }

    public List<Obra> getObrasDisponiveis() {
        return obraService.listarTodas();
    }

    public List<Usuario> getResponsaveisDisponiveis() {
        if (accessPolicy != null && accessPolicy.isEngenheiro() && accessPolicy.usuario() != null)
            return Collections.singletonList(accessPolicy.usuario());
        return usuarioService.listarPorPerfil(Perfil.ENGENHEIRO);
    }

    public String getTituloPainel() {
        if (accessPolicy != null && accessPolicy.isEngenheiro()) return "Minhas vistorias";
        if (accessPolicy != null && accessPolicy.isFiscal()) return "Fila de fiscalização";
        return "Central de vistorias";
    }

    public String getSubtituloPainel() {
        if (accessPolicy != null && accessPolicy.isEngenheiro())
            return "Acompanhe e atualize os registros sob sua responsabilidade técnica.";
        if (accessPolicy != null && accessPolicy.isFiscal())
            return "Revise evidências, consulte laudos e mantenha os status atualizados.";
        return "Visão consolidada da operação, equipes e andamento das inspeções.";
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

    public Long getObraId() { return obraId; }
    public void setObraId(Long obraId) { this.obraId = obraId; }
    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }
}
