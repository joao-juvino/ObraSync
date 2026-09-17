package com.obrasync.rest.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Objeto de transferência para padronização de respostas de erro da API REST.
 */
public class MensagemErroDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int status;
    private String mensagem;
    private String timestamp;

    public MensagemErroDTO() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public MensagemErroDTO(int status, String mensagem) {
        this();
        this.status = status;
        this.mensagem = mensagem;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
