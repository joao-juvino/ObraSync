package com.obrasync.security;

import com.obrasync.model.Perfil;
import com.obrasync.model.Usuario;
import com.obrasync.model.Vistoria;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/** Mesma política para chamadas JSF e REST, aplicada pelos serviços. */
@RequestScoped
@javax.inject.Named("accessPolicy")
public class AccessPolicy {
    public static final String JWT_USER = "obrasync.jwt.user";
    @Inject private HttpServletRequest request;

    public Usuario usuario() {
        Object authenticated = request.getAttribute(JWT_USER);
        if (authenticated instanceof Usuario) return (Usuario) authenticated;
        HttpSession session = request.getSession(false);
        return session == null ? null : (Usuario) session.getAttribute("usuarioLogado");
    }

    public void consultar() {
        if (usuario() == null || usuario().getPerfil() == null) throw new AcessoNegadoException();
    }

    public void administrar() {
        if (!isAdmin()) throw new AcessoNegadoException();
    }

    public boolean isAdmin() { return usuario() != null && usuario().getPerfil() == Perfil.ADMIN; }
    public boolean isEngenheiro() { return usuario() != null && usuario().getPerfil() == Perfil.ENGENHEIRO; }
    public boolean isFiscal() { return usuario() != null && usuario().getPerfil() == Perfil.FISCAL; }
    public boolean isPodeCriar() {
        return isAdmin() || isEngenheiro();
    }
    public boolean isPodeAlterarStatus() { return isAdmin() || isFiscal(); }
    public boolean isPodeExcluir() { return isAdmin(); }
    public boolean isPodeGerarLaudo() { return usuario() != null; }
    public boolean podeEditar(Vistoria vistoria) {
        return isAdmin() || (isPodeCriar() && vistoria != null && vistoria.getResponsavel() != null
                && usuario().getId() != null && usuario().getId().equals(vistoria.getResponsavel().getId()));
    }
    public void editar(Vistoria vistoria) {
        if (!podeEditar(vistoria)) throw new AcessoNegadoException();
    }
    public void fiscalizar() {
        if (!isPodeAlterarStatus())
            throw new AcessoNegadoException();
    }
    public void setRequest(HttpServletRequest request) { this.request = request; }
}
