package com.iestp.sistemagestion.config;

import com.iestp.sistemagestion.servicio.LimitadorIntentosService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

public class LimiteLoginFilter extends OncePerRequestFilter {
    private final LimitadorIntentosService limite;
    private final ObjectMapper mapper;

    public LimiteLoginFilter(LimitadorIntentosService limite, ObjectMapper mapper) {
        this.limite = limite;
        this.mapper = mapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !"/api/auth/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        var intento = limite.intentar(request.getRemoteAddr());
        if (!intento.permitido()) {
            response.setHeader("Retry-After", Long.toString(intento.reintentarEnSegundos()));
            RespuestaSeguridad.error(mapper, response, 429, "Demasiados intentos de acceso. Intente mas tarde");
            return;
        }
        String tipo = request.getContentType();
        if (tipo == null || !tipo.toLowerCase(Locale.ROOT).split(";", 2)[0].trim()
                .equals("application/x-www-form-urlencoded")) {
            RespuestaSeguridad.error(mapper, response, 415, "Envie usuario y contrasena como formulario");
            return;
        }
        if (request.getContentLengthLong() > 8192) {
            RespuestaSeguridad.error(mapper, response, 413, "Formulario demasiado grande");
            return;
        }
        try {
            String[] usuarios = request.getParameterValues("usuario");
            String[] claves = request.getParameterValues("contrasena");
            if (usuarios == null || usuarios.length != 1 || usuarios[0].isBlank() || usuarios[0].length() > 50
                    || claves == null || claves.length != 1 || claves[0].isEmpty()
                    || claves[0].getBytes(StandardCharsets.UTF_8).length > 72) {
                RespuestaSeguridad.error(mapper, response, 401, "Usuario o contrasena incorrectos");
                return;
            }
        } catch (IllegalStateException ex) {
            RespuestaSeguridad.error(mapper, response, 400, "Formulario invalido");
            return;
        }
        chain.doFilter(request, response);
    }
}
