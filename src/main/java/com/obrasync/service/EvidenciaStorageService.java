package com.obrasync.service;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.nio.file.*;
import java.util.Iterator;
import java.util.UUID;

/** Arquivos imutáveis identificados por UUID; nunca resolve nomes enviados pelo cliente. */
@ApplicationScoped
public class EvidenciaStorageService {
    public static final int MAX = 10 * 1024 * 1024;
    private Path raiz;
    public EvidenciaStorageService() {
        this(Paths.get(System.getenv().getOrDefault("OBRASYNC_UPLOAD_DIR",
                System.getProperty("java.io.tmpdir") + "/obrasync-evidencias")));
    }
    public EvidenciaStorageService(Path raiz) { this.raiz = raiz.toAbsolutePath().normalize(); }

    public byte[] receber(InputStream stream) {
        try {
            byte[] dados = stream.readNBytes(MAX + 1);
            if (dados.length > MAX) throw new EvidenciaException(413, "Limite de 10 MB excedido.");
            return dados;
        } catch (IOException e) { throw new EvidenciaException(400, "Não foi possível ler a imagem."); }
    }

    public String salvar(byte[] dados, String mime) {
        validar(dados, mime);
        String nome = UUID.randomUUID() + ("image/png".equals(mime) ? ".png" : ".jpg");
        try {
            Files.createDirectories(raiz);
            Files.write(caminho(nome), dados, StandardOpenOption.CREATE_NEW);
            return nome;
        } catch (IOException e) { throw new EvidenciaException(500, "Armazenamento de evidências indisponível."); }
    }

    public void validar(byte[] dados, String mime) {
        if (dados == null || dados.length == 0) throw new EvidenciaException(400, "Envie uma imagem não vazia.");
        if (dados.length > MAX) throw new EvidenciaException(413, "Limite de 10 MB excedido.");
        if (!"image/jpeg".equals(mime) && !"image/png".equals(mime))
            throw new EvidenciaException(415, "Somente JPEG e PNG são permitidos.");
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(dados))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new EvidenciaException(415, "Conteúdo de imagem inválido.");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String formato = reader.getFormatName();
                if (!("image/png".equals(mime) ? "png".equalsIgnoreCase(formato) : "jpeg".equalsIgnoreCase(formato)))
                    throw new EvidenciaException(415, "Tipo declarado difere do conteúdo.");
                if ((long) reader.getWidth(0) * reader.getHeight(0) > 20_000_000L)
                    throw new EvidenciaException(413, "Imagem excede 20 megapixels.");
                reader.read(0); // Rejeita imagens truncadas antes de persistir.
            } finally { reader.dispose(); }
        } catch (IOException | IllegalArgumentException e) { throw new EvidenciaException(415, "Conteúdo de imagem inválido."); }
    }

    public byte[] ler(String nome) {
        try { return Files.readAllBytes(caminho(nome)); }
        catch (IOException e) { throw new EvidenciaException(404, "Arquivo de evidência não encontrado."); }
    }
    public void excluir(String nome) {
        try { Files.deleteIfExists(caminho(nome)); }
        catch (IOException e) { throw new EvidenciaException(500, "Não foi possível remover o arquivo."); }
    }
    public void mover(String origem, String destino) {
        try { if (Files.exists(caminho(origem))) Files.move(caminho(origem), caminho(destino)); }
        catch (IOException e) { throw new EvidenciaException(500, "Não foi possível preparar a remoção do arquivo."); }
    }
    private Path caminho(String nome) {
        if (nome == null || !nome.matches("[a-f0-9-]{36}\\.(png|jpg)(\\.deleted)?"))
            throw new EvidenciaException(400, "Identificador de evidência inválido.");
        Path destino = raiz.resolve(nome).normalize();
        if (!destino.startsWith(raiz) || Files.isSymbolicLink(destino))
            throw new EvidenciaException(400, "Caminho de evidência inválido.");
        return destino;
    }
}
