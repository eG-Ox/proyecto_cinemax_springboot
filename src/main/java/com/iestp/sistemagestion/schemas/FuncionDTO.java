package com.iestp.sistemagestion.schemas;
import com.iestp.sistemagestion.modelo.Funcion;
import java.time.*;
import java.math.BigDecimal;
public record FuncionDTO(Integer id, Integer id_pelicula, Integer id_sala, LocalDate fecha_funcion, LocalTime hora, BigDecimal precio) {
    public static FuncionDTO fromEntity(Funcion v) { return new FuncionDTO(v.getId(), v.getPelicula().getId(), v.getSala().getId(), v.getFecha(), v.getHora(), v.getPrecio()); }
}
