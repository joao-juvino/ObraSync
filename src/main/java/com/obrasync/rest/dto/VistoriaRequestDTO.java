package com.obrasync.rest.dto;
import com.obrasync.model.StatusVistoria; import com.obrasync.model.TipoVistoria; import java.time.LocalDate;
@org.eclipse.microprofile.openapi.annotations.media.Schema(description = "Dados necessários para criar ou substituir uma vistoria.", example = "{\"obraId\":1,\"responsavelId\":2,\"tipo\":\"ELETRICA\",\"status\":\"PENDENTE\",\"dataVistoria\":\"2026-09-17\"}")
public class VistoriaRequestDTO {
    private Long obraId, responsavelId; private TipoVistoria tipo; private LocalDate dataVistoria; private StatusVistoria status; private String localizacao, observacoes;
    public Long getObraId(){return obraId;} public void setObraId(Long v){obraId=v;} public Long getResponsavelId(){return responsavelId;} public void setResponsavelId(Long v){responsavelId=v;} public TipoVistoria getTipo(){return tipo;} public void setTipo(TipoVistoria v){tipo=v;} public LocalDate getDataVistoria(){return dataVistoria;} public void setDataVistoria(LocalDate v){dataVistoria=v;} public StatusVistoria getStatus(){return status;} public void setStatus(StatusVistoria v){status=v;} public String getLocalizacao(){return localizacao;} public void setLocalizacao(String v){localizacao=v;} public String getObservacoes(){return observacoes;} public void setObservacoes(String v){observacoes=v;}
}
