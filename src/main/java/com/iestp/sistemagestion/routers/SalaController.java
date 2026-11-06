package com.iestp.sistemagestion.routers;
import com.iestp.sistemagestion.schemas.*;
import com.iestp.sistemagestion.servicio.SalaService;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/salas")
public class SalaController extends CrudController<SalaDatos,SalaDTO> {
    private final SalaService cine;
    public SalaController(SalaService cine) { super(cine); this.cine=cine; }
    @GetMapping("/pelicula/{id}") public List<SalaDTO> porPelicula(@PathVariable Integer id) { return cine.porPelicula(id); }
}
