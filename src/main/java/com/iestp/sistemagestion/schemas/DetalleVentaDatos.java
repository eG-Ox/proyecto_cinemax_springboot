package com.iestp.sistemagestion.schemas;
import jakarta.validation.constraints.*;
import java.time.*;
public record DetalleVentaDatos(@NotNull(groups=Crear.class) @Positive Integer id_venta, @NotNull(groups=Crear.class) @Positive Integer id_funcion, @NotBlank(groups=Crear.class) String asiento, @NotBlank(groups=Crear.class) String codigo_boleto) { }
