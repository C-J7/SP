package com.studypilot.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "StudyPilot API",
        version = "1.0.0",
        description = "Syllabus ingestion, study plan scheduling and progress tracking. "
                + "Mutating endpoints accept an Idempotency-Key header; every response carries X-Request-Id; "
                + "errors are RFC 9457 problem+json.",
        license = @License(name = "Source on GitHub", url = "https://github.com/C-J7/SP")))
public class OpenApiConfig {
}
