package com.iestp.sistemagestion.schemas;
import jakarta.validation.constraints.*;
import java.time.*;
public record VentaDatos(@NotNull(groups=Crear.class) @Positive Integer id_usuario, LocalDateTime fecha_compra) { }
