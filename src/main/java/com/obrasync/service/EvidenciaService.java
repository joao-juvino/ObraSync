package com.obrasync.service;

import com.obrasync.model.*;
import com.obrasync.security.AccessPolicy;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.*;
import javax.annotation.Resource;
import javax.transaction.*;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class EvidenciaService {
    @PersistenceContext(unitName="obrasyncPU") private EntityManager em;
    @Inject private EvidenciaStorageService storage;
    @Inject private AccessPolicy acesso;
    @Resource private TransactionSynchronizationRegistry transacoes;

    public List<EvidenciaVistoria> listar(Long vistoriaId) {
        acesso.consultar();
        vistoria(vistoriaId);
        return em.createQuery("SELECT e FROM EvidenciaVistoria e WHERE e.vistoria.id = :id ORDER BY e.id", EvidenciaVistoria.class)
                .setParameter("id", vistoriaId).getResultList();
    }
    public EvidenciaVistoria buscar(Long vistoriaId, Long evidenciaId) {
        acesso.consultar();
        EvidenciaVistoria evidencia = em.find(EvidenciaVistoria.class, evidenciaId);
        if (evidencia == null || !evidencia.getVistoria().getId().equals(vistoriaId))
            throw new EvidenciaException(404, "Evidência não encontrada nesta vistoria.");
        return evidencia;
    }
    public byte[] conteudo(Long vistoriaId, Long evidenciaId) {
        return storage.ler(buscar(vistoriaId, evidenciaId).getCaminhoOuIdentificador());
    }
    public EvidenciaVistoria salvar(Long vistoriaId, byte[] dados, String mime, String nome, String descricao) {
        Vistoria vistoria = vistoria(vistoriaId);
        acesso.editar(vistoria);
        if (descricao != null && descricao.length() > 500) throw new EvidenciaException(400, "Descrição excede 500 caracteres.");
        String identificador = storage.salvar(dados, mime);
        try {
            sincronizar(() -> {}, () -> storage.excluir(identificador));
            EvidenciaVistoria evidencia = new EvidenciaVistoria();
            evidencia.setVistoria(vistoria);
            // Nome é somente metadado, sem controles ou separadores de caminho.
            String seguro = nome == null ? "evidencia" : nome.replaceAll("[\\p{Cntrl}\\\\/]", "_");
            evidencia.setNomeArquivo(seguro.substring(0, Math.min(seguro.length(), 255)));
            evidencia.setTipoMime(mime);
            evidencia.setTamanho(dados.length);
            evidencia.setCaminhoOuIdentificador(identificador);
            evidencia.setDescricao(descricao);
            em.persist(evidencia);
            em.flush();
            return evidencia;
        } catch (RuntimeException e) { storage.excluir(identificador); throw e; }
    }
    public void excluir(Long vistoriaId, Long evidenciaId) {
        EvidenciaVistoria evidencia = buscar(vistoriaId, evidenciaId);
        acesso.editar(evidencia.getVistoria());
        prepararRemocao(evidencia);
        em.remove(evidencia);
        em.flush();
    }
    /** Participa da transação que exclui a vistoria e seus metadados em cascata. */
    public void removerArquivosDaVistoria(Long id) {
        acesso.administrar();
        for (EvidenciaVistoria evidencia : listar(id)) prepararRemocao(evidencia);
    }
    private void prepararRemocao(EvidenciaVistoria evidencia) {
        String nome = evidencia.getCaminhoOuIdentificador(), temporario = nome + ".deleted";
        sincronizar(() -> storage.excluir(temporario), () -> storage.mover(temporario, nome));
        storage.mover(nome, temporario);
    }
    private void sincronizar(Runnable commit, Runnable rollback) {
        transacoes.registerInterposedSynchronization(new Synchronization() {
            public void beforeCompletion() {}
            public void afterCompletion(int status) {
                try { if (status == Status.STATUS_COMMITTED) commit.run(); else rollback.run(); }
                catch (RuntimeException e) { Logger.getLogger(EvidenciaService.class.getName()).severe("Falha na compensação de arquivo de evidência; verificar storage."); }
            }
        });
    }
    private Vistoria vistoria(Long id) {
        Vistoria v = id == null ? null : em.find(Vistoria.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (v == null) throw new EvidenciaException(404, "Vistoria não encontrada.");
        return v;
    }
    public void setEntityManager(EntityManager em) { this.em = em; }
    public void setStorage(EvidenciaStorageService storage) { this.storage = storage; }
    public void setAcesso(AccessPolicy acesso) { this.acesso = acesso; }
    public void setTransacoes(TransactionSynchronizationRegistry transacoes) { this.transacoes = transacoes; }
}
