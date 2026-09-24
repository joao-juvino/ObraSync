package com.obrasync.rest.dto;
import com.obrasync.model.EvidenciaVistoria;
import java.time.LocalDateTime;
public class EvidenciaResponseDTO {
    private final EvidenciaVistoria evidencia;
    public EvidenciaResponseDTO(EvidenciaVistoria evidencia) { this.evidencia = evidencia; }
    public Long getId() { return evidencia.getId(); }
    public String getNomeArquivo() { return evidencia.getNomeArquivo(); }
    public String getTipoMime() { return evidencia.getTipoMime(); }
    public long getTamanho() { return evidencia.getTamanho(); }
    public String getDescricao() { return evidencia.getDescricao(); }
    public LocalDateTime getDataUpload() { return evidencia.getDataUpload(); }
}
