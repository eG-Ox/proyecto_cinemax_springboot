package com.iestp.sistemagestion.dao;
import com.iestp.sistemagestion.modelo.Funcion;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface FuncionRepository extends JpaRepository<Funcion, Integer> {
    boolean existsByPelicula_Id(Integer peliculaId);
    boolean existsBySala_Id(Integer salaId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Funcion f where f.id = :id")
    Optional<Funcion> bloquear(@Param("id") Integer id);
}
