package com.obrasync.rest.dto;
import com.obrasync.model.Vistoria; import java.time.LocalDate;
@org.eclipse.microprofile.openapi.annotations.media.Schema(description = "Representação pública de uma vistoria, incluindo os dados resumidos dos relacionamentos.")
public class VistoriaResponseDTO {
    private Long id, obraId, responsavelId; private String obra, responsavel, tipo, status, localizacao, observacoes; private LocalDate dataVistoria;
    public static VistoriaResponseDTO de(Vistoria v){ VistoriaResponseDTO d=new VistoriaResponseDTO(); d.id=v.getId(); d.obraId=v.getObra().getId(); d.obra=v.getNomeObra(); d.responsavelId=v.getResponsavel().getId(); d.responsavel=v.getNomeResponsavel(); d.tipo=v.getTipo().name(); d.status=v.getStatus().name(); d.dataVistoria=v.getDataVistoria(); d.localizacao=v.getLocalizacao(); d.observacoes=v.getObservacoes(); return d; }
    public Long getId(){return id;} public Long getObraId(){return obraId;} public String getObra(){return obra;} public Long getResponsavelId(){return responsavelId;} public String getResponsavel(){return responsavel;} public String getTipo(){return tipo;} public String getStatus(){return status;} public LocalDate getDataVistoria(){return dataVistoria;} public String getLocalizacao(){return localizacao;} public String getObservacoes(){return observacoes;}
}
