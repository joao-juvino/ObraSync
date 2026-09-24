package com.obrasync.rest;
import com.obrasync.model.*; import com.obrasync.rest.dto.*; import com.obrasync.service.*; import org.junit.jupiter.api.*; import org.junit.jupiter.api.extension.ExtendWith; import org.mockito.*; import org.mockito.junit.jupiter.MockitoExtension; import javax.ws.rs.core.Response; import java.time.LocalDate; import java.util.*; import static org.junit.jupiter.api.Assertions.*; import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) class VistoriaRestResourceTest {
 @Mock VistoriaService service; VistoriaRestResource resource;
 @BeforeEach void setUp(){resource=new VistoriaRestResource();resource.setVistoriaService(service);}
 @Test @DisplayName("Rejeita enums, datas e intervalos inválidos com 400") void filtrosInvalidos(){
  assertEquals(400,resource.listar("INVALIDO",null,null,null,null,null,0,20).getStatus());
  assertEquals(400,resource.listar(null,null,null,null,"ontem",null,0,20).getStatus());
  assertEquals(400,resource.listar(null,null,null,null,"2026-02-02","2026-01-01",0,20).getStatus());
  verifyNoInteractions(service);
 }
 @Test @DisplayName("CRUD trata payload ausente e IDs inexistentes") void errosCrud(){
  assertEquals(400,resource.criar(null).getStatus());assertEquals(400,resource.criar(new VistoriaRequestDTO()).getStatus());
  assertEquals(400,resource.atualizar(1L,null).getStatus());assertEquals(404,resource.atualizar(1L,req()).getStatus());
  assertEquals(404,resource.excluir(1L).getStatus());
 }
 @Test @DisplayName("Resumo apresenta totais e status altera somente o campo solicitado") void resumoStatus(){
  when(service.contarTotal()).thenReturn(10L);when(service.contarAprovadas()).thenReturn(4L);when(service.contarPendentes()).thenReturn(3L);when(service.contarReprovadas()).thenReturn(2L);when(service.calcularTaxaAprovacao()).thenReturn(40.0);
  Map<?,?> resumo=(Map<?,?>)resource.resumo().getEntity();assertEquals(1L,resumo.get("emAndamento"));assertEquals(40.0,resumo.get("taxaAprovacao"));
  assertEquals(400,resource.status(1L,null).getStatus());StatusRequestDTO dto=new StatusRequestDTO();dto.setStatus(StatusVistoria.APROVADA);
  when(service.atualizarStatus(1L,StatusVistoria.APROVADA)).thenReturn(v(1L));assertEquals(200,resource.status(1L,dto).getStatus());verify(service).atualizarStatus(1L,StatusVistoria.APROVADA);
 }
 @Test @DisplayName("GET filtra e pagina na camada de serviço") void listaPaginada(){when(service.pesquisar(eq(StatusVistoria.PENDENTE),isNull(),any(),any(),isNull(),isNull(),eq(0),eq(20))).thenReturn(new PaginaResultado<>(Collections.singletonList(v(1L)),1));Response r=resource.listar("PENDENTE",null,null,null,null,null,0,20);assertEquals(200,r.getStatus());verify(service).pesquisar(eq(StatusVistoria.PENDENTE),isNull(),isNull(),isNull(),isNull(),isNull(),eq(0),eq(20));}
 @Test @DisplayName("POST cria a partir de DTO") void cria(){VistoriaRequestDTO d=req();when(service.salvar(any())).thenReturn(v(3L));Response r=resource.criar(d);assertEquals(201,r.getStatus());assertTrue(r.getEntity() instanceof VistoriaResponseDTO);}
 @Test @DisplayName("PUT atualiza e DELETE retorna 204") void atualizaEExclui(){when(service.buscarPorId(1L)).thenReturn(v(1L));when(service.salvar(any())).thenReturn(v(1L));assertEquals(200,resource.atualizar(1L,req()).getStatus());when(service.excluir(1L)).thenReturn(true);assertEquals(204,resource.excluir(1L).getStatus());}
 @Test @DisplayName("Valida paginação e recurso inexistente") void valida(){assertEquals(400,resource.listar(null,null,null,null,null,null,-1,20).getStatus());when(service.buscarPorId(9L)).thenReturn(null);assertEquals(404,resource.buscar(9L).getStatus());}
 private VistoriaRequestDTO req(){VistoriaRequestDTO d=new VistoriaRequestDTO();d.setObraId(1L);d.setResponsavelId(2L);d.setTipo(TipoVistoria.ELETRICA);return d;} private Vistoria v(Long id){Obra o=new Obra();o.setId(1L);o.setNome("Obra");Usuario u=new Usuario();u.setId(2L);u.setNome("Eng");return new Vistoria(id,o,u,TipoVistoria.ELETRICA,LocalDate.now(),StatusVistoria.PENDENTE,null,null);}
}
