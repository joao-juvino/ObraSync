package com.obrasync.rest;

import com.obrasync.model.Perfil;
import com.obrasync.model.Usuario;
import com.obrasync.rest.dto.LoginRequestDTO;
import com.obrasync.rest.dto.LoginResponseDTO;
import com.obrasync.service.UsuarioService;
import com.obrasync.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.ws.rs.core.Response;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthRestResourceTest {
 @Mock UsuarioService usuarios; @Mock JwtService jwt;
 private AuthRestResource recurso(){AuthRestResource r=new AuthRestResource();r.setUsuarioService(usuarios);r.setJwtService(jwt);return r;}
 @Test void loginValidoRetornaToken(){Usuario u=new Usuario(1L,"Admin","admin@teste","hash", Perfil.ADMIN);when(usuarios.autenticar("admin@teste","senha")).thenReturn(u);when(jwt.gerarToken(any(),any(),any())).thenReturn("token");Response r=recurso().login(new LoginRequestDTO("admin@teste","senha"));assertEquals(200,r.getStatus());assertTrue(r.getEntity() instanceof LoginResponseDTO);}
 @Test void loginInvalidoRetorna401(){when(usuarios.autenticar(any(),any())).thenReturn(null);assertEquals(401,recurso().login(new LoginRequestDTO("x@teste","errada")).getStatus());}
 @Test void payloadAusenteRetorna400(){assertEquals(400,recurso().login(null).getStatus());}
}
