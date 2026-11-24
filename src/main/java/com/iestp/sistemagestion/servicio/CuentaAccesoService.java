package com.iestp.sistemagestion.servicio;

import com.iestp.sistemagestion.dao.CuentaAccesoRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuentaAccesoService implements UserDetailsService {
    private final CuentaAccesoRepository cuentas;

    public CuentaAccesoService(CuentaAccesoRepository cuentas) {
        this.cuentas = cuentas;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usuario) {
        var cuenta = cuentas.findByUsuario(usuario.trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));
        return User.withUsername(cuenta.getUsuario())
            .password(cuenta.getPasswordHash())
            .roles("ADMIN")
            .disabled(!cuenta.isHabilitada())
            .build();
    }
}
