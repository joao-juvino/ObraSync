package com.obrasync.controller;

import com.obrasync.model.Usuario;
import com.obrasync.service.UsuarioService;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.Serializable;

/**
 * Controller CDI de sessao responsavel pela autenticacao de usuarios via JSF.
 *
 * Gerencia o ciclo de vida da sessao web:
 *   - login()  : autentica com UsuarioService (BCrypt) e salva usuario na sessao
 *   - logout() : invalida a sessao HTTP e redireciona para login.xhtml
 */
@Named
@SessionScoped
public class AuthBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private UsuarioService usuarioService;

    private String email;
    private String senha;
    private Usuario usuarioLogado;

    /**
     * Executa o processo de autenticacao:
     *   1. Valida credenciais via UsuarioService (BCrypt)
     *   2. Salva o usuario autenticado na sessao HTTP
     *   3. Redireciona para o dashboard em caso de sucesso
     */
    public String login() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (email == null || email.trim().isEmpty() || senha == null || senha.isEmpty()) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN, "Atencao", "Informe e-mail e senha."));
            return null;
        }

        Usuario autenticado = usuarioService.autenticar(email.trim(), senha);

        if (autenticado == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Acesso negado", "E-mail ou senha invalidos."));
            senha = null;
            return null;
        }

        // Salva o usuario na sessao para o AuthorizationFilter verificar
        usuarioLogado = autenticado;
        HttpSession session = (HttpSession) ctx.getExternalContext().getSession(true);
        session.setAttribute("usuarioLogado", usuarioLogado);

        senha = null;
        return "/vistorias.xhtml?faces-redirect=true";
    }

    /**
     * Invalida a sessao HTTP e redireciona para a tela de login.
     */
    public String logout() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) ctx.getExternalContext().getSession(false);
        if (session != null) {
            session.invalidate();
        }
        usuarioLogado = null;
        email = null;
        senha = null;
        return "/login.xhtml?faces-redirect=true";
    }

    /** Retorna true se ha um usuario autenticado na sessao atual. */
    public boolean isAutenticado() {
        return usuarioLogado != null;
    }

    // -----------------------------------------------------------------------
    // Getters e Setters
    // -----------------------------------------------------------------------
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public Usuario getUsuarioLogado() { return usuarioLogado; }
    public void setUsuarioLogado(Usuario usuarioLogado) { this.usuarioLogado = usuarioLogado; }

    // Setter para injecao manual em testes unitarios
    public void setUsuarioService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
}