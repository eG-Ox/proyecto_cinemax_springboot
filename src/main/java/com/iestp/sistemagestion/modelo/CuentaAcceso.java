package com.iestp.sistemagestion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cuenta_acceso")
public class CuentaAcceso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String usuario;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false)
    private boolean habilitada = true;

    protected CuentaAcceso() { }

    public CuentaAcceso(String usuario, String passwordHash) {
        this.usuario = usuario;
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isHabilitada() { return habilitada; }
    public void setHabilitada(boolean habilitada) { this.habilitada = habilitada; }
}
