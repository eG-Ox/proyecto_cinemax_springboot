package com.iestp.sistemagestion.excepciones;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CinemaxException.class)
    public ResponseEntity<?> negocio(CinemaxException ex) { return error(ex.getStatus(), ex.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validar(MethodArgumentNotValidException ex) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage()).collect(Collectors.joining("; ")));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<?> formato(Exception ex) { return error(HttpStatus.BAD_REQUEST, "Datos invalidos: revise tipos, fechas y campos del formulario"); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> integridad(DataIntegrityViolationException ex) {
        return error(HttpStatus.CONFLICT, "El registro esta duplicado o tiene registros relacionados. Revise correo, codigo de boleto y asiento.");
    }
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<?> ocupado(Exception ex) { return error(HttpStatus.CONFLICT, "Registro en uso. Intente nuevamente."); }
    private ResponseEntity<?> error(HttpStatus status, String detail) {
        return ResponseEntity.status(status).body(Map.of("detail", detail));
    }
}
