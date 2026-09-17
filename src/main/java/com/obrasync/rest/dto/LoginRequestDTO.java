package com.obrasync.rest.dto;

/**
 * DTO para receber credenciais de autenticacao no endpoint POST /api/auth/login.
 */
public class LoginRequestDTO {

    private String email;
    private String senha;

    public LoginRequestDTO() {}

    public LoginRequestDTO(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}