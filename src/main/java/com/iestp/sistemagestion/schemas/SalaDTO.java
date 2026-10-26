package com.iestp.sistemagestion.schemas;
import com.iestp.sistemagestion.modelo.Sala;
import java.time.*;
public record SalaDTO(Integer id, Integer id_pelicula, String nombre_sala, Integer capacidad, Integer asientos_disponibles) {
    public static SalaDTO fromEntity(Sala v) { return new SalaDTO(v.getId(), v.getPelicula().getId(), v.getNombre(), v.getCapacidad(), v.getDisponibles()); }
}
