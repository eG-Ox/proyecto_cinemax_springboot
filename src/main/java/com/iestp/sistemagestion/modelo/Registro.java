package com.iestp.sistemagestion.modelo;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="registro_cinemax")
public class Registro {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public LocalDateTime hora;
    @Column(nullable=false) public String nivel;
    @Column(nullable=false) public String modulo;
    @Column(nullable=false) public String accion;
    @Column(nullable=false, columnDefinition="text") public String informacion;
    public Registro() { }
}
