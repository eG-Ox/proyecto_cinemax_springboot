package com.iestp.sistemagestion.schemas;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record VentaDTO(Integer id, Integer id_usuario, LocalDateTime fecha_compra, BigDecimal total) { }
