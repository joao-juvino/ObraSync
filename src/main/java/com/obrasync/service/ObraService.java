package com.obrasync.service;

import com.obrasync.model.Obra;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

/**
 * Serviço de negócio EJB Stateless para gerenciamento de Obras.
 * Utiliza o EntityManager injetado pelo container Java EE para persistência JPA.
 */
@Stateless
public class ObraService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "obrasyncPU")
    private EntityManager em;

    /**
     * Persiste ou atualiza uma Obra no banco de dados.
     *
     * @param obra Entidade a ser salva
     * @return Entidade persistida gerenciada pelo JPA
     */
    public Obra salvar(Obra obra) {
        if (obra == null) {
            throw new IllegalArgumentException("A obra não pode ser nula.");
        }
        if (obra.getId() == null) {
            em.persist(obra);
            return obra;
        } else {
            return em.merge(obra);
        }
    }

    /**
     * Retorna todas as obras cadastradas ordenadas por nome.
     */
    public List<Obra> listarTodas() {
        return em.createQuery("SELECT o FROM Obra o ORDER BY o.nome ASC", Obra.class)
                .getResultList();
    }

    /**
     * Busca uma obra pelo seu identificador primário.
     */
    public Obra buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(Obra.class, id);
    }

    /**
     * Busca uma obra carregando antecipadamente (JOIN FETCH) suas vistorias relacionadas.
     */
    public Obra buscarComVistorias(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return em.createQuery("SELECT o FROM Obra o LEFT JOIN FETCH o.vistorias WHERE o.id = :id", Obra.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Remove uma obra do banco de dados pelo seu identificador.
     */
    public void excluir(Long id) {
        if (id != null) {
            Obra obra = buscarPorId(id);
            if (obra != null) {
                em.remove(obra);
            }
        }
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }
}
