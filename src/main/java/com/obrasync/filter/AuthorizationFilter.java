package com.obrasync.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filtro Servlet que protege todas as paginas XHTML da aplicacao.
 *
 * Regras de acesso:
 *   - Paginas publicas (nao requerem login): login.xhtml, recursos JSF, endpoints REST
 *   - Todas as demais paginas XHTML exigem o atributo "usuarioLogado" na HttpSession
 *   - Acessos nao autenticados sao redirecionados para /login.xhtml
 */
@WebFilter(urlPatterns = "/*")
public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq  = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String uri = httpReq.getRequestURI();
        String ctx = httpReq.getContextPath();

        // Recursos que nao precisam de autenticacao
        if (isRecursoPublico(uri, ctx)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpReq.getSession(false);
        boolean autenticado = session != null && session.getAttribute("usuarioLogado") != null;

        if (autenticado) {
            chain.doFilter(request, response);
        } else {
            httpResp.sendRedirect(ctx + "/login.xhtml");
        }
    }

    /**
     * Define quais URIs sao acessiveis sem autenticacao.
     */
    private boolean isRecursoPublico(String uri, String ctx) {
        // Tela de login
        if (uri.equals(ctx + "/login.xhtml")) return true;

        // Recursos estaticos do JSF/PrimeFaces (CSS, JS, imagens)
        if (uri.contains("/javax.faces.resource/")) return true;

        // Endpoints REST (autenticados via JWT, nao via sessao)
        if (uri.startsWith(ctx + "/api/")) return true;

        // Favicon e recursos estaticos da webapp
        if (uri.endsWith(".css") || uri.endsWith(".js") ||
            uri.endsWith(".png") || uri.endsWith(".ico")) return true;

        return false;
    }

    @Override public void init(FilterConfig filterConfig) {}
    @Override public void destroy() {}
}