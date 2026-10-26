package com.iestp.sistemagestion.schemas;
import jakarta.validation.constraints.*;
import java.time.*;
public record UsuarioDatos(@NotBlank(groups=Crear.class) String nombres_usuario, @NotBlank(groups=Crear.class) @Email String correo) { }
