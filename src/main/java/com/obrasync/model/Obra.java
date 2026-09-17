package com.obrasync.model;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidade JPA representando uma obra ou empreendimento da construção civil.
 */
@Entity
@Table(name = "tb_obra")
public class Obra implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 255)
    private String endereco;

    @JsonbDateFormat("yyyy-MM-dd")
    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @JsonbDateFormat("yyyy-MM-dd")
    @Column(name = "data_previsao_fim")
    private LocalDate dataPrevisaoFim;

    @JsonbTransient
    @OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Vistoria> vistorias = new ArrayList<>();

    public Obra() {
    }

    public Obra(String nome) {
        this.nome = nome;
    }

    public Obra(Long id, String nome, String endereco, LocalDate dataInicio, LocalDate dataPrevisaoFim) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.dataInicio = dataInicio;
        this.dataPrevisaoFim = dataPrevisaoFim;
    }

    public void adicionarVistoria(Vistoria vistoria) {
        if (vistoria != null) {
            vistorias.add(vistoria);
            vistoria.setObra(this);
        }
    }

    public void removerVistoria(Vistoria vistoria) {
        if (vistoria != null) {
            vistorias.remove(vistoria);
            vistoria.setObra(null);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataPrevisaoFim() {
        return dataPrevisaoFim;
    }

    public void setDataPrevisaoFim(LocalDate dataPrevisaoFim) {
        this.dataPrevisaoFim = dataPrevisaoFim;
    }

    public List<Vistoria> getVistorias() {
        return vistorias;
    }

    public void setVistorias(List<Vistoria> vistorias) {
        this.vistorias = vistorias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Obra obra = (Obra) o;
        return Objects.equals(id, obra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nome != null ? nome : "";
    }
}
