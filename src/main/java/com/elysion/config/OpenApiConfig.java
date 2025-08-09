package com.elysion.config;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(title = "Elysion API", version = "1.0.0")
)
@SecurityScheme(
        securitySchemeName = "bearerAuth",
        // vollqualifiziert, damit kein Import nötig ist:
        type = org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT ohne 'Bearer ' einfügen. Die UI ergänzt 'Bearer ' automatisch."
)
public class OpenApiConfig {}