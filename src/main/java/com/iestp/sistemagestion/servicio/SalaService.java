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
public class SalaService implements CrudService<SalaDatos,SalaDTO> {
    private final SalaRepository repo;
    private final PeliculaRepository peliculas;
    private final FuncionRepository funciones;
    private final RegistroService log;
    public SalaService(SalaRepository repo, PeliculaRepository peliculas, FuncionRepository funciones, RegistroService log) { this.repo=repo; this.peliculas=peliculas; this.funciones=funciones; this.log=log; }

    private Sala buscar(Integer id) { return repo.findById(id).orElseThrow(() -> noEncontrado("Sala",id)); }
    public Sala bloquear(Integer id) { return repo.bloquear(id).orElseThrow(() -> noEncontrado("Sala",id)); }
    @Transactional(readOnly=true) public SalaDTO obtenerPorId(Integer id) { return SalaDTO.fromEntity(buscar(id)); }
    @Transactional(readOnly=true) public List<SalaDTO> listar() { return repo.findAll(Sort.by("nombre")).stream().map(SalaDTO::fromEntity).toList(); }
    @Transactional(readOnly=true) public List<SalaDTO> porPelicula(Integer id) {
        if(!peliculas.existsById(id)) throw noEncontrado("Pelicula",id);
        return repo.findByPelicula_IdOrderByIdAsc(id).stream().map(SalaDTO::fromEntity).toList();
    }
    public SalaDTO crear(SalaDatos d) { return guardar(new Sala(),d,"Registrar"); }
    public SalaDTO actualizar(Integer id,SalaDatos d) { return guardar(bloquear(id),d,"Actualizar"); }
    private SalaDTO guardar(Sala v,SalaDatos d,String accion) {
        if(d.id_pelicula()!=null) v.setPelicula(peliculas.findById(d.id_pelicula()).orElseThrow(() -> noEncontrado("Pelicula",d.id_pelicula())));
        if(d.nombre_sala()!=null) { if(d.nombre_sala().isBlank()) throw invalido("Nombre de sala obligatorio"); v.setNombre(d.nombre_sala().trim()); }
        if(d.capacidad()!=null) {
            int vendidos=v.getId()==null ? 0 : v.getCapacidad()-v.getDisponibles();
            if(d.capacidad()<vendidos) throw conflicto("La capacidad no puede ser menor a los asientos vendidos: "+vendidos);
            v.setCapacidad(d.capacidad()); v.setDisponibles(d.capacidad()-vendidos);
        }
        repo.saveAndFlush(v); log.agregar("Salas",accion,v.getNombre()); return SalaDTO.fromEntity(v);
    }
    public void eliminar(Integer id) {
        Sala v=bloquear(id); if(funciones.existsBySala_Id(id)) throw conflicto("La sala tiene funciones asociadas");
        repo.delete(v); repo.flush(); log.agregar("Salas","Eliminar","ID="+id);
    }
    // Conserva el contador por sala del sistema Python. El bloqueo evita sobreventa concurrente.
    public void reservar(Sala sala) {
        if(sala.getDisponibles()<=0) throw conflicto("La sala no tiene asientos disponibles");
        sala.setDisponibles(sala.getDisponibles()-1);
    }
    public void liberar(Sala sala) { sala.setDisponibles(Math.min(sala.getCapacidad(),sala.getDisponibles()+1)); }

}
