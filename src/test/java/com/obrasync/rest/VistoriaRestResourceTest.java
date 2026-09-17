package com.obrasync.rest;

import com.obrasync.model.StatusVistoria;
import com.obrasync.model.TipoVistoria;
import com.obrasync.model.Vistoria;
import com.obrasync.rest.dto.MensagemErroDTO;
import com.obrasync.service.VistoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VistoriaRestResourceTest {

    private VistoriaRestResource resource;
    private VistoriaService service;

    @BeforeEach
    void setUp() {
        service = new VistoriaService();
        service.inicializarDadosIniciais();

        resource = new VistoriaRestResource();
        resource.setVistoriaService(service);
    }

    @Test
    @DisplayName("GET /api/vistorias deve retornar 200 OK com lista de vistorias")
    void deveListarVistoriasCom200OK() {
        Response response = resource.listar();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Vistoria> lista = (List<Vistoria>) response.getEntity();
        assertFalse(lista.isEmpty());
    }

    @Test
    @DisplayName("GET /api/vistorias/{id} deve retornar 200 OK para ID existente")
    void deveBuscarPorIdExistenteCom200OK() {
        List<Vistoria> lista = service.listarTodas();
        Long idExistente = lista.get(0).getId();

        Response response = resource.buscarPorId(idExistente);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof Vistoria);
        Vistoria vistoria = (Vistoria) response.getEntity();
        assertEquals(idExistente, vistoria.getId());
    }

    @Test
    @DisplayName("GET /api/vistorias/{id} deve retornar 404 Not Found para ID inexistente")
    void deveRetornar404ParaIdInexistente() {
        Response response = resource.buscarPorId(999999L);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof MensagemErroDTO);
    }

    @Test
    @DisplayName("POST /api/vistorias deve retornar 201 Created quando dados são válidos")
    void deveCadastrarVistoriaCom201Created() {
        Vistoria nova = new Vistoria();
        nova.setObra("Residencial Gran Ville");
        nova.setResponsavel("Eng. Juliana Castro");
        nova.setTipo(TipoVistoria.ELETRICA);
        nova.setDataVistoria(LocalDate.now());
        nova.setStatus(StatusVistoria.APROVADA);
        nova.setLocalizacao("Bloco A - Subsolo");
        nova.setObservacoes("Quadro geral conforme memorial descritivo.");

        Response response = resource.cadastrar(nova);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getLocation());
        assertTrue(response.getEntity() instanceof Vistoria);

        Vistoria salva = (Vistoria) response.getEntity();
        assertNotNull(salva.getId());
        assertEquals("Residencial Gran Ville", salva.getObra());
    }

    @Test
    @DisplayName("POST /api/vistorias deve retornar 400 Bad Request se payload for nulo")
    void deveRetornar400SePayloadNulo() {
        Response response = resource.cadastrar(null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof MensagemErroDTO);
        MensagemErroDTO erro = (MensagemErroDTO) response.getEntity();
        assertTrue(erro.getMensagem().contains("não pode ser nulo"));
    }

    @Test
    @DisplayName("POST /api/vistorias deve retornar 400 Bad Request se obra não for informada")
    void deveRetornar400SeObraVazia() {
        Vistoria invalida = new Vistoria();
        invalida.setObra("   ");
        invalida.setResponsavel("Eng. Carlos");
        invalida.setTipo(TipoVistoria.ESTRUTURAL);

        Response response = resource.cadastrar(invalida);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        MensagemErroDTO erro = (MensagemErroDTO) response.getEntity();
        assertTrue(erro.getMensagem().contains("obra"));
    }

    @Test
    @DisplayName("POST /api/vistorias deve retornar 400 Bad Request se responsavel não for informado")
    void deveRetornar400SeResponsavelVazio() {
        Vistoria invalida = new Vistoria();
        invalida.setObra("Residencial Teste");
        invalida.setResponsavel(null);
        invalida.setTipo(TipoVistoria.ESTRUTURAL);

        Response response = resource.cadastrar(invalida);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        MensagemErroDTO erro = (MensagemErroDTO) response.getEntity();
        assertTrue(erro.getMensagem().contains("responsavel"));
    }

    @Test
    @DisplayName("POST /api/vistorias deve retornar 400 Bad Request se tipo de vistoria for nulo")
    void deveRetornar400SeTipoNulo() {
        Vistoria invalida = new Vistoria();
        invalida.setObra("Residencial Teste");
        invalida.setResponsavel("Eng. Carlos");
        invalida.setTipo(null);

        Response response = resource.cadastrar(invalida);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        MensagemErroDTO erro = (MensagemErroDTO) response.getEntity();
        assertTrue(erro.getMensagem().contains("tipo"));
    }
}
