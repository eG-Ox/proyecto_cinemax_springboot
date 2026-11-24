package com.iestp.sistemagestion.config;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;

public final class RespuestaSeguridad {
    private RespuestaSeguridad() { }

    public static void escribir(ObjectMapper mapper, HttpServletResponse response,
            int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(mapper.writeValueAsString(body));
    }

    public static void error(ObjectMapper mapper, HttpServletResponse response,
            int status, String mensaje) throws IOException {
        escribir(mapper, response, status, Map.of("detail", mensaje));
    }
}
