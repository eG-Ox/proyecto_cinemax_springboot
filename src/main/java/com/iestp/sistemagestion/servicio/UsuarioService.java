package com.iestp.sistemagestion.servicio;
import com.iestp.sistemagestion.dao.*;
import com.iestp.sistemagestion.modelo.*;
import com.iestp.sistemagestion.schemas.*;
import static com.iestp.sistemagestion.excepciones.CinemaxException.*;
import java.util.*;
import java.time.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class UsuarioService implements CrudService<UsuarioDatos,UsuarioDTO> {
    private final UsuarioRepository repo;
    private final VentaRepository ventas;
    private final RegistroService log;
    public UsuarioService(UsuarioRepository repo, VentaRepository ventas, RegistroService log) { this.repo=repo; this.ventas=ventas; this.log=log; }

    private Usuario buscar(Integer id) { return repo.findById(id).orElseThrow(() -> noEncontrado("Usuario",id)); }
    @Transactional(readOnly=true) public UsuarioDTO obtenerPorId(Integer id) { return UsuarioDTO.fromEntity(buscar(id)); }
    @Transactional(readOnly=true) public List<UsuarioDTO> listar() { return repo.findAll(Sort.by("nombres")).stream().map(UsuarioDTO::fromEntity).toList(); }
    public UsuarioDTO crear(UsuarioDatos d) { return guardar(new Usuario(),d,"Registrar"); }
    public UsuarioDTO actualizar(Integer id,UsuarioDatos d) { return guardar(buscar(id),d,"Actualizar"); }
    private UsuarioDTO guardar(Usuario v,UsuarioDatos d,String accion) {
        if(d.nombres_usuario()!=null) { if(d.nombres_usuario().isBlank()) throw invalido("El nombre es obligatorio"); v.setNombres(d.nombres_usuario().trim()); }
        if(d.correo()!=null) {
            String correo=d.correo().trim();
            if(correo.isBlank()) throw invalido("El correo es obligatorio");
            boolean repetido=v.getId()==null ? repo.existsByCorreo(correo) : repo.existsByCorreoAndIdNot(correo,v.getId());
            if(repetido) throw invalido("Correo ya registrado");
            v.setCorreo(correo);
        }
        repo.saveAndFlush(v); log.agregar("Usuarios",accion,v.getNombres()); return UsuarioDTO.fromEntity(v);
    }
    public void eliminar(Integer id) {
        Usuario v=buscar(id); if(ventas.existsByUsuario_Id(id)) throw conflicto("El usuario tiene ventas asociadas");
        repo.delete(v); repo.flush(); log.agregar("Usuarios","Eliminar","ID="+id);
    }

}
