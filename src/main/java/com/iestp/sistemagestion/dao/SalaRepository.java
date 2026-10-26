package com.iestp.sistemagestion.dao;
import com.iestp.sistemagestion.modelo.Sala;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface SalaRepository extends JpaRepository<Sala, Integer> {
    List<Sala> findByPelicula_IdOrderByIdAsc(Integer peliculaId);
    boolean existsByPelicula_Id(Integer peliculaId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Sala s where s.id = :id")
    Optional<Sala> bloquear(@Param("id") Integer id);
}
