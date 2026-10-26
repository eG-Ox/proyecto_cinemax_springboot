package com.iestp.sistemagestion.modelo;
import jakarta.persistence.*;
import java.time.*;
@Entity
@Table(name = "venta")
public class Venta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_venta")
    private Integer id;
    public Venta() { }
    public Integer getId() { return id; }
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario valor) { this.usuario = valor; }
    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fecha;
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime valor) { this.fecha = valor; }
}
