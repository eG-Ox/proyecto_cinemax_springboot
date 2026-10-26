package com.iestp.sistemagestion.schemas;
import jakarta.validation.constraints.*;
import java.time.*;
public record SalaDatos(@NotNull(groups=Crear.class) @Positive Integer id_pelicula, @NotBlank(groups=Crear.class) String nombre_sala, @NotNull(groups=Crear.class) @Positive Integer capacidad) { }
