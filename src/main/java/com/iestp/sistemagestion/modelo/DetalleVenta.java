package com.iestp.sistemagestion.modelo;
import jakarta.persistence.*;
import java.time.*;
@Entity
@Table(name = "detalle_venta", uniqueConstraints = {
    @UniqueConstraint(name = "uq_detalle_venta_codigo_boleto", columnNames = "codigo_boleto"),
    @UniqueConstraint(name = "uq_detalle_venta_funcion_asiento", columnNames = {"id_funcion", "asiento"})})
public class DetalleVenta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_detalle")
    private Integer id;
    public DetalleVenta() { }
    public Integer getId() { return id; }
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_venta", nullable = false)
    private Venta venta;
    public Venta getVenta() { return venta; }
    public void setVenta(Venta valor) { this.venta = valor; }
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_funcion", nullable = false)
    private Funcion funcion;
    public Funcion getFuncion() { return funcion; }
    public void setFuncion(Funcion valor) { this.funcion = valor; }
    @Column(name = "asiento", nullable = false, columnDefinition = "text")
    private String asiento;
    public String getAsiento() { return asiento; }
    public void setAsiento(String valor) { this.asiento = valor; }
    @Column(name = "codigo_boleto", nullable = false, columnDefinition = "text")
    private String codigo;
    public String getCodigo() { return codigo; }
    public void setCodigo(String valor) { this.codigo = valor; }
}
