package com.iestp.sistemagestion;

import com.iestp.sistemagestion.dao.CuentaAccesoRepository;
import com.iestp.sistemagestion.modelo.CuentaAcceso;
import com.iestp.sistemagestion.servicio.CuentaAccesoInicializador;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CuentaAccesoInicializadorTests {
    private final CuentaAccesoRepository cuentas = mock(CuentaAccesoRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Test void almacenaHashBCryptSinGuardarLaClaveOriginal() {
        String password = "ClaveDePrueba-2026";
        new CuentaAccesoInicializador(cuentas, encoder, "ADMIN", password).run(null);
        var captura = ArgumentCaptor.forClass(CuentaAcceso.class);
        verify(cuentas).saveAndFlush(captura.capture());
        var cuenta = captura.getValue();
        assertThat(cuenta.getUsuario()).isEqualTo("admin");
        assertThat(cuenta.getPasswordHash()).startsWith("$2a$12$").isNotEqualTo(password);
        assertThat(encoder.matches(password, cuenta.getPasswordHash())).isTrue();
    }

    @Test void noCreaUnaCuentaConClavePredeterminada() {
        new CuentaAccesoInicializador(cuentas, encoder, "admin", "").run(null);
        verify(cuentas, never()).saveAndFlush(any());
    }

    @Test void noSobrescribeLaClaveDeUnaCuentaExistente() {
        when(cuentas.existsByUsuario("admin")).thenReturn(true);
        new CuentaAccesoInicializador(cuentas, encoder, "admin", "OtraClaveDePrueba").run(null);
        verify(cuentas, never()).saveAndFlush(any());
    }

    @Test void rechazaClavesCortasYLasQueBCryptTruncaria() {
        for (String password : new String[]{"admin123", "a".repeat(73), "ñ".repeat(37)}) {
            assertThatThrownBy(() -> new CuentaAccesoInicializador(cuentas, encoder, "admin", password).run(null))
                .isInstanceOf(IllegalStateException.class);
        }
        verify(cuentas, never()).saveAndFlush(any());
    }

    @Test void rechazaNombreDeAdministradorInvalido() {
        assertThatThrownBy(() -> new CuentaAccesoInicializador(cuentas, encoder, "a b", "ClaveDePrueba-2026").run(null))
            .isInstanceOf(IllegalStateException.class);
        verify(cuentas, never()).saveAndFlush(any());
    }
}
