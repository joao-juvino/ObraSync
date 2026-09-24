package com.obrasync.rest;

import com.obrasync.model.EvidenciaVistoria;
import com.obrasync.rest.dto.EvidenciaResponseDTO;
import com.obrasync.service.*;
import org.junit.jupiter.api.*;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import javax.ws.rs.core.Response;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EvidenciaRestResourceTest {
    EvidenciaService service; EvidenciaStorageService storage; EvidenciaRestResource resource;
    @BeforeEach void preparar() {
        service = mock(EvidenciaService.class); storage = mock(EvidenciaStorageService.class);
        resource = new EvidenciaRestResource(); resource.setService(service); resource.setStorage(storage);
    }
    @Test @DisplayName("Upload retorna 201 e DTO sem caminho do storage")
    void upload() {
        byte[] bytes = {1,2}; when(storage.receber(any())).thenReturn(bytes);
        EvidenciaVistoria e = new EvidenciaVistoria(); e.setId(3L);
        when(service.salvar(1L, bytes, "image/png", "foto.png", "teste")).thenReturn(e);
        Response response = resource.enviar(1L, "image/png", "foto.png", "teste", new ByteArrayInputStream(bytes));
        assertEquals(201, response.getStatus()); assertEquals(3L, ((EvidenciaResponseDTO) response.getEntity()).getId());
        assertTrue(response.getLocation().toString().endsWith("3"));
    }
    @Test @DisplayName("Leitura entrega imagem com MIME e proteção nosniff")
    void leituraExclusao() {
        EvidenciaVistoria e = new EvidenciaVistoria(); e.setTipoMime("image/png");
        when(service.listar(1L)).thenReturn(Collections.singletonList(e));
        when(service.buscar(1L, 2L)).thenReturn(e); when(service.conteudo(1L,2L)).thenReturn(new byte[]{1});
        assertEquals(200, resource.listar(1L).getStatus());
        Response response = resource.imagem(1L, 2L);
        assertEquals("image/png", response.getMediaType().toString());
        assertEquals("nosniff", response.getHeaderString("X-Content-Type-Options"));
        assertArrayEquals(new byte[]{1}, (byte[]) response.getEntity());
        assertEquals(204, resource.excluir(1L, 2L).getStatus()); verify(service).excluir(1L, 2L);
    }
    @Test @DisplayName("404 do serviço é preservado pelo tratamento de erro")
    void ausente() {
        when(service.buscar(1L,2L)).thenThrow(new EvidenciaException(404, "Não encontrada"));
        EvidenciaException erro = assertThrows(EvidenciaException.class, () -> resource.imagem(1L,2L));
        assertEquals(404, new ApiExceptionMapper().toResponse(erro).getStatus());
    }
}
