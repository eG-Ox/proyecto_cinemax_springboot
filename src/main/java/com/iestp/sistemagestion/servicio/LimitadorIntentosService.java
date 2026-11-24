package com.iestp.sistemagestion.servicio;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LimitadorIntentosService {
    public record Resultado(boolean permitido, long reintentarEnSegundos) { }
    private record Ventana(int intentos, Instant vence) { }
    private final Map<String, Ventana> ventanas = new HashMap<>();
    private final Clock clock;
    private final int maxIntentos;
    private final int maxOrigenes;
    private final Duration duracion;

    public LimitadorIntentosService(Clock clock,
            @Value("${cinemax.security.login.max-attempts:5}") int maxIntentos,
            @Value("${cinemax.security.login.window-seconds:300}") long segundos,
            @Value("${cinemax.security.login.max-origins:10000}") int maxOrigenes) {
        if (maxIntentos < 1 || maxIntentos > 100 || segundos < 1 || segundos > 3600
                || maxOrigenes < 1 || maxOrigenes > 100000) {
            throw new IllegalArgumentException("Configuracion del limite de acceso fuera de rango");
        }
        this.clock = clock;
        this.maxIntentos = maxIntentos;
        this.maxOrigenes = maxOrigenes;
        this.duracion = Duration.ofSeconds(segundos);
    }

    public synchronized Resultado intentar(String origen) {
        Instant ahora = clock.instant();
        Ventana ventana = ventanas.get(origen);
        if (ventana != null && !ventana.vence().isAfter(ahora)) {
            ventanas.remove(origen);
            ventana = null;
        }
        if (ventana == null) {
            if (ventanas.size() >= maxOrigenes) {
                ventanas.entrySet().removeIf(e -> !e.getValue().vence().isAfter(ahora));
                if (ventanas.size() >= maxOrigenes) {
                    return new Resultado(false, duracion.toSeconds());
                }
            }
            ventanas.put(origen, new Ventana(1, ahora.plus(duracion)));
            return new Resultado(true, 0);
        }
        if (ventana.intentos() >= maxIntentos) {
            long espera = Math.max(1, (Duration.between(ahora, ventana.vence()).toMillis() + 999) / 1000);
            return new Resultado(false, espera);
        }
        ventanas.put(origen, new Ventana(ventana.intentos() + 1, ventana.vence()));
        return new Resultado(true, 0);
    }

    public synchronized void restablecer(String origen) {
        ventanas.remove(origen);
    }
}
