package com.iestp.sistemagestion.dao;
import com.iestp.sistemagestion.modelo.Venta;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface VentaRepository extends JpaRepository<Venta, Integer> {
    boolean existsByUsuario_Id(Integer usuarioId);
    List<Venta> findByUsuario_IdOrderByIdAsc(Integer usuarioId);
}
