package com.iestp.sistemagestion.servicio;
import com.iestp.sistemagestion.dao.*;
import com.iestp.sistemagestion.modelo.*;
import com.iestp.sistemagestion.schemas.*;
import static com.iestp.sistemagestion.excepciones.CinemaxException.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class VentaService implements CrudService<VentaDatos,VentaDTO> {
    private final VentaRepository repo;
    private final UsuarioRepository usuarios;
    private final DetalleVentaRepository detalles;
    private final DetalleVentaService boletos;
    private final RegistroService log;
    public VentaService(VentaRepository repo, UsuarioRepository usuarios, DetalleVentaRepository detalles, DetalleVentaService boletos, RegistroService log) { this.repo=repo; this.usuarios=usuarios; this.detalles=detalles; this.boletos=boletos; this.log=log; }

    private Venta buscar(Integer id) { return repo.findById(id).orElseThrow(() -> noEncontrado("Venta",id)); }
    public VentaDTO respuesta(Venta v) {
        BigDecimal total=detalles.findByVenta_IdOrderByIdAsc(v.getId()).stream().map(d -> d.getFuncion().getPrecio()).reduce(BigDecimal.ZERO,BigDecimal::add);
        return new VentaDTO(v.getId(),v.getUsuario().getId(),v.getFecha(),total);
    }
    @Transactional(readOnly=true) public VentaDTO obtenerPorId(Integer id) { return respuesta(buscar(id)); }
    @Transactional(readOnly=true) public List<VentaDTO> listar() {
        return repo.findAll(Sort.by("fecha","id")).stream().filter(v -> detalles.existsByVenta_Id(v.getId())).map(this::respuesta).toList();
    }
    @Transactional(readOnly=true) public List<VentaDTO> porUsuario(Integer id) {
        if(!usuarios.existsById(id)) throw noEncontrado("Usuario",id);
        return repo.findByUsuario_IdOrderByIdAsc(id).stream().filter(v -> detalles.existsByVenta_Id(v.getId())).map(this::respuesta).toList();
    }
    public VentaDTO crear(VentaDatos d) {
        Venta v=new Venta(); v.setUsuario(usuarios.findById(d.id_usuario()).orElseThrow(() -> noEncontrado("Usuario",d.id_usuario())));
        v.setFecha(LocalDateTime.now(ZoneId.of("America/Lima"))); repo.saveAndFlush(v); log.agregar("Ventas","Registrar","ID="+v.getId()); return respuesta(v);
    }
    // Una sola transaccion: si falla el boleto tampoco queda una venta vacia ni se descuenta cupo.
    public VentaConBoletoDTO conBoleto(VentaConBoletoDatos d) {
        VentaDTO venta=crear(new VentaDatos(d.id_usuario(),null));
        DetalleVentaDTO detalle=boletos.crear(new DetalleVentaDatos(venta.id(),d.id_funcion(),d.asiento(),d.codigo_boleto()));
        return new VentaConBoletoDTO(obtenerPorId(venta.id()),detalle);
    }
    public VentaDTO actualizar(Integer id,VentaDatos d) {
        Venta v=buscar(id);
        if(d.id_usuario()!=null) v.setUsuario(usuarios.findById(d.id_usuario()).orElseThrow(() -> noEncontrado("Usuario",d.id_usuario())));
        if(d.fecha_compra()!=null) v.setFecha(d.fecha_compra());
        repo.saveAndFlush(v); log.agregar("Ventas","Actualizar","ID="+id); return respuesta(v);
    }
    public void eliminar(Integer id) {
        Venta v=buscar(id); if(detalles.existsByVenta_Id(id)) throw conflicto("La venta tiene boletos asociados");
        repo.delete(v); repo.flush(); log.agregar("Ventas","Eliminar","ID="+id);
    }

}
