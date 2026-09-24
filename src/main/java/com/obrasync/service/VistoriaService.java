package com.obrasync.service;

import com.obrasync.model.StatusVistoria;
import com.obrasync.model.Obra;
import com.obrasync.model.Usuario;
import com.obrasync.model.Vistoria;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviço EJB para gerenciamento das vistorias persistidas no PostgreSQL.
 */
@Stateless
public class VistoriaService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "obrasyncPU")
    private EntityManager em;
    @javax.inject.Inject private com.obrasync.security.AccessPolicy acesso;
    @javax.inject.Inject private EvidenciaService evidencias;

    public List<Vistoria> listarTodas() {
        return em.createQuery("SELECT v FROM Vistoria v "
                        + "JOIN FETCH v.obra JOIN FETCH v.responsavel ORDER BY v.id DESC", Vistoria.class)
                .getResultList();
    }

    /**
     * Retorna a fila adequada ao perfil autenticado. Administradores e fiscais
     * consultam o conjunto completo; engenheiros veem somente as vistorias sob
     * sua responsabilidade.
     */
    public List<Vistoria> listarVisiveis() {
        acesso.consultar();
        Usuario usuario = acesso.usuario();
        if (usuario != null && usuario.getPerfil() == com.obrasync.model.Perfil.ENGENHEIRO) {
            return em.createQuery("SELECT v FROM Vistoria v "
                            + "JOIN FETCH v.obra JOIN FETCH v.responsavel r "
                            + "WHERE r.id = :responsavelId ORDER BY v.id DESC", Vistoria.class)
                    .setParameter("responsavelId", usuario.getId())
                    .getResultList();
        }
        return listarTodas();
    }

    public PaginaResultado<Vistoria> pesquisar(StatusVistoria status, com.obrasync.model.TipoVistoria tipo,
            String obra, String responsavel, LocalDate dataInicial, LocalDate dataFinal, int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE
                || (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)))
            throw new IllegalArgumentException("Paginação ou período inválidos.");
        StringBuilder where = new StringBuilder(" WHERE 1=1"); Map<String,Object> params = new HashMap<>();
        if(status!=null){where.append(" AND v.status=:status");params.put("status",status);} if(tipo!=null){where.append(" AND v.tipo=:tipo");params.put("tipo",tipo);}
        if(obra!=null&&!obra.isBlank()){where.append(" AND LOWER(o.nome) LIKE :obra");params.put("obra","%"+obra.toLowerCase()+"%");}
        if(responsavel!=null&&!responsavel.isBlank()){where.append(" AND LOWER(r.nome) LIKE :responsavel");params.put("responsavel","%"+responsavel.toLowerCase()+"%");}
        if(dataInicial!=null){where.append(" AND v.dataVistoria>=:dataInicial");params.put("dataInicial",dataInicial);} if(dataFinal!=null){where.append(" AND v.dataVistoria<=:dataFinal");params.put("dataFinal",dataFinal);}
        javax.persistence.TypedQuery<Vistoria> q=em.createQuery("SELECT v FROM Vistoria v JOIN FETCH v.obra o JOIN FETCH v.responsavel r"+where+" ORDER BY v.id DESC",Vistoria.class);
        javax.persistence.TypedQuery<Long> c=em.createQuery("SELECT COUNT(v) FROM Vistoria v JOIN v.obra o JOIN v.responsavel r"+where,Long.class);
        params.forEach((k,v)->{q.setParameter(k,v);c.setParameter(k,v);}); q.setFirstResult(page*size).setMaxResults(size);
        return new PaginaResultado<>(q.getResultList(),c.getSingleResult());
    }

    public Vistoria buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return em.createQuery("SELECT v FROM Vistoria v "
                            + "JOIN FETCH v.obra JOIN FETCH v.responsavel WHERE v.id = :id", Vistoria.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Vistoria salvar(Vistoria vistoria) {
        if (vistoria == null) {
            throw new IllegalArgumentException("A vistoria não pode ser nula.");
        }
        if (vistoria.getId() == null) {
            acesso.editar(vistoria);
            associarRelacionamentosGerenciados(vistoria);
            em.persist(vistoria);
            return vistoria;
        }
        Vistoria atual = em.find(Vistoria.class, vistoria.getId());
        if (atual == null) throw new IllegalArgumentException("Vistoria não encontrada.");
        acesso.editar(atual);
        acesso.editar(vistoria);
        associarRelacionamentosGerenciados(vistoria);
        // Atualiza somente os campos editáveis; preserva a coleção persistente de evidências.
        atual.setObra(vistoria.getObra());
        atual.setResponsavel(vistoria.getResponsavel());
        atual.setTipo(vistoria.getTipo());
        atual.setStatus(vistoria.getStatus());
        atual.setDataVistoria(vistoria.getDataVistoria());
        atual.setLocalizacao(vistoria.getLocalizacao());
        atual.setObservacoes(vistoria.getObservacoes());
        return atual;
    }

    public Vistoria atualizarStatus(Long id, StatusVistoria status) {
        acesso.fiscalizar();
        if (status == null) throw new IllegalArgumentException("Status obrigatório.");
        Vistoria atual = buscarPorId(id);
        if (atual == null) throw new EvidenciaException(404, "Vistoria não encontrada.");
        atual.setStatus(status);
        return atual;
    }

    public boolean excluir(Long id) {
        acesso.administrar();
        if (id != null) {
            Vistoria vistoria = em.find(Vistoria.class, id);
            if (vistoria != null) {
                evidencias.removerArquivosDaVistoria(id);
                em.remove(vistoria);
                return true;
            }
        }
        return false;
    }

    public long contarTotal() {
        return em.createQuery("SELECT COUNT(v) FROM Vistoria v", Long.class).getSingleResult();
    }

    public long contarAprovadas() {
        return contarPorStatus(StatusVistoria.APROVADA);
    }

    public long contarPendentes() {
        return contarPorStatus(StatusVistoria.PENDENTE);
    }

    public long contarReprovadas() {
        return contarPorStatus(StatusVistoria.REPROVADA);
    }

    public double calcularTaxaAprovacao() {
        long total = contarTotal();
        if (total == 0) {
            return 0.0;
        }
        return ((double) contarAprovadas() / total) * 100.0;
    }

    private void associarRelacionamentosGerenciados(Vistoria vistoria) {
        if (vistoria.getObra() == null || vistoria.getObra().getId() == null) {
            throw new IllegalArgumentException("A vistoria deve estar associada a uma obra persistida.");
        }
        if (vistoria.getResponsavel() == null || vistoria.getResponsavel().getId() == null) {
            throw new IllegalArgumentException("A vistoria deve estar associada a um responsável persistido.");
        }
        Obra obra = em.find(Obra.class, vistoria.getObra().getId());
        Usuario usuario = em.find(Usuario.class, vistoria.getResponsavel().getId());
        if (obra == null || usuario == null) throw new IllegalArgumentException("Obra ou responsável não encontrado.");
        if (vistoria.getTipo() == null || vistoria.getStatus() == null || vistoria.getDataVistoria() == null)
            throw new IllegalArgumentException("Tipo, status e data são obrigatórios.");
        if (vistoria.getLocalizacao() != null && vistoria.getLocalizacao().length() > 150)
            throw new IllegalArgumentException("Localização excede 150 caracteres.");
        vistoria.setObra(obra);
        vistoria.setResponsavel(usuario);
    }

    private long contarPorStatus(StatusVistoria status) {
        return em.createQuery("SELECT COUNT(v) FROM Vistoria v WHERE v.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }
    public void setAcesso(com.obrasync.security.AccessPolicy acesso) { this.acesso = acesso; }
    public void setEvidencias(EvidenciaService evidencias) { this.evidencias = evidencias; }
}
