package com.obrasync.controller;
import com.obrasync.model.*;
import com.obrasync.service.*;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.junit.jupiter.api.*;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class EvidenciaBeanTest {
    static class Bean extends EvidenciaBean {
        boolean erro; String mensagem;
        @Override protected void mensagem(boolean erro,String mensagem) {this.erro=erro;this.mensagem=mensagem;}
    }
    @Test @DisplayName("Tela envia e remove evidência, atualiza galeria e mostra feedback")
    void fluxo() throws Exception {
        EvidenciaService service=mock(EvidenciaService.class); EvidenciaStorageService storage=mock(EvidenciaStorageService.class);
        Bean bean=new Bean();bean.setService(service);bean.setStorage(storage);
        Vistoria v=new Vistoria();v.setId(1L);EvidenciaVistoria e=new EvidenciaVistoria();e.setId(2L);
        when(service.listar(1L)).thenReturn(Collections.singletonList(e));bean.abrir(v);
        assertSame(v,bean.getVistoria());assertEquals(1,bean.getEvidencias().size());
        UploadedFile file=mock(UploadedFile.class);FileUploadEvent event=mock(FileUploadEvent.class);
        when(event.getFile()).thenReturn(file);when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));
        when(file.getContentType()).thenReturn("image/png");when(file.getFileName()).thenReturn("foto.png");when(storage.receber(any())).thenReturn(new byte[]{1});
        bean.upload(event);assertFalse(bean.erro);verify(service).salvar(eq(1L),any(),eq("image/png"),eq("foto.png"),isNull());
        bean.excluir(e);assertFalse(bean.erro);verify(service).excluir(1L,2L);
        doThrow(new EvidenciaException(500,"Erro interno")).when(service).excluir(1L,2L);
        bean.excluir(e);assertTrue(bean.erro);assertFalse(bean.mensagem.contains("Erro interno"));
        when(file.getInputStream()).thenThrow(new java.io.IOException());bean.upload(event);assertTrue(bean.erro);
    }
}
