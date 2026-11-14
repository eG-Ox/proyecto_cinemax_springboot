package com.iestp.sistemagestion.routers;
import com.iestp.sistemagestion.schemas.*;
import com.iestp.sistemagestion.servicio.VentaService;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/ventas")
public class VentaController extends CrudController<VentaDatos,VentaDTO> {
    private final VentaService cine;
    public VentaController(VentaService cine) { super(cine); this.cine=cine; }
    @GetMapping("/usuario/{id}") public List<VentaDTO> porUsuario(@PathVariable Integer id) { return cine.porUsuario(id); }
    @PostMapping("/con-boleto") @ResponseStatus(HttpStatus.CREATED)
    public VentaConBoletoDTO conBoleto(@Valid @RequestBody VentaConBoletoDatos datos) { return cine.conBoleto(datos); }
}
