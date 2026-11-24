package com.iestp.sistemagestion;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:cinemax-limite;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "cinemax.security.admin.username=adminprueba",
    "cinemax.security.admin.password=ClaveDePrueba-2026",
    "cinemax.security.login.max-attempts=3"
})
class LimiteLoginIntegrationTests {
    @Value("${local.server.port}") int port;
    @Autowired ObjectMapper mapper;

    @Test void limitaPorOrigenRealAunqueCambienLaCookieOLosHeadersDeProxy() throws Exception {
        var client = new ClienteApi(port, mapper);
        for (int i = 0; i < 2; i++) assertThat(client.login("adminprueba", "incorrecta").status()).isEqualTo(401);
        assertThat(client.login("adminprueba", "ClaveDePrueba-2026").status()).isEqualTo(200);
        for (int i = 0; i < 3; i++) assertThat(client.login("noexiste", "incorrecta").status()).isEqualTo(401);
        var otro = new ClienteApi(port, mapper);
        var bloqueado = otro.login("adminprueba", "ClaveDePrueba-2026");
        assertThat(bloqueado.status()).isEqualTo(429);
        assertThat(Long.parseLong(bloqueado.headers().firstValue("retry-after").orElseThrow())).isBetween(1L, 300L);
        var csrf = otro.pedir("GET", "/api/auth/csrf", null).datos();
        assertThat(otro.raw("POST", "/api/auth/login", "usuario=adminprueba&contrasena=ClaveDePrueba-2026",
            Map.of("Content-Type", "application/x-www-form-urlencoded",
                csrf.get("headerName").asText(), csrf.get("token").asText(),
                "X-Forwarded-For", "203.0.113.100")).status()).isEqualTo(429);
        assertThat(otro.pedir("GET", "/api/auth/me", null).status()).isEqualTo(401);
    }
}
