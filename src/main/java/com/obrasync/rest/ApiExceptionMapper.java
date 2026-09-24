package com.obrasync.rest;

import com.obrasync.rest.dto.MensagemErroDTO;
import com.obrasync.security.AcessoNegadoException;
import com.obrasync.service.EvidenciaException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<Exception> {
    @Override public Response toResponse(Exception erro) {
        Throwable causa = erro;
        while (causa instanceof javax.ejb.EJBException && causa.getCause() != null) causa = causa.getCause();
        int status = 500;
        String mensagem = "Não foi possível concluir a operação.";
        if (causa instanceof AcessoNegadoException) { status = 403; mensagem = causa.getMessage(); }
        else if (causa instanceof EvidenciaException) { status = ((EvidenciaException) causa).getStatus(); mensagem = causa.getMessage(); }
        else if (causa instanceof IllegalArgumentException) { status = 400; mensagem = "Dados inválidos."; }
        else if (causa instanceof WebApplicationException) { status = ((WebApplicationException) causa).getResponse().getStatus(); mensagem = "Requisição inválida ou recurso indisponível."; }
        else if (causa instanceof javax.persistence.PersistenceException) { status = 409; mensagem = "Operação em conflito com os dados existentes."; }
        return Response.status(status).type(MediaType.APPLICATION_JSON).entity(new MensagemErroDTO(status, mensagem)).build();
    }
}
