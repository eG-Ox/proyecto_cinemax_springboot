package com.iestp.sistemagestion.schemas;
import jakarta.validation.constraints.*;
import java.time.*;
public record PeliculaDatos(@NotBlank(groups=Crear.class) String titulo, @NotBlank(groups=Crear.class) String genero, @NotBlank(groups=Crear.class) String clasificacion, @NotNull(groups=Crear.class) @Positive Integer duracion) { }
