package com.iestp.sistemagestion.modelo;
import jakarta.persistence.*;
import java.time.*;
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_usuario")
    private Integer id;
    public Usuario() { }
    public Integer getId() { return id; }
    @Column(name = "nombres_usuario", nullable = false, columnDefinition = "text")
    private String nombres;
    public String getNombres() { return nombres; }
    public void setNombres(String valor) { this.nombres = valor; }
    @Column(name = "correo", nullable = false, columnDefinition = "text", unique = true)
    private String correo;
    public String getCorreo() { return correo; }
    public void setCorreo(String valor) { this.correo = valor; }
}
