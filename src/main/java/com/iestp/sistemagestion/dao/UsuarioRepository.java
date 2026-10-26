package com.iestp.sistemagestion.dao;
import com.iestp.sistemagestion.modelo.Usuario;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByCorreoAndIdNot(String correo, Integer id);
    boolean existsByCorreo(String correo);
}
