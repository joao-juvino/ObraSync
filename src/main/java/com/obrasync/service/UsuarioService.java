package com.obrasync.service;

import com.obrasync.model.Perfil;
import com.obrasync.model.Usuario;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

/**
 * Serviço de negócio EJB Stateless para gerenciamento de Usuários.
 * Fornece operações de persistência e consultas por perfil e email via JPA EntityManager.
 */
@Stateless
public class UsuarioService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "obrasyncPU")
    private EntityManager em;

    /**
     * Persiste ou atualiza um Usuário no banco de dados.
     *
     * @param usuario Entidade a ser salva
     * @return Entidade persistida gerenciada pelo JPA
     */
    public Usuario salvar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("O usuário não pode ser nulo.");
        }
        if (usuario.getEmail() != null) {
            usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        }
        if (usuario.getId() == null) {
            em.persist(usuario);
            return usuario;
        } else {
            return em.merge(usuario);
        }
    }

    /**
     * Retorna todos os usuários cadastrados ordenados por nome.
     */
    public List<Usuario> listarTodos() {
        return em.createQuery("SELECT u FROM Usuario u ORDER BY u.nome ASC", Usuario.class)
                .getResultList();
    }

    /**
     * Busca um usuário pelo seu identificador primário.
     */
    public Usuario buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(Usuario.class, id);
    }

    /**
     * Busca um usuário único através do seu endereço de email.
     */
    public Usuario buscarPorEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class)
                    .setParameter("email", email.trim().toLowerCase())
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Lista usuários filtrando pelo perfil de acesso (ex: ENGENHEIRO ou ADMIN).
     */
    public List<Usuario> listarPorPerfil(Perfil perfil) {
        if (perfil == null) {
            return listarTodos();
        }
        return em.createQuery("SELECT u FROM Usuario u WHERE u.perfil = :perfil ORDER BY u.nome ASC", Usuario.class)
                .setParameter("perfil", perfil)
                .getResultList();
    }

    /**
     * Remove um usuário do banco de dados pelo seu identificador.
     */
    public void excluir(Long id) {
        if (id != null) {
            Usuario usuario = buscarPorId(id);
            if (usuario != null) {
                em.remove(usuario);
            }
        }
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }
}
