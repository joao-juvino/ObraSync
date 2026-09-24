package com.obrasync.rest;
import com.obrasync.rest.dto.MensagemErroDTO;
import com.obrasync.security.AcessoNegadoException;
import com.obrasync.service.EvidenciaException;
import org.junit.jupiter.api.*;
import javax.ws.rs.core.Response;
import static org.junit.jupiter.api.Assertions.*;
class ApiExceptionMapperTest {
    @Test @DisplayName("Erros técnicos não vazam dados internos; status de domínio é preservado")
    void erros() {
        ApiExceptionMapper mapper = new ApiExceptionMapper();
        Exception[] erros = {new IllegalStateException("password=secret"), new IllegalArgumentException("sql"),
                new javax.persistence.PersistenceException("jdbc senha"), new AcessoNegadoException(),
                new EvidenciaException(413,"Limite excedido"), new javax.ws.rs.NotFoundException()};
        int[] status = {500,400,409,403,413,404};
        for (int i=0; i<erros.length; i++) {
            Response response = mapper.toResponse(new javax.ejb.EJBException(erros[i]));
            assertEquals(status[i], response.getStatus());
            String mensagem = ((MensagemErroDTO) response.getEntity()).getMensagem();
            assertFalse(mensagem.contains("secret")); assertFalse(mensagem.contains("jdbc"));
        }
    }
}
