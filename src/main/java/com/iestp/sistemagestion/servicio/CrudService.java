package com.iestp.sistemagestion.servicio;
import java.util.List;
public interface CrudService<D,R> {
    List<R> listar(); R obtenerPorId(Integer id); R crear(D datos); R actualizar(Integer id,D datos); void eliminar(Integer id);
}
