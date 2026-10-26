package com.iestp.sistemagestion.schemas;
import jakarta.validation.constraints.*;
import java.time.*;
public record VentaConBoletoDatos(@NotNull @Positive Integer id_usuario, @NotNull @Positive Integer id_funcion, @NotBlank String asiento, @NotBlank String codigo_boleto) { }
