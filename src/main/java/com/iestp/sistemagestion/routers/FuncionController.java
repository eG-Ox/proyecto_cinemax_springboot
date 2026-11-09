package com.iestp.sistemagestion.routers;
import com.iestp.sistemagestion.schemas.*;
import com.iestp.sistemagestion.servicio.FuncionService;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/funciones")
public class FuncionController extends CrudController<FuncionDatos,FuncionDTO> {
    private final FuncionService cine;
    public FuncionController(FuncionService cine) { super(cine); this.cine=cine; }
    
}
