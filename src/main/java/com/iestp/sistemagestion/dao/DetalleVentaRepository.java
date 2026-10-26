package com.iestp.sistemagestion.dao;
import com.iestp.sistemagestion.modelo.DetalleVenta;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {
    List<DetalleVenta> findByVenta_IdOrderByIdAsc(Integer ventaId);
    boolean existsByVenta_Id(Integer ventaId);
    boolean existsByFuncion_Id(Integer funcionId);
    boolean existsByCodigoAndIdNot(String codigo, Integer id);
    boolean existsByFuncion_IdAndAsientoAndIdNot(Integer funcionId, String asiento, Integer id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DetalleVenta d where d.id = :id")
    Optional<DetalleVenta> bloquear(@Param("id") Integer id);
}
