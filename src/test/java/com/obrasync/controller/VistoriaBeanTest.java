package com.obrasync.controller;

import com.obrasync.model.Obra;
import com.obrasync.model.Perfil;
import com.obrasync.model.StatusVistoria;
import com.obrasync.model.TipoVistoria;
import com.obrasync.model.Usuario;
import com.obrasync.model.Vistoria;
import com.obrasync.report.RelatorioService;
import com.obrasync.service.VistoriaService;
import com.obrasync.service.ObraService;
import com.obrasync.service.UsuarioService;
import com.obrasync.security.AccessPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VistoriaBeanTest {

    private VistoriaBean bean;
    @Mock private VistoriaService service;
    @Mock private RelatorioService relatorioService;
    @Mock private ObraService obraService;
    @Mock private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        when(service.listarVisiveis()).thenReturn(Collections.singletonList(vistoria(1L)));
        lenient().when(obraService.buscarPorId(1L)).thenAnswer(invocation -> vistoria(1L).getObra());
        lenient().when(usuarioService.buscarPorId(2L)).thenAnswer(invocation -> vistoria(1L).getResponsavel());

        bean = new VistoriaBean();
        bean.setVistoriaService(service);
        bean.setRelatorioService(relatorioService);
        bean.setObraService(obraService);
        bean.setUsuarioService(usuarioService);
        bean.inicializar();
    }

    @Test
    @DisplayName("Deve inicializar o bean com listagem do serviço JPA e formulário novo")
    void deveInicializarBean() {
        assertEquals(1, bean.getVistorias().size());
        assertNull(bean.getVistoria().getId());
        assertEquals(StatusVistoria.PENDENTE, bean.getVistoria().getStatus());
    }

    @Test
    @DisplayName("Deve preparar edição com cópia independente da vistoria listada")
    void devePrepararEdicaoComCopiaIndependente() {
        Vistoria selecionada = bean.getVistorias().get(0);

        bean.prepararEdicao(selecionada);
        bean.getVistoria().getObra().setNome("Obra alterada no formulário");

        assertEquals(1L, bean.getVistoria().getId());
        assertEquals("Residencial Aurora", selecionada.getObra().getNome());
    }

    @Test
    @DisplayName("Deve salvar e recarregar a listagem")
    void deveSalvarVistoria() {
        bean.prepararEdicao(vistoria(1L));

        bean.salvar();

        verify(service).salvar(any(Vistoria.class));
        verify(service, times(2)).listarVisiveis();
        verify(obraService).buscarPorId(1L);
        verify(usuarioService).buscarPorId(2L);
        assertNull(bean.getVistoria().getId());
    }

    @Test
    @DisplayName("Ao criar como engenheiro, define o usuário autenticado como responsável")
    void devePrepararNovoParaEngenheiro() {
        Usuario engenheiro = new Usuario(9L, "Eng. Logado", "eng@obrasync.com", "hash", Perfil.ENGENHEIRO);
        AccessPolicy policy = mock(AccessPolicy.class);
        when(policy.isEngenheiro()).thenReturn(true);
        when(policy.usuario()).thenReturn(engenheiro);
        bean.setAccessPolicy(policy);

        bean.prepararNovo();

        assertSame(engenheiro, bean.getVistoria().getResponsavel());
    }

    @Test
    @DisplayName("Deve excluir vistoria persistida e recarregar a listagem")
    void deveExcluirVistoria() {
        bean.excluir(vistoria(1L));

        verify(service).excluir(1L);
        verify(service, times(2)).listarVisiveis();
    }

    @Test
    @DisplayName("Deve expor métricas e enums do dashboard")
    void deveExporMetricasEEnums() {
        assertEquals(1L, bean.getTotalVistorias());
        assertEquals(1L, bean.getAprovadasCount());
        assertEquals(StatusVistoria.values().length, bean.getStatusList().length);
        assertEquals(TipoVistoria.values().length, bean.getTiposList().length);
    }

    private Vistoria vistoria(Long id) {
        Obra obra = new Obra(1L, "Residencial Aurora", "Rua A", LocalDate.now(), LocalDate.now().plusDays(1));
        Usuario usuario = new Usuario(2L, "Eng. Juliana", "juliana@obrasync.com", "hash", Perfil.ENGENHEIRO);
        return new Vistoria(id, obra, usuario, TipoVistoria.ESTRUTURAL, LocalDate.now(),
                StatusVistoria.APROVADA, "Bloco A", "Conforme");
    }
}
