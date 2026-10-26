package com.iestp.sistemagestion.schemas;
import com.iestp.sistemagestion.modelo.DetalleVenta;
import java.time.*;
public record DetalleVentaDTO(Integer id, Integer id_venta, Integer id_funcion, String asiento, String codigo_boleto) {
    public static DetalleVentaDTO fromEntity(DetalleVenta v) { return new DetalleVentaDTO(v.getId(), v.getVenta().getId(), v.getFuncion().getId(), v.getAsiento(), v.getCodigo()); }
}
