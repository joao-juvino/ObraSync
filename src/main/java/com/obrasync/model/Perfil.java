package com.obrasync.model;

/**
 * Enumeração dos perfis de acesso e autorização dos usuários no sistema ObraSync.
 */
public enum Perfil {
    ADMIN("Administrador"),
    ENGENHEIRO("Engenheiro"),
    FISCAL("Fiscal");

    private final String descricao;

    Perfil(String descricao) {
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
