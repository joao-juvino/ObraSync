package com.obrasync.rest.dto;

/**
 * DTO retornado pelo endpoint POST /api/auth/login em caso de autenticacao bem-sucedida.
 *
 * Campos:
 *   - token : JWT assinado a ser enviado no header "Authorization: Bearer <token>"
 *   - tipo  : sempre "Bearer"
 *   - nome  : nome do usuario para exibicao no cliente
 *   - perfil: perfil de acesso (ADMIN ou ENGENHEIRO)
 */
public class LoginResponseDTO {

    private String token;
    private String tipo;
    private String nome;
    private String perfil;

    public LoginResponseDTO() {}

    public LoginResponseDTO(String token, String nome, String perfil) {
        this.token  = token;
        this.tipo   = "Bearer";
        this.nome   = nome;
        this.perfil = perfil;
    }

    public String getToken()  { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTipo()   { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getNome()   { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
}