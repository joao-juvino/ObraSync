package com.obrasync.service;
import java.io.IOException; import java.nio.file.*; import java.util.Set; import java.util.UUID;
public class EvidenciaStorageService {
 private static final Set<String> TIPOS=Set.of("image/jpeg","image/png"); private static final long MAX=10*1024*1024L;
 public String salvar(byte[] dados,String mime) throws IOException { if(dados==null||dados.length==0||dados.length>MAX||!TIPOS.contains(mime)) throw new IllegalArgumentException("Evidência inválida."); Path raiz=Paths.get(System.getenv().getOrDefault("OBRASYNC_UPLOAD_DIR",System.getProperty("java.io.tmpdir")+"/obrasync-evidencias")).toAbsolutePath().normalize(); Files.createDirectories(raiz); String nome=UUID.randomUUID()+("image/png".equals(mime)?".png":".jpg"); Path destino=raiz.resolve(nome).normalize(); if(!destino.startsWith(raiz)) throw new SecurityException("Caminho inválido."); Files.write(destino,dados,StandardOpenOption.CREATE_NEW); return nome; }
}
