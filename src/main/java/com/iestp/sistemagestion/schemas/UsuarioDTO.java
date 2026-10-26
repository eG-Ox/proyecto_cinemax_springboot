package com.iestp.sistemagestion.schemas;
import com.iestp.sistemagestion.modelo.Usuario;
import java.time.*;
public record UsuarioDTO(Integer id, String nombres_usuario, String correo) {
    public static UsuarioDTO fromEntity(Usuario v) { return new UsuarioDTO(v.getId(), v.getNombres(), v.getCorreo()); }
}
