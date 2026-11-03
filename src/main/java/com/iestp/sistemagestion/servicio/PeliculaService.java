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
public class PeliculaService implements CrudService<PeliculaDatos,PeliculaDTO> {
    private final PeliculaRepository repo;
    private final SalaRepository salas;
    private final FuncionRepository funciones;
    private final RegistroService log;
    public PeliculaService(PeliculaRepository repo, SalaRepository salas, FuncionRepository funciones, RegistroService log) { this.repo=repo; this.salas=salas; this.funciones=funciones; this.log=log; }

    private Pelicula buscar(Integer id) { return repo.findById(id).orElseThrow(() -> noEncontrado("Pelicula",id)); }
    @Transactional(readOnly=true) public PeliculaDTO obtenerPorId(Integer id) { return PeliculaDTO.fromEntity(buscar(id)); }
    @Transactional(readOnly=true) public List<PeliculaDTO> listar() { return repo.findAll(Sort.by("titulo")).stream().map(PeliculaDTO::fromEntity).toList(); }
    public PeliculaDTO crear(PeliculaDatos d) { return guardar(new Pelicula(),d,"Registrar"); }
    public PeliculaDTO actualizar(Integer id,PeliculaDatos d) { return guardar(buscar(id),d,"Actualizar"); }
    private String texto(String valor) { if(valor.isBlank()) throw invalido("Los campos de pelicula no pueden estar vacios"); return valor.trim(); }
    private PeliculaDTO guardar(Pelicula v,PeliculaDatos d,String accion) {
        if(d.titulo()!=null) v.setTitulo(texto(d.titulo())); if(d.genero()!=null) v.setGenero(texto(d.genero()));
        if(d.clasificacion()!=null) v.setClasificacion(texto(d.clasificacion())); if(d.duracion()!=null) v.setDuracion(d.duracion());
        repo.saveAndFlush(v); log.agregar("Peliculas",accion,v.getTitulo()); return PeliculaDTO.fromEntity(v);
    }
    public void eliminar(Integer id) {
        Pelicula v=buscar(id); if(salas.existsByPelicula_Id(id)||funciones.existsByPelicula_Id(id)) throw conflicto("La pelicula tiene salas o funciones asociadas");
        repo.delete(v); repo.flush(); log.agregar("Peliculas","Eliminar","ID="+id);
    }

}
