package com.obrasync.rest;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;

@OpenAPIDefinition(info = @Info(title = "ObraSync API", version = "1.0.0",
        description = "API para gerenciamento de vistorias de obras.", contact = @Contact(name = "ObraSync")),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(securitySchemeName = "bearerAuth", type = SecuritySchemeType.HTTP,
        scheme = "bearer", bearerFormat = "JWT", description = "Token JWT obtido no endpoint de autenticação.")
public class OpenApiDefinition {
}
