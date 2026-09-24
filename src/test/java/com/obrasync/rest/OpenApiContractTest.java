package com.obrasync.rest;

import com.fasterxml.jackson.databind.*;
import javax.ws.rs.*;
import org.junit.jupiter.api.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class OpenApiContractTest {
    @Test @DisplayName("Todos os endpoints reais estão no contrato OpenAPI, sem rotas fictícias")
    void contrato() throws Exception {
        JsonNode doc = new ObjectMapper().readTree(getClass().getResourceAsStream("/META-INF/openapi.json"));
        Set<String> esperadas = new HashSet<>();
        for (Class<?> recurso : new Class<?>[]{AuthRestResource.class,VistoriaRestResource.class,EvidenciaRestResource.class}) {
            String base = recurso.getAnnotation(Path.class).value();
            for (Method metodo : recurso.getDeclaredMethods()) {
                for (Annotation annotation : metodo.getAnnotations()) {
                    HttpMethod http = annotation.annotationType().getAnnotation(HttpMethod.class);
                    if (http == null) continue;
                    Path sub = metodo.getAnnotation(Path.class);
                    String caminho = base + (sub == null ? "" : sub.value());
                    String verbo = http.value().toLowerCase(Locale.ROOT);
                    assertTrue(doc.path("paths").path(caminho).has(verbo), verbo + " " + caminho);
                    esperadas.add(verbo + " " + caminho);
                }
            }
        }
        Set<String> documentadas = new HashSet<>();
        doc.path("paths").fields().forEachRemaining(path -> path.getValue().fieldNames().forEachRemaining(verbo -> {
            if (Set.of("get","post","put","patch","delete").contains(verbo)) documentadas.add(verbo + " " + path.getKey());
        }));
        assertEquals(esperadas,documentadas);
        assertEquals("bearer",doc.at("/components/securitySchemes/bearerAuth/scheme").asText());
    }
}
