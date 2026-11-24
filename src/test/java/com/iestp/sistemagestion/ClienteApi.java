package com.iestp.sistemagestion;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

final class ClienteApi {
    record Respuesta(int status, JsonNode datos, HttpHeaders headers) { }
    private final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private final HttpClient client = HttpClient.newBuilder().cookieHandler(cookies).build();
    private final ObjectMapper mapper;
    private final String base;
    private String csrf;
    private String csrfHeader;

    ClienteApi(int port, ObjectMapper mapper) {
        this.mapper = mapper;
        this.base = "http://localhost:" + port;
    }

    void refrescarCsrf() throws Exception {
        var respuesta = raw("GET", "/api/auth/csrf", null, Map.of());
        if (respuesta.status() != 200) throw new IllegalStateException("No se obtuvo CSRF");
        csrf = respuesta.datos().get("token").asText();
        csrfHeader = respuesta.datos().get("headerName").asText();
    }

    Respuesta login(String usuario, String password) throws Exception {
        if (csrf == null) refrescarCsrf();
        String form = "usuario=" + URLEncoder.encode(usuario, StandardCharsets.UTF_8)
            + "&contrasena=" + URLEncoder.encode(password, StandardCharsets.UTF_8);
        var respuesta = raw("POST", "/api/auth/login", form,
            Map.of("Content-Type", "application/x-www-form-urlencoded", csrfHeader, csrf));
        if (respuesta.status() == 200) refrescarCsrf();
        return respuesta;
    }

    Respuesta pedir(String metodo, String ruta, Object body) throws Exception {
        boolean escribe = !metodo.equals("GET") && !metodo.equals("HEAD") && !metodo.equals("OPTIONS");
        if (escribe && csrf == null) refrescarCsrf();
        Map<String, String> headers = escribe
            ? Map.of("Content-Type", "application/json", csrfHeader, csrf)
            : Map.of("Content-Type", "application/json");
        return raw(metodo, ruta, body == null ? null : mapper.writeValueAsString(body), headers);
    }

    Respuesta raw(String metodo, String ruta, String body, Map<String, String> headers) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(base + ruta));
        headers.forEach(builder::header);
        builder.method(metodo, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        var response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        JsonNode datos;
        if (response.body().isBlank()) {
            datos = mapper.nullNode();
        } else if (response.headers().firstValue("content-type").orElse("").contains("json")) {
            datos = mapper.readTree(response.body());
        } else {
            datos = mapper.valueToTree(response.body());
        }
        return new Respuesta(response.statusCode(), datos, response.headers());
    }

    String sesionId() {
        return cookies.getCookieStore().getCookies().stream().filter(c -> c.getName().equals("JSESSIONID"))
            .map(c -> c.getValue()).findFirst().orElse(null);
    }
}
