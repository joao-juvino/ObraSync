package com.obrasync.service;

import com.obrasync.model.Perfil;
import com.obrasync.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

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
        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            throw new IllegalArgumentException("A senha é obrigatória.");
        }
        if (!usuario.getSenha().startsWith("$2a$") && !usuario.getSenha().startsWith("$2b$")) {
            usuario.setSenha(BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt(12)));
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

    /**
     * Autentica um usuario verificando email e hash BCrypt da senha.
     *
     * @param email email do usuario (case-insensitive)
     * @param senha senha em texto puro a ser verificada contra o hash armazenado
     * @return o Usuario autenticado, ou null se as credenciais forem invalidas
     */
    public Usuario autenticar(String email, String senha) {
        if (email == null || email.trim().isEmpty() || senha == null || senha.isEmpty()) {
            return null;
        }
        Usuario usuario = buscarPorEmail(email);
        if (usuario == null) {
            return null;
        }
        try {
            if (BCrypt.checkpw(senha, usuario.getSenha())) {
                return usuario;
            }
        } catch (IllegalArgumentException e) {
            // Hash armazenado com formato invalido — log e retorna null
        }
        return null;
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }
}
