package com.obrasync.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET = "unit-test-only-key-with-at-least-32-bytes";
    @Test @DisplayName("Configuração ausente ou curta falha explicitamente")
    void configuracao() {
        assertThrows(IllegalStateException.class, () -> new JwtService((String) null));
        assertThrows(IllegalStateException.class, () -> new JwtService("curta"));
    }
    @Test @DisplayName("JWT válido mantém subject e perfil; adulteração é rejeitada")
    void assinatura() {
        JwtService service = new JwtService(SECRET);
        String token = service.gerarToken("admin@teste", "Admin", "ADMIN");
        Claims claims = service.validarToken(token);
        assertEquals("ADMIN", claims.get("perfil")); assertEquals("admin@teste", service.extrairEmail(token));
        assertTrue(service.isTokenValido(token));
        assertFalse(new JwtService(SECRET + "outro").isTokenValido(token));
        assertNull(service.extrairEmail("inválido")); assertFalse(service.isTokenValido(null));
        assertTrue(claims.getExpiration().after(new Date()));
    }
    @Test @DisplayName("Token expirado é rejeitado")
    void expirado() {
        String token = Jwts.builder().setSubject("a@b").setExpiration(new Date(1))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        assertFalse(new JwtService(SECRET).isTokenValido(token));
    }
}
