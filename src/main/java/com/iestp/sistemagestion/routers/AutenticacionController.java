package com.iestp.sistemagestion.routers;

import com.iestp.sistemagestion.schemas.SesionDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {
    public record CsrfDTO(String headerName, String parameterName, String token) { }

    @GetMapping("/csrf")
    public CsrfDTO csrf(CsrfToken token) {
        return new CsrfDTO(token.getHeaderName(), token.getParameterName(), token.getToken());
    }

    @GetMapping("/me")
    public SesionDTO sesion(Authentication authentication) {
        return SesionDTO.fromAuthentication(authentication);
    }
}
