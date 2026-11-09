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
public class FuncionService implements CrudService<FuncionDatos,FuncionDTO> {
    private final FuncionRepository repo;
    private final PeliculaRepository peliculas;
    private final SalaRepository salas;
    private final DetalleVentaRepository detalles;
    private final RegistroService log;
    public FuncionService(FuncionRepository repo, PeliculaRepository peliculas, SalaRepository salas, DetalleVentaRepository detalles, RegistroService log) { this.repo=repo; this.peliculas=peliculas; this.salas=salas; this.detalles=detalles; this.log=log; }

    private Funcion buscar(Integer id) { return repo.findById(id).orElseThrow(() -> noEncontrado("Funcion",id)); }
    @Transactional(readOnly=true) public FuncionDTO obtenerPorId(Integer id) { return FuncionDTO.fromEntity(buscar(id)); }
    @Transactional(readOnly=true) public List<FuncionDTO> listar() { return repo.findAll(Sort.by("id")).stream().map(FuncionDTO::fromEntity).toList(); }
    public FuncionDTO crear(FuncionDatos d) { return guardar(new Funcion(),d,"Registrar"); }
    public FuncionDTO actualizar(Integer id,FuncionDatos d) { return guardar(repo.bloquear(id).orElseThrow(() -> noEncontrado("Funcion",id)),d,"Actualizar"); }
    private FuncionDTO guardar(Funcion v,FuncionDatos d,String accion) {
        if(d.id_pelicula()!=null) v.setPelicula(peliculas.findById(d.id_pelicula()).orElseThrow(() -> noEncontrado("Pelicula",d.id_pelicula())));
        if(d.id_sala()!=null) {
            if(v.getId()!=null && !d.id_sala().equals(v.getSala().getId()) && detalles.existsByFuncion_Id(v.getId()))
                throw conflicto("No se puede cambiar la sala de una funcion con boletos vendidos");
            v.setSala(salas.findById(d.id_sala()).orElseThrow(() -> noEncontrado("Sala",d.id_sala())));
        }
        if(d.fecha_funcion()!=null) v.setFecha(d.fecha_funcion()); if(d.hora()!=null) v.setHora(d.hora()); if(d.precio()!=null) v.setPrecio(d.precio());
        repo.saveAndFlush(v); log.agregar("Funciones",accion,"ID="+v.getId()); return FuncionDTO.fromEntity(v);
    }
    public void eliminar(Integer id) {
        Funcion v=repo.bloquear(id).orElseThrow(() -> noEncontrado("Funcion",id));
        if(detalles.existsByFuncion_Id(id)) throw conflicto("La funcion tiene boletos asociados");
        repo.delete(v); repo.flush(); log.agregar("Funciones","Eliminar","ID="+id);
    }

}
