package com.iestp.sistemagestion;

import jakarta.servlet.ServletContext;
import jakarta.servlet.SessionTrackingMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"test", "prod"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:cinemax-prod;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "cinemax.security.admin.password="
})
class SeguridadProduccionTests {
    @Value("${local.server.port}") int port;
    @Autowired ObjectMapper mapper;
    @Autowired ServletContext servletContext;
    @Autowired @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mappings;

    @Test void usaCookieSeguraCabecerasYNoRegistraSwaggerEnProduccion() throws Exception {
        var client = new ClienteApi(port, mapper);
        var csrf = client.pedir("GET", "/api/auth/csrf", null);
        assertThat(csrf.status()).isEqualTo(200);
        assertThat(csrf.headers().allValues("set-cookie").toString()).contains("Secure", "HttpOnly", "SameSite=Lax");
        assertThat(csrf.headers().firstValue("cache-control").orElseThrow()).contains("no-store");
        assertThat(csrf.headers().firstValue("x-content-type-options")).contains("nosniff");
        assertThat(csrf.headers().firstValue("x-frame-options")).contains("DENY");
        assertThat(csrf.headers().firstValue("content-security-policy").orElseThrow()).contains("frame-ancestors 'none'");
        assertThat(csrf.headers().firstValue("referrer-policy")).contains("no-referrer");
        assertThat(servletContext.getEffectiveSessionTrackingModes()).containsExactly(SessionTrackingMode.COOKIE);
        assertThat(mappings.getHandlerMethods().keySet().stream().flatMap(m -> m.getPatternValues().stream()).toList())
            .noneMatch(p -> p.contains("api-docs") || p.contains("swagger-ui") || p.equals("/docs"));
    }
}
