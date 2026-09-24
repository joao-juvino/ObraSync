package com.obrasync.controller;

import com.obrasync.model.*;
import com.obrasync.service.*;
import org.primefaces.event.FileUploadEvent;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.*;
import java.io.Serializable;
import java.util.*;

@Named @ViewScoped
public class EvidenciaBean implements Serializable {
    @Inject private EvidenciaService service;
    @Inject private EvidenciaStorageService storage;
    private Vistoria vistoria;
    private List<EvidenciaVistoria> evidencias = Collections.emptyList();
    public void abrir(Vistoria vistoria) {
        this.vistoria = vistoria;
        evidencias = service.listar(vistoria.getId());
    }
    public void upload(FileUploadEvent event) {
        try (java.io.InputStream input = event.getFile().getInputStream()) {
            service.salvar(vistoria.getId(), storage.receber(input), event.getFile().getContentType(), event.getFile().getFileName(), null);
            abrir(vistoria);
            mensagem(false, "Evidência enviada com sucesso.");
        } catch (Exception e) { mensagem(true, "Não foi possível enviar. Use JPEG ou PNG válido, até 10 MB, e verifique sua permissão."); }
    }
    public void excluir(EvidenciaVistoria evidencia) {
        try {
            service.excluir(vistoria.getId(), evidencia.getId());
            abrir(vistoria);
            mensagem(false, "Evidência excluída.");
        } catch (RuntimeException e) { mensagem(true, "Não foi possível excluir esta evidência."); }
    }
    protected void mensagem(boolean erro, String texto) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) context.addMessage(null, new FacesMessage(erro ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO, texto, null));
    }
    public Vistoria getVistoria() { return vistoria; }
    public List<EvidenciaVistoria> getEvidencias() { return evidencias; }
    public void setService(EvidenciaService service) { this.service = service; }
    public void setStorage(EvidenciaStorageService storage) { this.storage = storage; }
}
