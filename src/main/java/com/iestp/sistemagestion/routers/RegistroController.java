package com.iestp.sistemagestion.routers;
import com.iestp.sistemagestion.servicio.RegistroService;
import com.iestp.sistemagestion.modelo.Registro;
import java.util.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/registros")
public class RegistroController {
    private final RegistroService servicio;
    public RegistroController(RegistroService servicio) { this.servicio=servicio; }
    @GetMapping({"", "/"}) public List<Registro> listar() { return servicio.listar(); }
    @DeleteMapping({"", "/"}) public Map<String,String> limpiar() { servicio.limpiar(); return Map.of("mensaje","Historial eliminado correctamente"); }
}
