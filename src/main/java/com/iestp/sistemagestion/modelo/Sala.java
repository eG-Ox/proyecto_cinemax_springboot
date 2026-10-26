package com.iestp.sistemagestion.modelo;
import jakarta.persistence.*;
import java.time.*;
@Entity
@Table(name = "sala")
public class Sala {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_sala")
    private Integer id;
    public Sala() { }
    public Integer getId() { return id; }
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pelicula", nullable = false)
    private Pelicula pelicula;
    public Pelicula getPelicula() { return pelicula; }
    public void setPelicula(Pelicula valor) { this.pelicula = valor; }
    @Column(name = "nombre_sala", nullable = false, columnDefinition = "text")
    private String nombre;
    public String getNombre() { return nombre; }
    public void setNombre(String valor) { this.nombre = valor; }
    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer valor) { this.capacidad = valor; }
    @Column(name = "asientos_disponibles", nullable = false)
    private Integer disponibles;
    public Integer getDisponibles() { return disponibles; }
    public void setDisponibles(Integer valor) { this.disponibles = valor; }
}
