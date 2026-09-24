package com.obrasync.rest.dto;
import com.obrasync.model.StatusVistoria;
public class StatusRequestDTO {
    private StatusVistoria status;
    public StatusVistoria getStatus() { return status; }
    public void setStatus(StatusVistoria status) { this.status = status; }
}
