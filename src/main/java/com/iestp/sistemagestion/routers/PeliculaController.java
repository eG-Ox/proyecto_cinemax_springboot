package com.iestp.sistemagestion.routers;
import com.iestp.sistemagestion.schemas.*;
import com.iestp.sistemagestion.servicio.PeliculaService;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/peliculas")
public class PeliculaController extends CrudController<PeliculaDatos,PeliculaDTO> {
    private final PeliculaService cine;
    public PeliculaController(PeliculaService cine) { super(cine); this.cine=cine; }
    
}
