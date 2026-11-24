package com.iestp.sistemagestion.config;

import com.iestp.sistemagestion.dao.CuentaAccesoRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

public class CuentaVigenteFilter extends OncePerRequestFilter {
    private final CuentaAccesoRepository cuentas;
    private final ObjectMapper mapper;

    public CuentaVigenteFilter(CuentaAccesoRepository cuentas, ObjectMapper mapper) {
        this.cuentas = cuentas;
        this.mapper = mapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && !cuentas.existsByUsuarioAndHabilitadaTrue(authentication.getName())) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            RespuestaSeguridad.error(mapper, response, 401, "Sesion invalida. Inicie sesion nuevamente");
            return;
        }
        chain.doFilter(request, response);
    }
}
