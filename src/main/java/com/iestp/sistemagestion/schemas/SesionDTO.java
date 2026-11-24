package com.iestp.sistemagestion.schemas;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public record SesionDTO(String usuario, List<String> roles) {
    public static SesionDTO fromAuthentication(Authentication authentication) {
        return new SesionDTO(authentication.getName(), authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).toList());
    }
}
