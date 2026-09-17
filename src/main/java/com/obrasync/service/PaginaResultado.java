package com.obrasync.service;

import java.util.List;

public class PaginaResultado<T> {
    private final List<T> itens; private final long total;
    public PaginaResultado(List<T> itens, long total) { this.itens = itens; this.total = total; }
    public List<T> getItens() { return itens; } public long getTotal() { return total; }
}
