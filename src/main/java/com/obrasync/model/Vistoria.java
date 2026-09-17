package com.obrasync.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidade de domínio representando uma vistoria técnica de engenharia no canteiro de obras.
 */
public class Vistoria implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String obra;
    private String responsavel;
    private TipoVistoria tipo;
    private LocalDate dataVistoria;
    private StatusVistoria status;
    private String localizacao;
    private String observacoes;

    public Vistoria() {
        this.dataVistoria = LocalDate.now();
        this.status = StatusVistoria.PENDENTE;
    }

    public Vistoria(Long id, String obra, String responsavel, TipoVistoria tipo, LocalDate dataVistoria,
                    StatusVistoria status, String localizacao, String observacoes) {
        this.id = id;
        this.obra = obra;
        this.responsavel = responsavel;
        this.tipo = tipo;
        this.dataVistoria = dataVistoria;
        this.status = status;
        this.localizacao = localizacao;
        this.observacoes = observacoes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getObra() {
        return obra;
    }

    public void setObra(String obra) {
        this.obra = obra;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
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

    @Override
    public Vistoria clone() {
        try {
            return (Vistoria) super.clone();
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
                ", obra='" + obra + '\'' +
                ", responsavel='" + responsavel + '\'' +
                ", tipo=" + tipo +
                ", dataVistoria=" + dataVistoria +
                ", status=" + status +
                '}';
    }
}
