package com.iestp.sistemagestion;

import com.iestp.sistemagestion.servicio.LimitadorIntentosService;
import java.time.*;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class LimitadorIntentosTests {
    static class Reloj extends Clock {
        private final AtomicReference<Instant> ahora = new AtomicReference<>(Instant.parse("2026-11-23T12:00:00Z"));
        void avanzar(long segundos) { ahora.updateAndGet(t -> t.plusSeconds(segundos)); }
        @Override public Instant instant() { return ahora.get(); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return Clock.fixed(instant(), zone); }
    }

    @Test void bloqueaTrasElLimiteYPermiteDeNuevoAlVencer() {
        Reloj reloj = new Reloj();
        var limite = new LimitadorIntentosService(reloj, 3, 300, 10);
        for (int i = 0; i < 3; i++) assertThat(limite.intentar("127.0.0.1").permitido()).isTrue();
        var rechazo = limite.intentar("127.0.0.1");
        assertThat(rechazo.permitido()).isFalse();
        assertThat(rechazo.reintentarEnSegundos()).isEqualTo(300);
        reloj.avanzar(299);
        assertThat(limite.intentar("127.0.0.1").reintentarEnSegundos()).isEqualTo(1);
        reloj.avanzar(1);
        assertThat(limite.intentar("127.0.0.1").permitido()).isTrue();
    }

    @Test void unAccesoCorrectoRestableceElContador() {
        var limite = new LimitadorIntentosService(new Reloj(), 1, 300, 10);
        assertThat(limite.intentar("127.0.0.1").permitido()).isTrue();
        assertThat(limite.intentar("127.0.0.1").permitido()).isFalse();
        limite.restablecer("127.0.0.1");
        assertThat(limite.intentar("127.0.0.1").permitido()).isTrue();
    }

    @Test void limitaLaMemoriaYRecuperaEspacioDeEntradasVencidas() {
        Reloj reloj = new Reloj();
        var limite = new LimitadorIntentosService(reloj, 1, 300, 2);
        assertThat(limite.intentar("origen1").permitido()).isTrue();
        assertThat(limite.intentar("origen2").permitido()).isTrue();
        assertThat(limite.intentar("origen3").permitido()).isFalse();
        reloj.avanzar(300);
        assertThat(limite.intentar("origen3").permitido()).isTrue();
    }

    @Test void solicitudesSimultaneasNoSuperanElLimite() throws Exception {
        var limite = new LimitadorIntentosService(new Reloj(), 3, 300, 10);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            var tareas = new ArrayList<Callable<Boolean>>();
            for (int i = 0; i < 30; i++) tareas.add(() -> limite.intentar("mismo-origen").permitido());
            int aceptadas = 0;
            for (var resultado : pool.invokeAll(tareas)) if (resultado.get()) aceptadas++;
            assertThat(aceptadas).isEqualTo(3);
        } finally { pool.shutdownNow(); }
    }

    @Test void rechazaConfiguracionQueDesactiveElLimite() {
        assertThatThrownBy(() -> new LimitadorIntentosService(new Reloj(), 0, 300, 10))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
