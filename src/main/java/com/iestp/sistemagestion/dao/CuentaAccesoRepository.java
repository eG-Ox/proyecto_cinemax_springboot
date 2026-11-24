package com.iestp.sistemagestion.dao;

import com.iestp.sistemagestion.modelo.CuentaAcceso;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaAccesoRepository extends JpaRepository<CuentaAcceso, Long> {
    Optional<CuentaAcceso> findByUsuario(String usuario);
    boolean existsByUsuario(String usuario);
    boolean existsByUsuarioAndHabilitadaTrue(String usuario);
}
