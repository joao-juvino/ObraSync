package com.obrasync.rest;

import com.obrasync.model.StatusVistoria;
import com.obrasync.model.Vistoria;
import com.obrasync.rest.dto.MensagemErroDTO;
import com.obrasync.service.VistoriaService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * Endpoint REST para integração com aplicativos móveis e sistemas de campo.
 * Permite a consulta e o envio de laudos e vistorias de obras.
 */
@Path("/vistorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class VistoriaRestResource {

    @Inject
    private VistoriaService vistoriaService;

    @Context
    private UriInfo uriInfo;

    /**
     * Lista todas as vistorias cadastradas no sistema em formato JSON.
     * Retorna HTTP 200 OK.
     */
    @GET
    public Response listar() {
        List<Vistoria> vistorias = vistoriaService.listarTodas();
        return Response.ok(vistorias).build();
    }

    /**
     * Consulta uma vistoria específica pelo seu identificador.
     * Retorna HTTP 200 OK se encontrada, ou 404 Not Found caso contrário.
     */
    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MensagemErroDTO(Response.Status.BAD_REQUEST.getStatusCode(), "O identificador da vistoria é obrigatório."))
                    .build();
        }

        Vistoria vistoria = vistoriaService.buscarPorId(id);
        if (vistoria == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new MensagemErroDTO(Response.Status.NOT_FOUND.getStatusCode(), "Vistoria #" + id + " não encontrada."))
                    .build();
        }

        return Response.ok(vistoria).build();
    }

    /**
     * Recebe dados de uma nova vistoria enviada por aplicativos de campo.
     * Valida os campos obrigatórios e delega ao VistoriaService.
     *
     * Respostas HTTP:
     * - 201 Created: Vistoria persistida com sucesso (contém cabeçalho Location e objeto criado).
     * - 400 Bad Request: Dados inválidos ou campos obrigatórios ausentes.
     */
    @POST
    public Response cadastrar(Vistoria vistoria) {
        if (vistoria == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MensagemErroDTO(Response.Status.BAD_REQUEST.getStatusCode(), "O payload da vistoria não pode ser nulo."))
                    .build();
        }

        // Validação de campos obrigatórios
        if (vistoria.getObra() == null || vistoria.getNomeObra().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MensagemErroDTO(Response.Status.BAD_REQUEST.getStatusCode(), "O campo 'obra' é obrigatório."))
                    .build();
        }

        if (vistoria.getResponsavel() == null || vistoria.getNomeResponsavel().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MensagemErroDTO(Response.Status.BAD_REQUEST.getStatusCode(), "O campo 'responsavel' é obrigatório."))
                    .build();
        }

        if (vistoria.getTipo() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MensagemErroDTO(Response.Status.BAD_REQUEST.getStatusCode(), "O campo 'tipo' (tipo de vistoria) é obrigatório."))
                    .build();
        }

        // Atribui valores padrão se não informados pelo app de campo
        if (vistoria.getDataVistoria() == null) {
            vistoria.setDataVistoria(LocalDate.now());
        }

        if (vistoria.getStatus() == null) {
            vistoria.setStatus(StatusVistoria.PENDENTE);
        }

        try {
            Vistoria salva = vistoriaService.salvar(vistoria);

            URI locationUri;
            if (uriInfo != null) {
                locationUri = uriInfo.getAbsolutePathBuilder()
                        .path(String.valueOf(salva.getId()))
                        .build();
            } else {
                locationUri = URI.create("/api/vistorias/" + salva.getId());
            }

            return Response.created(locationUri)
                    .entity(salva)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MensagemErroDTO(Response.Status.BAD_REQUEST.getStatusCode(), "Erro ao processar vistoria: " + e.getMessage()))
                    .build();
        }
    }

    // Métodos para suporte a injeção manual em testes unitários
    public void setVistoriaService(VistoriaService vistoriaService) {
        this.vistoriaService = vistoriaService;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }
}
