package com.obrasync.rest.dto;
import java.util.List;
public class PaginaResponseDTO<T> { private final List<T> itens; private final int page,size; private final long total; public PaginaResponseDTO(List<T> i,int p,int s,long t){itens=i;page=p;size=s;total=t;} public List<T> getItens(){return itens;} public int getPage(){return page;} public int getSize(){return size;} public long getTotal(){return total;} public long getTotalPages(){return (total+size-1)/size;} }
