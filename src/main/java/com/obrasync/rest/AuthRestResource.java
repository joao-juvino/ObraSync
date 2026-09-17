package com.obrasync.rest;

import com.obrasync.model.Usuario;
import com.obrasync.rest.dto.LoginRequestDTO;
import com.obrasync.rest.dto.LoginResponseDTO;
import com.obrasync.rest.dto.MensagemErroDTO;
import com.obrasync.security.JwtService;
import com.obrasync.service.UsuarioService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Endpoint REST de autenticacao.
 *
 * POST /api/auth/login
 *   - Recebe { "email": "...", "senha": "..." }
 *   - Verifica credenciais via UsuarioService (BCrypt)
 *   - Retorna 200 OK com token JWT ou 401 Unauthorized
 *
 * Este endpoint e publico — nao usa @Secured.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class AuthRestResource {

    @Inject
    private UsuarioService usuarioService;

    @Inject
    private JwtService jwtService;

    /**
     * Autentica o usuario e retorna um token JWT valido por 8 horas.
     *
     * Respostas:
     *   200 OK          – credenciais validas; corpo contem LoginResponseDTO com o token
     *   400 Bad Request – payload nulo ou campos obrigatorios ausentes
     *   401 Unauthorized – e-mail nao encontrado ou senha incorreta
     */
    @POST
    @Path("/login")
    public Response login(LoginRequestDTO request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MensagemErroDTO(400, "O payload de autenticacao nao pode ser nulo."))
                    .build();
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MensagemErroDTO(400, "O campo 'email' e obrigatorio."))
                    .build();
        }

        if (request.getSenha() == null || request.getSenha().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MensagemErroDTO(400, "O campo 'senha' e obrigatorio."))
                    .build();
        }

        Usuario autenticado = usuarioService.autenticar(
                request.getEmail().trim(),
                request.getSenha()
        );

        if (autenticado == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new MensagemErroDTO(401, "E-mail ou senha invalidos."))
                    .build();
        }

        String token = jwtService.gerarToken(
                autenticado.getEmail(),
                autenticado.getNome(),
                autenticado.getPerfil().name()
        );

        return Response.ok(new LoginResponseDTO(
                token,
                autenticado.getNome(),
                autenticado.getPerfil().name()
        )).build();
    }

    // Setters para injecao manual em testes
    public void setUsuarioService(UsuarioService svc) { this.usuarioService = svc; }
    public void setJwtService(JwtService svc)         { this.jwtService = svc; }
}