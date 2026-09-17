package com.obrasync.model;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA representando uma vistoria técnica de engenharia no canteiro de obras.
 * Possui relacionamentos @ManyToOne com Obra e com Usuario (engenheiro/responsável).
 */
@Entity
@Table(name = "tb_vistoria")
public class Vistoria implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "obra_id", nullable = false)
    private Obra obra;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_responsavel_id", nullable = false)
    private Usuario responsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoVistoria tipo;

    @JsonbDateFormat("yyyy-MM-dd")
    @Column(name = "data_vistoria", nullable = false)
    private LocalDate dataVistoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusVistoria status;

    @Column(length = 150)
    private String localizacao;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @OneToMany(mappedBy = "vistoria", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EvidenciaVistoria> evidencias = new ArrayList<>();

    public Vistoria() {
        this.dataVistoria = LocalDate.now();
        this.status = StatusVistoria.PENDENTE;
        this.obra = new Obra();
        this.responsavel = new Usuario();
    }

    public Vistoria(Long id, Obra obra, Usuario responsavel, TipoVistoria tipo, LocalDate dataVistoria,
                    StatusVistoria status, String localizacao, String observacoes) {
        this.id = id;
        this.obra = (obra != null) ? obra : new Obra();
        this.responsavel = (responsavel != null) ? responsavel : new Usuario();
        this.tipo = tipo;
        this.dataVistoria = dataVistoria;
        this.status = status;
        this.localizacao = localizacao;
        this.observacoes = observacoes;
    }

    /**
     * Construtor de conveniência para instanciação com nomes de obra e responsável em String.
     */
    public Vistoria(Long id, String nomeObra, String nomeResponsavel, TipoVistoria tipo, LocalDate dataVistoria,
                    StatusVistoria status, String localizacao, String observacoes) {
        this(id, new Obra(nomeObra), new Usuario(nomeResponsavel), tipo, dataVistoria, status, localizacao, observacoes);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Obra getObra() {
        return obra;
    }

    public void setObra(Obra obra) {
        this.obra = obra;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(Usuario responsavel) {
        this.responsavel = responsavel;
    }

    /**
     * Métodos de conveniência para obter o nome da obra e do responsável de forma nula-segura.
     */
    public String getNomeObra() {
        return obra != null ? obra.getNome() : "";
    }

    public String getNomeResponsavel() {
        return responsavel != null ? responsavel.getNome() : "";
    }

    public TipoVistoria getTipo() {
        return tipo;
    }

    public void setTipo(TipoVistoria tipo) {
        this.tipo = tipo;
    }

    public LocalDate getDataVistoria() {
        return dataVistoria;
    }

    public void setDataVistoria(LocalDate dataVistoria) {
        this.dataVistoria = dataVistoria;
    }

    public StatusVistoria getStatus() {
        return status;
    }

    public void setStatus(StatusVistoria status) {
        this.status = status;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    public List<EvidenciaVistoria> getEvidencias() { return evidencias; }

    @Override
    public Vistoria clone() {
        try {
            Vistoria copia = (Vistoria) super.clone();
            if (this.obra != null) {
                Obra novaObra = new Obra();
                novaObra.setId(this.obra.getId());
                novaObra.setNome(this.obra.getNome());
                novaObra.setEndereco(this.obra.getEndereco());
                novaObra.setDataInicio(this.obra.getDataInicio());
                novaObra.setDataPrevisaoFim(this.obra.getDataPrevisaoFim());
                copia.setObra(novaObra);
            }
            if (this.responsavel != null) {
                Usuario novoUsuario = new Usuario();
                novoUsuario.setId(this.responsavel.getId());
                novoUsuario.setNome(this.responsavel.getNome());
                novoUsuario.setEmail(this.responsavel.getEmail());
                novoUsuario.setSenha(this.responsavel.getSenha());
                novoUsuario.setPerfil(this.responsavel.getPerfil());
                copia.setResponsavel(novoUsuario);
            }
            return copia;
        } catch (CloneNotSupportedException e) {
            Vistoria copia = new Vistoria();
            copia.setId(this.id);
            copia.setObra(this.obra);
            copia.setResponsavel(this.responsavel);
            copia.setTipo(this.tipo);
            copia.setDataVistoria(this.dataVistoria);
            copia.setStatus(this.status);
            copia.setLocalizacao(this.localizacao);
            copia.setObservacoes(this.observacoes);
            return copia;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vistoria vistoria = (Vistoria) o;
        return Objects.equals(id, vistoria.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Vistoria{" +
                "id=" + id +
                ", obra=" + (obra != null ? obra.getNome() : null) +
                ", responsavel=" + (responsavel != null ? responsavel.getNome() : null) +
                ", tipo=" + tipo +
                ", dataVistoria=" + dataVistoria +
                ", status=" + status +
                '}';
    }
}
