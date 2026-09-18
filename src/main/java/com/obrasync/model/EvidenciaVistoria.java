package com.obrasync.model;
import javax.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="tb_evidencia_vistoria") public class EvidenciaVistoria {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="vistoria_id",nullable=false) private Vistoria vistoria;
 @Column(name="nome_arquivo", nullable=false,length=255) private String nomeArquivo; @Column(name="tipo_mime", nullable=false,length=100) private String tipoMime; @Column(nullable=false) private long tamanho;
 @Column(name="caminho_ou_identificador", nullable=false,unique=true,length=255) private String caminhoOuIdentificador; @Column(length=500) private String descricao; @Column(name="data_upload", nullable=false) private LocalDateTime dataUpload=LocalDateTime.now();
 public Long getId(){return id;} public void setId(Long v){id=v;} public Vistoria getVistoria(){return vistoria;} public void setVistoria(Vistoria v){vistoria=v;} public String getNomeArquivo(){return nomeArquivo;} public void setNomeArquivo(String v){nomeArquivo=v;} public String getTipoMime(){return tipoMime;} public void setTipoMime(String v){tipoMime=v;} public long getTamanho(){return tamanho;} public void setTamanho(long v){tamanho=v;} public String getCaminhoOuIdentificador(){return caminhoOuIdentificador;} public void setCaminhoOuIdentificador(String v){caminhoOuIdentificador=v;} public String getDescricao(){return descricao;} public void setDescricao(String v){descricao=v;} public LocalDateTime getDataUpload(){return dataUpload;} public void setDataUpload(LocalDateTime v){dataUpload=v;}
}
