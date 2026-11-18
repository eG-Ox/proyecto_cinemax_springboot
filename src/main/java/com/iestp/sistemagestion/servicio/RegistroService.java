package com.iestp.sistemagestion.servicio;
import com.iestp.sistemagestion.dao.RegistroRepository;
import com.iestp.sistemagestion.modelo.Registro;
import java.time.*;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional
public class RegistroService {
    private final RegistroRepository repo;
    public RegistroService(RegistroRepository repo) { this.repo=repo; }
    public void agregar(String modulo, String accion, Object informacion) {
        Registro r=new Registro(); r.hora=LocalDateTime.now(ZoneId.of("America/Lima")); r.nivel="INFO";
        r.modulo=modulo; r.accion=accion; r.informacion=informacion.toString(); repo.save(r);
    }
    @Transactional(readOnly=true)
    public List<Registro> listar() { return repo.findAll(Sort.by("id")); }
    public void limpiar() { repo.deleteAllInBatch(); }
}
