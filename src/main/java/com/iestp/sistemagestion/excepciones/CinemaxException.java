package com.iestp.sistemagestion.excepciones;
import org.springframework.http.HttpStatus;
public class CinemaxException extends RuntimeException {
    private final HttpStatus status;
    public CinemaxException(HttpStatus status, String mensaje) { super(mensaje); this.status = status; }
    public HttpStatus getStatus() { return status; }
    public static CinemaxException noEncontrado(String recurso, Integer id) {
        return new CinemaxException(HttpStatus.NOT_FOUND, recurso + " ID=" + id + " no encontrado");
    }
    public static CinemaxException conflicto(String mensaje) { return new CinemaxException(HttpStatus.CONFLICT, mensaje); }
    public static CinemaxException invalido(String mensaje) { return new CinemaxException(HttpStatus.BAD_REQUEST, mensaje); }
}
