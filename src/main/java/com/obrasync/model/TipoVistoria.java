package com.obrasync.model;

/**
 * Tipos de vistoria técnica aplicáveis ao canteiro de obras.
 */
public enum TipoVistoria {
    ESTRUTURAL("Estrutural / Fundações"),
    ELETRICA("Instalações Elétricas"),
    HIDRAULICA("Instalações Hidrossanitárias"),
    ALVENARIA("Alvenaria e Vedação"),
    ACABAMENTO("Acabamento e Pintura"),
    SEGURANCA_TRABALHO("Segurança do Trabalho");

    private final String descricao;

    TipoVistoria(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
