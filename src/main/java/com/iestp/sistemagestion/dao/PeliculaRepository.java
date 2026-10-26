package com.iestp.sistemagestion.dao;
import com.iestp.sistemagestion.modelo.Pelicula;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface PeliculaRepository extends JpaRepository<Pelicula, Integer> {
    
}
