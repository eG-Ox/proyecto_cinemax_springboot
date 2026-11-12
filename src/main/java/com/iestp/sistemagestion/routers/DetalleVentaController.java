package com.iestp.sistemagestion.routers;
import com.iestp.sistemagestion.schemas.*;
import com.iestp.sistemagestion.servicio.DetalleVentaService;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/detalles-venta")
public class DetalleVentaController extends CrudController<DetalleVentaDatos,DetalleVentaDTO> {
    private final DetalleVentaService cine;
    public DetalleVentaController(DetalleVentaService cine) { super(cine); this.cine=cine; }
    @GetMapping("/venta/{id}") public List<DetalleVentaDTO> porVenta(@PathVariable Integer id) { return cine.porVenta(id); }
}
