package com.iestp.sistemagestion;

import com.iestp.sistemagestion.dao.CuentaAccesoRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:cinemax-auth;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "cinemax.security.admin.username=adminprueba",
    "cinemax.security.admin.password=ClaveDePrueba-2026"
})
class AutenticacionTests {
    @Value("${local.server.port}") int port;
    @Autowired ObjectMapper mapper;
    @Autowired CuentaAccesoRepository cuentas;
    ClienteApi client;

    @BeforeEach void preparar() {
        client = new ClienteApi(port, mapper);
        var cuenta = cuentas.findByUsuario("adminprueba").orElseThrow();
        cuenta.setHabilitada(true);
        cuentas.saveAndFlush(cuenta);
    }

    @Test void exigeAutenticacionEnLaApiYEnSwagger() throws Exception {
        for (String ruta : new String[]{"/api/usuarios", "/api/peliculas", "/api/ventas", "/api/registros", "/api/auth/me", "/v3/api-docs"}) {
            var r = client.pedir("GET", ruta, null);
            assertThat(r.status()).as(ruta).isEqualTo(401);
            assertThat(r.datos().get("detail").asText()).isEqualTo("Debe iniciar sesion");
        }
    }

    @Test void loginValidoCreaSesionYRotaSuIdentificador() throws Exception {
        client.refrescarCsrf();
        String sesionAnterior = client.sesionId();
        var login = client.login("adminprueba", "ClaveDePrueba-2026");
        assertThat(login.status()).isEqualTo(200);
        assertThat(login.datos().get("usuario").asText()).isEqualTo("adminprueba");
        assertThat(login.datos().toString()).doesNotContain("password", "contrasena", "$2a$");
        assertThat(client.sesionId()).isNotBlank().isNotEqualTo(sesionAnterior);
        assertThat(client.pedir("GET", "/api/auth/me", null).status()).isEqualTo(200);
        assertThat(client.pedir("GET", "/api/usuarios", null).status()).isEqualTo(200);
        assertThat(login.headers().allValues("set-cookie").toString()).contains("HttpOnly", "SameSite=Lax");
    }

    @Test void usuarioInexistenteYClaveErroneaDevuelvenElMismoMensaje() throws Exception {
        var incorrecta = client.login("adminprueba", "Incorrecta-2026");
        var inexistente = client.login("noexiste", "Incorrecta-2026");
        assertThat(incorrecta.status()).isEqualTo(401);
        assertThat(inexistente.status()).isEqualTo(401);
        assertThat(incorrecta.datos()).isEqualTo(inexistente.datos());
        assertThat(client.pedir("GET", "/api/auth/me", null).status()).isEqualTo(401);
    }

    @Test void cuentaDeshabilitadaNoPuedeIniciarSesion() throws Exception {
        var cuenta = cuentas.findByUsuario("adminprueba").orElseThrow();
        cuenta.setHabilitada(false);
        cuentas.saveAndFlush(cuenta);
        var r = client.login("adminprueba", "ClaveDePrueba-2026");
        assertThat(r.status()).isEqualTo(401);
        assertThat(r.datos().get("detail").asText()).isEqualTo("Usuario o contrasena incorrectos");
    }

    @Test void csrfProtegeLoginYEscriturasAunqueExistaSesion() throws Exception {
        assertThat(client.raw("POST", "/api/auth/login", "usuario=adminprueba&contrasena=ClaveDePrueba-2026",
            Map.of("Content-Type", "application/x-www-form-urlencoded")).status()).isEqualTo(403);
        assertThat(client.login("adminprueba", "ClaveDePrueba-2026").status()).isEqualTo(200);
        assertThat(client.raw("POST", "/api/usuarios", "{}", Map.of("Content-Type", "application/json")).status()).isEqualTo(403);
        assertThat(client.raw("DELETE", "/api/registros", null, Map.of("X-CSRF-TOKEN", "incorrecto")).status()).isEqualTo(403);
    }

    @Test void cerrarSesionImpideReutilizarLaCookieAnterior() throws Exception {
        assertThat(client.login("adminprueba", "ClaveDePrueba-2026").status()).isEqualTo(200);
        String idAnterior = client.sesionId();
        assertThat(client.pedir("POST", "/api/auth/logout", null).status()).isEqualTo(200);
        assertThat(client.pedir("GET", "/api/auth/me", null).status()).isEqualTo(401);
        var atacante = new ClienteApi(port, mapper);
        assertThat(atacante.raw("GET", "/api/auth/me", null, Map.of("Cookie", "JSESSIONID=" + idAnterior)).status()).isEqualTo(401);
    }

    @Test void corsAceptaSoloElOrigenConfigurado() throws Exception {
        var permitido = client.raw("OPTIONS", "/api/usuarios", null,
            Map.of("Origin", "http://localhost:5173", "Access-Control-Request-Method", "POST", "Access-Control-Request-Headers", "content-type,x-csrf-token"));
        assertThat(permitido.status()).isEqualTo(200);
        assertThat(permitido.headers().firstValue("access-control-allow-origin")).contains("http://localhost:5173");
        assertThat(permitido.headers().firstValue("access-control-allow-credentials")).contains("true");
        var externo = client.raw("OPTIONS", "/api/usuarios", null,
            Map.of("Origin", "https://otro-sitio.example", "Access-Control-Request-Method", "POST"));
        assertThat(externo.status()).isEqualTo(403);
        assertThat(externo.headers().firstValue("access-control-allow-origin")).isEmpty();
    }
}
