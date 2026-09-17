package com.obrasync.service;

import com.obrasync.model.StatusVistoria;
import com.obrasync.model.TipoVistoria;
import com.obrasync.model.Vistoria;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Serviço de aplicação CDI para gerenciamento do ciclo de vida das Vistorias.
 * Fornece operações de persistência em memória (com suporte a concorrência)
 * e métodos analíticos para os indicadores do Dashboard.
 */
@ApplicationScoped
public class VistoriaService implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<Long, Vistoria> repositorio = new ConcurrentHashMap<>();
    private final AtomicLong geradorId = new AtomicLong(1);

    @PostConstruct
    public void inicializarDadosIniciais() {
        carregarDadosExemplo();
    }

    private void carregarDadosExemplo() {
        salvar(new Vistoria(null, "Residencial Jardins das Oliveiras", "Eng. Carlos Eduardo Ramos",
                TipoVistoria.ESTRUTURAL, LocalDate.now().minusDays(10), StatusVistoria.APROVADA,
                "Bloco B - Fundações e Pilares", "Fundações e armaduras inspecionadas conforme laudo de cálculo estrutural. Aprovado para concretagem."));

        salvar(new Vistoria(null, "Edifício Horizon Corporate", "Eng. Mariana Tavares",
                TipoVistoria.ELETRICA, LocalDate.now().minusDays(5), StatusVistoria.PENDENTE,
                "12º Pavimento - Quadro de Distribuição", "Quadro secundário aguardando instalação do barramento de cobre e aterramento definitivo."));

        salvar(new Vistoria(null, "Condomínio Villa Bella", "Eng. Rodrigo Albuquerque",
                TipoVistoria.HIDRAULICA, LocalDate.now().minusDays(3), StatusVistoria.APROVADA,
                "Torre 3 - Prumadas de Água Fria", "Teste de estanqueidade realizado com 10 bar de pressão durante 12 horas. Sem vazamentos."));

        salvar(new Vistoria(null, "Parque Empresarial Alpha", "Eng. Camila Guimarães",
                TipoVistoria.SEGURANCA_TRABALHO, LocalDate.now().minusDays(2), StatusVistoria.REPROVADA,
                "Canteiro Geral - Linha de Vida", "Ausência de trava-quedas nas linhas de vida do 4º pavimento e andaimes sem rodapé regulamentar. Reinspeção exigida."));

        salvar(new Vistoria(null, "Residencial Jardins das Oliveiras", "Eng. Carlos Eduardo Ramos",
                TipoVistoria.ALVENARIA, LocalDate.now().minusDays(1), StatusVistoria.EM_ANDAMENTO,
                "Bloco A - 3º Pavimento", "Levantamento de alvenarias estruturais em execução com prumo e nível em conformidade."));

        salvar(new Vistoria(null, "Edifício Horizon Corporate", "Eng. Mariana Tavares",
                TipoVistoria.ACABAMENTO, LocalDate.now(), StatusVistoria.PENDENTE,
                "Térreo - Hall de Entrada e Portaria", "Assentamento de porcelanato com juntas de dilatação pendentes de rejunte epóxi."));
    }

    public List<Vistoria> listarTodas() {
        return repositorio.values().stream()
                .sorted(Comparator.comparing(Vistoria::getId).reversed())
                .collect(Collectors.toList());
    }

    public Vistoria buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        return repositorio.get(id);
    }

    public synchronized Vistoria salvar(Vistoria vistoria) {
        if (vistoria == null) {
            throw new IllegalArgumentException("A vistoria não pode ser nula.");
        }

        if (vistoria.getId() == null) {
            vistoria.setId(geradorId.getAndIncrement());
        }

        repositorio.put(vistoria.getId(), vistoria.clone());
        return vistoria;
    }

    public void excluir(Long id) {
        if (id != null) {
            repositorio.remove(id);
        }
    }

    public long contarTotal() {
        return repositorio.size();
    }

    public long contarAprovadas() {
        return repositorio.values().stream()
                .filter(v -> v.getStatus() == StatusVistoria.APROVADA)
                .count();
    }

    public long contarPendentes() {
        return repositorio.values().stream()
                .filter(v -> v.getStatus() == StatusVistoria.PENDENTE)
                .count();
    }

    public long contarReprovadas() {
        return repositorio.values().stream()
                .filter(v -> v.getStatus() == StatusVistoria.REPROVADA)
                .count();
    }

    public double calcularTaxaAprovacao() {
        long total = contarTotal();
        if (total == 0) {
            return 0.0;
        }
        return ((double) contarAprovadas() / total) * 100.0;
    }
}
