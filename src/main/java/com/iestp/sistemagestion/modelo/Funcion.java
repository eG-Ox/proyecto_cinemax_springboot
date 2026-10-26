package com.iestp.sistemagestion.modelo;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity
@Table(name = "funcion")
public class Funcion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_funcion")
    private Integer id;
    public Funcion() { }
    public Integer getId() { return id; }
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pelicula", nullable = false)
    private Pelicula pelicula;
    public Pelicula getPelicula() { return pelicula; }
    public void setPelicula(Pelicula valor) { this.pelicula = valor; }
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sala", nullable = false)
    private Sala sala;
    public Sala getSala() { return sala; }
    public void setSala(Sala valor) { this.sala = valor; }
    @Column(name = "fecha_funcion", nullable = false)
    private LocalDate fecha;
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate valor) { this.fecha = valor; }
    @Column(name = "hora", nullable = false)
    private LocalTime hora;
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime valor) { this.hora = valor; }
    @Column(name = "precio", nullable = false, precision = 8, scale = 2)
    private BigDecimal precio;
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal valor) { this.precio = valor; }
}
