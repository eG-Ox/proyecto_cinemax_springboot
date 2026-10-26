package com.iestp.sistemagestion.schemas;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
public record FuncionDatos(@NotNull(groups=Crear.class) @Positive Integer id_pelicula, @NotNull(groups=Crear.class) @Positive Integer id_sala, @NotNull(groups=Crear.class) LocalDate fecha_funcion, @NotNull(groups=Crear.class) LocalTime hora, @NotNull(groups=Crear.class) @DecimalMin("0.01") @Digits(integer=6, fraction=2) BigDecimal precio) { }
