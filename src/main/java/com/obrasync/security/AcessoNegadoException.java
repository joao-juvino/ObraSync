package com.obrasync.security;

@javax.ejb.ApplicationException(rollback = true)
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException() { super("Operação não permitida para este usuário."); }
}
