package com.obrasync.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;

class EvidenciaStorageServiceTest {
    @TempDir Path diretorio;
    public static byte[] imagem(String formato) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), formato, out);
        return out.toByteArray();
    }
    @Test @DisplayName("Armazena JPEG/PNG, gera UUID e permite leitura e exclusão")
    void uploadValido() throws Exception {
        EvidenciaStorageService storage = new EvidenciaStorageService(diretorio);
        for (String formato : new String[]{"png", "jpeg"}) {
            byte[] bytes = imagem(formato);
            String nome = storage.salvar(bytes, "image/" + formato);
            assertTrue(nome.matches("[a-f0-9-]{36}\\.(png|jpg)"));
            assertArrayEquals(bytes, storage.ler(nome));
            storage.excluir(nome); storage.excluir(nome);
            assertEquals(404, assertThrows(EvidenciaException.class, () -> storage.ler(nome)).getStatus());
        }
    }
    @Test @DisplayName("Rejeita MIME, conteúdo e tamanho inválidos sem criar arquivos")
    void invalidos() throws Exception {
        EvidenciaStorageService storage = new EvidenciaStorageService(diretorio);
        assertEquals(415, assertThrows(EvidenciaException.class, () -> storage.salvar(imagem("png"), "text/html")).getStatus());
        assertEquals(415, assertThrows(EvidenciaException.class, () -> storage.salvar(imagem("png"), "image/jpeg")).getStatus());
        assertEquals(415, assertThrows(EvidenciaException.class, () -> storage.salvar("não é imagem".getBytes(), "image/png")).getStatus());
        assertEquals(400, assertThrows(EvidenciaException.class, () -> storage.salvar(new byte[0], "image/png")).getStatus());
        assertEquals(413, assertThrows(EvidenciaException.class, () -> storage.salvar(new byte[EvidenciaStorageService.MAX + 1], "image/png")).getStatus());
        assertEquals(413, assertThrows(EvidenciaException.class, () -> storage.receber(new ByteArrayInputStream(new byte[EvidenciaStorageService.MAX + 1]))).getStatus());
        try (java.util.stream.Stream<Path> files = Files.list(diretorio)) { assertEquals(0, files.count()); }
    }
    @Test @DisplayName("Não resolve nomes externos e restaura arquivo em rollback")
    void caminhos() throws Exception {
        EvidenciaStorageService storage = new EvidenciaStorageService(diretorio);
        assertThrows(EvidenciaException.class, () -> storage.ler("../segredo"));
        assertThrows(EvidenciaException.class, () -> storage.excluir("C:\\segredo"));
        String nome = storage.salvar(imagem("png"), "image/png");
        storage.mover(nome, nome + ".deleted");
        assertFalse(Files.exists(diretorio.resolve(nome)));
        storage.mover(nome + ".deleted", nome);
        assertTrue(Files.exists(diretorio.resolve(nome)));
    }
}
