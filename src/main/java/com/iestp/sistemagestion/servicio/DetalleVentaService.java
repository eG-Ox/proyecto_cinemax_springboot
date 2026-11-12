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
public class DetalleVentaService implements CrudService<DetalleVentaDatos,DetalleVentaDTO> {
    private final DetalleVentaRepository repo;
    private final VentaRepository ventas;
    private final FuncionRepository funciones;
    private final SalaService salas;
    private final RegistroService log;
    public DetalleVentaService(DetalleVentaRepository repo, VentaRepository ventas, FuncionRepository funciones, SalaService salas, RegistroService log) { this.repo=repo; this.ventas=ventas; this.funciones=funciones; this.salas=salas; this.log=log; }

    private DetalleVenta buscar(Integer id) { return repo.findById(id).orElseThrow(() -> noEncontrado("Detalle de venta",id)); }
    @Transactional(readOnly=true) public DetalleVentaDTO obtenerPorId(Integer id) { return DetalleVentaDTO.fromEntity(buscar(id)); }
    @Transactional(readOnly=true) public List<DetalleVentaDTO> listar() { return repo.findAll(Sort.by("id")).stream().map(DetalleVentaDTO::fromEntity).toList(); }
    @Transactional(readOnly=true) public List<DetalleVentaDTO> porVenta(Integer id) {
        if(!ventas.existsById(id)) throw noEncontrado("Venta",id);
        return repo.findByVenta_IdOrderByIdAsc(id).stream().map(DetalleVentaDTO::fromEntity).toList();
    }
    public DetalleVentaDTO crear(DetalleVentaDatos d) { return guardar(new DetalleVenta(),d,"Registrar"); }
    public DetalleVentaDTO actualizar(Integer id,DetalleVentaDatos d) { return guardar(repo.bloquear(id).orElseThrow(() -> noEncontrado("Detalle de venta",id)),d,"Actualizar"); }
    private String normalizar(String texto) {
        if(texto.isBlank()) throw invalido("Asiento y codigo de boleto son obligatorios");
        return texto.trim().toUpperCase(Locale.ROOT);
    }
    private DetalleVentaDTO guardar(DetalleVenta v,DetalleVentaDatos d,String accion) {
        Integer funcionAnterior=v.getId()==null ? null : v.getFuncion().getId();
        Integer nuevaFuncion=d.id_funcion()==null ? funcionAnterior : d.id_funcion();
        // Bloquear funciones en orden estable antes de modificar sus salas.
        TreeSet<Integer> ids=new TreeSet<>(); ids.add(nuevaFuncion); if(funcionAnterior!=null) ids.add(funcionAnterior);
        Map<Integer,Funcion> bloqueadas=new HashMap<>();
        for(Integer id:ids) bloqueadas.put(id,funciones.bloquear(id).orElseThrow(() -> noEncontrado("Funcion",id)));
        Funcion nueva=bloqueadas.get(nuevaFuncion);
        Integer salaAnterior=funcionAnterior==null ? null : bloqueadas.get(funcionAnterior).getSala().getId();
        Integer salaNueva=nueva.getSala().getId();
        TreeSet<Integer> salasIds=new TreeSet<>(); salasIds.add(salaNueva); if(salaAnterior!=null) salasIds.add(salaAnterior);
        Map<Integer,Sala> salasBloqueadas=new HashMap<>();
        for(Integer id:salasIds) salasBloqueadas.put(id,salas.bloquear(id));
        if(d.id_venta()!=null) v.setVenta(ventas.findById(d.id_venta()).orElseThrow(() -> noEncontrado("Venta",d.id_venta())));
        v.setFuncion(nueva);
        if(d.asiento()!=null) v.setAsiento(normalizar(d.asiento())); if(d.codigo_boleto()!=null) v.setCodigo(normalizar(d.codigo_boleto()));
        Integer excluir=v.getId()==null ? -1 : v.getId();
        if(repo.existsByCodigoAndIdNot(v.getCodigo(),excluir)) throw conflicto("Codigo de boleto ya registrado");
        if(repo.existsByFuncion_IdAndAsientoAndIdNot(nuevaFuncion,v.getAsiento(),excluir)) throw conflicto("Asiento ya ocupado para esta funcion");
        if(salaAnterior==null) salas.reservar(salasBloqueadas.get(salaNueva));
        else if(!salaAnterior.equals(salaNueva)) { salas.liberar(salasBloqueadas.get(salaAnterior)); salas.reservar(salasBloqueadas.get(salaNueva)); }
        repo.saveAndFlush(v); log.agregar("Detalles de venta",accion,"ID="+v.getId()); return DetalleVentaDTO.fromEntity(v);
    }
    public void eliminar(Integer id) {
        DetalleVenta v=repo.bloquear(id).orElseThrow(() -> noEncontrado("Detalle de venta",id));
        Funcion f=funciones.bloquear(v.getFuncion().getId()).orElseThrow(() -> noEncontrado("Funcion",v.getFuncion().getId()));
        Sala sala=salas.bloquear(f.getSala().getId());
        repo.delete(v); repo.flush(); salas.liberar(sala); log.agregar("Detalles de venta","Eliminar","ID="+id);
    }

}
