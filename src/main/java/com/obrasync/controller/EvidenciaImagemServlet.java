package com.obrasync.controller;

import com.obrasync.model.EvidenciaVistoria;
import com.obrasync.service.*;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/** Imagens da tela JSF usam a sessão protegida pelo AuthorizationFilter. */
@WebServlet("/evidencias/imagem")
public class EvidenciaImagemServlet extends HttpServlet {
    @Inject private EvidenciaService service;
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Long vistoria = Long.valueOf(request.getParameter("vistoria"));
            Long id = Long.valueOf(request.getParameter("id"));
            EvidenciaVistoria evidencia = service.buscar(vistoria, id);
            byte[] conteudo = service.conteudo(vistoria, id);
            response.setContentType(evidencia.getTipoMime());
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Cache-Control", "private, no-store");
            response.getOutputStream().write(conteudo);
        } catch (IllegalArgumentException e) { response.sendError(400); }
        catch (EvidenciaException e) { response.sendError(e.getStatus()); }
    }
    public void setService(EvidenciaService service) { this.service = service; }
}
