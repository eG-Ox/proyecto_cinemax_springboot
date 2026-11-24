package com.iestp.sistemagestion.servicio;

import com.iestp.sistemagestion.dao.CuentaAccesoRepository;
import com.iestp.sistemagestion.modelo.CuentaAcceso;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CuentaAccesoInicializador implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(CuentaAccesoInicializador.class);
    private final CuentaAccesoRepository cuentas;
    private final PasswordEncoder encoder;
    private final String usuario;
    private final String password;

    public CuentaAccesoInicializador(CuentaAccesoRepository cuentas, PasswordEncoder encoder,
            @Value("${cinemax.security.admin.username:admin}") String usuario,
            @Value("${cinemax.security.admin.password:}") String password) {
        this.cuentas = cuentas;
        this.encoder = encoder;
        this.usuario = usuario.trim().toLowerCase(Locale.ROOT);
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!usuario.matches("[a-z0-9._-]{3,50}")) {
            throw new IllegalStateException("El usuario administrador debe tener entre 3 y 50 caracteres: letras, numeros, punto, guion o guion bajo");
        }
        if (cuentas.existsByUsuario(usuario)) {
            return;
        }
        if (password.isBlank()) {
            LOG.warn("No hay cuenta de acceso configurada. Defina CINEMAX_ADMIN_PASSWORD antes del primer inicio de sesion.");
            return;
        }
        if (password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("La clave inicial debe tener al menos 12 caracteres y un maximo de 72 bytes UTF-8");
        }
        cuentas.saveAndFlush(new CuentaAcceso(usuario, encoder.encode(password)));
        LOG.info("Cuenta administradora inicial configurada");
    }
}
