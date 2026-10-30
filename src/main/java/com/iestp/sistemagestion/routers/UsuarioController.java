package com.iestp.sistemagestion.routers;
import com.iestp.sistemagestion.schemas.*;
import com.iestp.sistemagestion.servicio.UsuarioService;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/usuarios")
public class UsuarioController extends CrudController<UsuarioDatos,UsuarioDTO> {
    private final UsuarioService cine;
    public UsuarioController(UsuarioService cine) { super(cine); this.cine=cine; }
    
}
