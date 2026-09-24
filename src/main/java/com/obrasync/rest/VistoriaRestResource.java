package com.obrasync.rest;

import com.obrasync.model.*;
import com.obrasync.rest.dto.*;
import com.obrasync.security.*;
import com.obrasync.service.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Secured @Path("/vistorias") @Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON) @RequestScoped
@Tag(name="Vistorias", description="CRUD, filtros e indicadores de vistorias")
public class VistoriaRestResource {
    @Inject private VistoriaService vistoriaService;
    @Context private UriInfo uriInfo;

    @GET @Operation(summary="Lista vistorias com filtros e paginação no banco")
    public Response listar(@QueryParam("status") String status, @QueryParam("tipo") String tipo,
            @QueryParam("obra") String obra, @QueryParam("responsavel") String responsavel,
            @QueryParam("dataInicial") String inicialTexto, @QueryParam("dataFinal") String finalTexto,
            @DefaultValue("0") @QueryParam("page") int page, @DefaultValue("20") @QueryParam("size") int size) {
        try {
            LocalDate inicial = data(inicialTexto), fim = data(finalTexto);
            if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE
                    || (inicial != null && fim != null && inicial.isAfter(fim)))
                return erro(400,"Paginação ou período inválidos.");
            PaginaResultado<Vistoria> pagina = vistoriaService.pesquisar(en(status,StatusVistoria.class),
                    en(tipo,TipoVistoria.class),obra,responsavel,inicial,fim,page,size);
            return Response.ok(new PaginaResponseDTO<>(pagina.getItens().stream().map(VistoriaResponseDTO::de)
                    .collect(Collectors.toList()),page,size,pagina.getTotal())).build();
        } catch (IllegalArgumentException | DateTimeParseException e) {
            return erro(400,"Filtros ou datas inválidos.");
        }
    }

    @GET @Path("/resumo") @Operation(summary="Obtém totalizadores e taxa de aprovação")
    public Response resumo() {
        long total = vistoriaService.contarTotal(), aprovadas = vistoriaService.contarAprovadas();
        long pendentes = vistoriaService.contarPendentes(), reprovadas = vistoriaService.contarReprovadas();
        Map<String,Object> resumo = new LinkedHashMap<>();
        resumo.put("total",total); resumo.put("aprovadas",aprovadas); resumo.put("pendentes",pendentes);
        resumo.put("reprovadas",reprovadas); resumo.put("emAndamento",total-aprovadas-pendentes-reprovadas);
        resumo.put("taxaAprovacao",vistoriaService.calcularTaxaAprovacao());
        return Response.ok(resumo).build();
    }

    @GET @Path("/{id}") @Operation(summary="Busca uma vistoria por ID")
    public Response buscar(@PathParam("id") Long id) {
        Vistoria vistoria = vistoriaService.buscarPorId(id);
        return vistoria == null ? erro(404,"Vistoria não encontrada.") : Response.ok(VistoriaResponseDTO.de(vistoria)).build();
    }

    @POST @Operation(summary="Cria uma vistoria")
    public Response criar(VistoriaRequestDTO dto) {
        if (dto == null) return erro(400,"Payload obrigatório.");
        try {
            Vistoria vistoria = salvar(null,dto);
            URI location = uriInfo == null ? URI.create("/api/vistorias/" + vistoria.getId())
                    : uriInfo.getAbsolutePathBuilder().path(vistoria.getId().toString()).build();
            return Response.created(location).entity(VistoriaResponseDTO.de(vistoria)).build();
        } catch (IllegalArgumentException e) { return erro(400,e.getMessage()); }
    }

    @PUT @Path("/{id}") @Operation(summary="Substitui os campos editáveis de uma vistoria")
    public Response atualizar(@PathParam("id") Long id, VistoriaRequestDTO dto) {
        if (dto == null) return erro(400,"Payload obrigatório.");
        if (vistoriaService.buscarPorId(id) == null) return erro(404,"Vistoria não encontrada.");
        try { return Response.ok(VistoriaResponseDTO.de(salvar(id,dto))).build(); }
        catch (IllegalArgumentException e) { return erro(400,e.getMessage()); }
    }

    @PATCH @Path("/{id}/status") @RolesPermitidos({"ADMIN","FISCAL"})
    @Operation(summary="Altera somente o status; permitido a ADMIN e FISCAL")
    public Response status(@PathParam("id") Long id, StatusRequestDTO dto) {
        if (dto == null || dto.getStatus() == null) return erro(400,"Status obrigatório.");
        return Response.ok(VistoriaResponseDTO.de(vistoriaService.atualizarStatus(id,dto.getStatus()))).build();
    }

    @DELETE @Path("/{id}") @Operation(summary="Exclui uma vistoria e suas evidências; somente ADMIN")
    public Response excluir(@PathParam("id") Long id) {
        return vistoriaService.excluir(id) ? Response.noContent().build() : erro(404,"Vistoria não encontrada.");
    }

    private Vistoria salvar(Long id,VistoriaRequestDTO dto) {
        if (dto.getObraId() == null || dto.getResponsavelId() == null || dto.getTipo() == null)
            throw new IllegalArgumentException("obraId, responsavelId e tipo são obrigatórios.");
        Vistoria vistoria = new Vistoria(); vistoria.setId(id);
        Obra obra = new Obra(); obra.setId(dto.getObraId());
        Usuario usuario = new Usuario(); usuario.setId(dto.getResponsavelId());
        vistoria.setObra(obra); vistoria.setResponsavel(usuario); vistoria.setTipo(dto.getTipo());
        vistoria.setDataVistoria(dto.getDataVistoria() == null ? LocalDate.now() : dto.getDataVistoria());
        vistoria.setStatus(dto.getStatus() == null ? StatusVistoria.PENDENTE : dto.getStatus());
        vistoria.setLocalizacao(dto.getLocalizacao()); vistoria.setObservacoes(dto.getObservacoes());
        return vistoriaService.salvar(vistoria);
    }
    private LocalDate data(String valor) { return valor == null || valor.isBlank() ? null : LocalDate.parse(valor); }
    private <E extends Enum<E>> E en(String valor,Class<E> tipo) {
        return valor == null || valor.isBlank() ? null : Enum.valueOf(tipo,valor.toUpperCase(Locale.ROOT));
    }
    private Response erro(int status,String mensagem) {
        return Response.status(status).entity(new MensagemErroDTO(status,mensagem)).build();
    }
    public void setVistoriaService(VistoriaService service) { vistoriaService = service; }
    public void setUriInfo(UriInfo uri) { uriInfo = uri; }
}
