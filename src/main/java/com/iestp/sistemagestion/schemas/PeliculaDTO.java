package com.iestp.sistemagestion.schemas;
import com.iestp.sistemagestion.modelo.Pelicula;
import java.time.*;
public record PeliculaDTO(Integer id, String titulo, String genero, String clasificacion, Integer duracion) {
    public static PeliculaDTO fromEntity(Pelicula v) { return new PeliculaDTO(v.getId(), v.getTitulo(), v.getGenero(), v.getClasificacion(), v.getDuracion()); }
}
