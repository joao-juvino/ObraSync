package com.obrasync.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Ponto de entrada e configuração da aplicação JAX-RS.
 * Mapeia todos os recursos e endpoints REST sob o caminho base '/api'.
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
}
