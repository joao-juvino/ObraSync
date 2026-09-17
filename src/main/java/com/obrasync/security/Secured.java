package com.obrasync.security;

import javax.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotacao JAX-RS @NameBinding usada para marcar endpoints REST que
 * exigem autenticacao via token JWT.
 *
 * Uso:
 *   - Aplique em uma classe de recurso para proteger TODOS os seus endpoints.
 *   - Aplique em um metodo especifico para proteger apenas aquele endpoint.
 *
 * Exemplo:
 *   @Secured
 *   @Path("/vistorias")
 *   public class VistoriaRestResource { ... }
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Secured {
}