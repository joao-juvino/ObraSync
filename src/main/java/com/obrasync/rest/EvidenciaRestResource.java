package com.obrasync.rest;

import com.obrasync.model.EvidenciaVistoria;
import com.obrasync.rest.dto.EvidenciaResponseDTO;
import com.obrasync.security.*;
import com.obrasync.service.*;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.InputStream;
import java.net.URI;
import java.util.stream.Collectors;

@Secured @RequestScoped @Path("/vistorias/{vistoriaId}/evidencias")
@Produces(MediaType.APPLICATION_JSON)
@org.eclipse.microprofile.openapi.annotations.tags.Tag(name="Evidências")
public class EvidenciaRestResource {
    @Inject private EvidenciaService service;
    @Inject private EvidenciaStorageService storage;
    @Context private UriInfo uriInfo;
    @POST @Consumes({"image/jpeg", "image/png"})
    @org.eclipse.microprofile.openapi.annotations.Operation(summary="Envia JPEG ou PNG como corpo binário, até 10 MB")
    public Response enviar(@PathParam("vistoriaId") Long id, @HeaderParam("Content-Type") String mime,
                           @QueryParam("nome") String nome, @QueryParam("descricao") String descricao, InputStream arquivo) {
        EvidenciaVistoria evidencia = service.salvar(id, storage.receber(arquivo), mime, nome, descricao);
        URI location = uriInfo == null ? URI.create("/api/vistorias/" + id + "/evidencias/" + evidencia.getId())
                : uriInfo.getAbsolutePathBuilder().path(evidencia.getId().toString()).build();
        return Response.created(location).entity(new EvidenciaResponseDTO(evidencia)).build();
    }
    @GET public Response listar(@PathParam("vistoriaId") Long id) {
        return Response.ok(service.listar(id).stream().map(EvidenciaResponseDTO::new).collect(Collectors.toList())).build();
    }
    @GET @Path("/{evidenciaId}") @Produces({"image/jpeg", "image/png"})
    public Response imagem(@PathParam("vistoriaId") Long id, @PathParam("evidenciaId") Long evidenciaId) {
        EvidenciaVistoria evidencia = service.buscar(id, evidenciaId);
        return Response.ok(service.conteudo(id, evidenciaId), evidencia.getTipoMime())
                .header("X-Content-Type-Options", "nosniff").header("Cache-Control", "private, no-store").build();
    }
    @DELETE @Path("/{evidenciaId}") @RolesPermitidos({"ADMIN", "ENGENHEIRO"})
    public Response excluir(@PathParam("vistoriaId") Long id, @PathParam("evidenciaId") Long evidenciaId) {
        service.excluir(id, evidenciaId);
        return Response.noContent().build();
    }
    public void setService(EvidenciaService service) { this.service = service; }
    public void setStorage(EvidenciaStorageService storage) { this.storage = storage; }
}
