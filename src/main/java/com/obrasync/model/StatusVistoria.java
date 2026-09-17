package com.obrasync.model;

/**
 * Enumeração representando os estados possíveis de uma vistoria técnica na obra.
 * Contém propriedades para exibição em badges/tags do PrimeFaces (severity e icon).
 */
public enum StatusVistoria {
    APROVADA("Aprovada", "success", "pi pi-check-circle"),
    PENDENTE("Pendente", "warning", "pi pi-clock"),
    REPROVADA("Reprovada", "danger", "pi pi-times-circle"),
    EM_ANDAMENTO("Em Andamento", "info", "pi pi-spin pi-spinner");

    private final String descricao;
    private final String severity;
    private final String icon;

    StatusVistoria(String descricao, String severity, String icon) {
        this.descricao = descricao;
        this.severity = severity;
        this.icon = icon;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getSeverity() {
        return severity;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
