package com.iestp.sistemagestion.modelo;
import jakarta.persistence.*;
import java.time.*;
@Entity
@Table(name = "pelicula")
public class Pelicula {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_pelicula")
    private Integer id;
    public Pelicula() { }
    public Integer getId() { return id; }
    @Column(name = "titulo", nullable = false, columnDefinition = "text")
    private String titulo;
    public String getTitulo() { return titulo; }
    public void setTitulo(String valor) { this.titulo = valor; }
    @Column(name = "genero", nullable = false, columnDefinition = "text")
    private String genero;
    public String getGenero() { return genero; }
    public void setGenero(String valor) { this.genero = valor; }
    @Column(name = "clasificacion", nullable = false, columnDefinition = "text")
    private String clasificacion;
    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String valor) { this.clasificacion = valor; }
    @Column(name = "duracion", nullable = false)
    private Integer duracion;
    public Integer getDuracion() { return duracion; }
    public void setDuracion(Integer valor) { this.duracion = valor; }
}
