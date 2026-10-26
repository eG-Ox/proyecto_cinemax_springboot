package com.iestp.sistemagestion.routers;
import com.iestp.sistemagestion.servicio.CrudService;
import com.iestp.sistemagestion.schemas.Crear;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;
public abstract class CrudController<D,R> {
    protected final CrudService<D,R> servicio;
    protected CrudController(CrudService<D,R> servicio) { this.servicio=servicio; }
    @GetMapping({"", "/"}) public List<R> listar() { return servicio.listar(); }
    @GetMapping("/{id}") public R obtener(@PathVariable Integer id) { return servicio.obtenerPorId(id); }
    @PostMapping({"", "/"}) @ResponseStatus(HttpStatus.CREATED)
    public R crear(@Validated(Crear.class) @RequestBody D datos) { return servicio.crear(datos); }
    @PutMapping("/{id}") public R actualizar(@PathVariable Integer id,@Valid @RequestBody D datos) { return servicio.actualizar(id,datos); }
    @DeleteMapping("/{id}") public Map<String,String> eliminar(@PathVariable Integer id) { servicio.eliminar(id); return Map.of("mensaje","Registro eliminado correctamente"); }
}
