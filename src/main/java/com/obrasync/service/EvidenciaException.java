package com.obrasync.service;

@javax.ejb.ApplicationException(rollback = true)
public class EvidenciaException extends RuntimeException {
    private final int status;
    public EvidenciaException(int status, String mensagem) { super(mensagem); this.status = status; }
    public int getStatus() { return status; }
}
