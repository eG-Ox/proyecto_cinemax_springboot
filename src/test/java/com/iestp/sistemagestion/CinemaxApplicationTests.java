package com.iestp.sistemagestion;

import com.iestp.sistemagestion.dao.*;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
    "spring.datasource.url=jdbc:h2:mem:cinemax-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CinemaxApplicationTests {
    @Value("${local.server.port}") int port;
    @Autowired ObjectMapper json;
    @Autowired DetalleVentaRepository detalles;
    @Autowired VentaRepository ventas;
    @Autowired FuncionRepository funciones;
    @Autowired SalaRepository salas;
    @Autowired PeliculaRepository peliculas;
    @Autowired UsuarioRepository usuarios;
    @Autowired RegistroRepository registros;
    final HttpClient client=HttpClient.newHttpClient();
    int usuario, pelicula, sala, funcion;

    record Respuesta(int status, JsonNode datos) { }
    Respuesta pedir(String metodo,String ruta,Object body) throws Exception {
        var builder=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api"+ruta));
        builder.header("Content-Type","application/json");
        builder.method(metodo,body==null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        var response=client.send(builder.build(),HttpResponse.BodyHandlers.ofString());
        return new Respuesta(response.statusCode(),json.readTree(response.body()));
    }
    JsonNode crear(String ruta,Object body) throws Exception {
        var r=pedir("POST",ruta,body); assertThat(r.status()).as(r.datos().toString()).isEqualTo(201); return r.datos();
    }
    @BeforeEach void preparar() throws Exception {
        detalles.deleteAllInBatch(); ventas.deleteAllInBatch(); funciones.deleteAllInBatch();
        salas.deleteAllInBatch(); peliculas.deleteAllInBatch(); usuarios.deleteAllInBatch(); registros.deleteAllInBatch();
        usuario=crear("/usuarios/",Map.of("nombres_usuario","Ana Prueba","correo","ana@example.com")).get("id").asInt();
        pelicula=crear("/peliculas/",Map.of("titulo","Pelicula de prueba","genero","Drama","clasificacion","APT","duracion",120)).get("id").asInt();
        sala=crear("/salas/",Map.of("id_pelicula",pelicula,"nombre_sala","Sala 1","capacidad",2)).get("id").asInt();
        funcion=crear("/funciones/",Map.of("id_pelicula",pelicula,"id_sala",sala,"fecha_funcion","2026-12-01","hora","19:30","precio",15.50)).get("id").asInt();
    }
    Map<String,Object> boleto(String asiento,String codigo) {
        return Map.of("id_usuario",usuario,"id_funcion",funcion,"asiento",asiento,"codigo_boleto",codigo);
    }
    int disponibles(int id) throws Exception { return pedir("GET","/salas/"+id,null).datos().get("asientos_disponibles").asInt(); }

    @Test void ventaAtomicaYEliminacionRestauranCupo() throws Exception {
        var compra=crear("/ventas/con-boleto",boleto(" a1 "," bol-1 "));
        int venta=compra.get("venta").get("id").asInt(); int detalle=compra.get("detalle").get("id").asInt();
        assertThat(compra.get("venta").get("total").asDouble()).isEqualTo(15.50);
        assertThat(compra.get("detalle").get("asiento").asText()).isEqualTo("A1");
        assertThat(disponibles(sala)).isEqualTo(1);
        assertThat(pedir("GET","/ventas/usuario/"+usuario,null).datos().size()).isEqualTo(1);
        assertThat(pedir("GET","/detalles-venta/venta/"+venta,null).datos().size()).isEqualTo(1);
        assertThat(pedir("DELETE","/ventas/"+venta,null).status()).isEqualTo(409);
        assertThat(pedir("DELETE","/usuarios/"+usuario,null).status()).isEqualTo(409);
        assertThat(pedir("DELETE","/funciones/"+funcion,null).status()).isEqualTo(409);
        assertThat(pedir("DELETE","/detalles-venta/"+detalle,null).status()).isEqualTo(200);
        assertThat(disponibles(sala)).isEqualTo(2);
        assertThat(pedir("GET","/ventas/"+venta,null).datos().get("total").asDouble()).isZero();
        assertThat(pedir("GET","/ventas/",null).datos().size()).isZero();
        assertThat(pedir("DELETE","/ventas/"+venta,null).status()).isEqualTo(200);
    }

    @Test void rechazaDuplicadosYRevierteVentaYRegistro() throws Exception {
        crear("/ventas/con-boleto",boleto("A1","BOL-1"));
        long cantidadVentas=ventas.count(); long cantidadLogs=registros.count();
        assertThat(pedir("POST","/ventas/con-boleto",boleto(" a1 ","BOL-2")).status()).isEqualTo(409);
        assertThat(pedir("POST","/ventas/con-boleto",boleto("A2"," bol-1 ")).status()).isEqualTo(409);
        assertThat(ventas.count()).isEqualTo(cantidadVentas);
        assertThat(registros.count()).isEqualTo(cantidadLogs);
        assertThat(disponibles(sala)).isEqualTo(1);
        assertThat(pedir("POST","/usuarios",Map.of("nombres_usuario","Otro","correo","ana@example.com")).status()).isEqualTo(400);
    }

    @Test void validaCamposYReferenciasSinEscrituras() throws Exception {
        assertThat(pedir("POST","/usuarios/",Map.of()).status()).isEqualTo(422);
        assertThat(pedir("POST","/usuarios/",Map.of("nombres_usuario","Ana","correo","invalido")).status()).isEqualTo(422);
        assertThat(pedir("PUT","/peliculas/"+pelicula,Map.of("duracion",0)).status()).isEqualTo(422);
        assertThat(pedir("POST","/salas",Map.of("id_pelicula",999999,"nombre_sala","Sin pelicula","capacidad",10)).status()).isEqualTo(404);
        assertThat(pedir("POST","/ventas/con-boleto",Map.of("id_usuario",usuario,"id_funcion",999999,"asiento","A1","codigo_boleto","X")).status()).isEqualTo(404);
        assertThat(ventas.count()).isZero();
        assertThat(pedir("GET","/usuarios/999999",null).status()).isEqualTo(404);
    }

    @Test void actualizacionParcialYProteccionDeRelaciones() throws Exception {
        assertThat(pedir("PUT","/usuarios/"+usuario,Map.of("nombres_usuario","Ana Actualizada")).status()).isEqualTo(200);
        var u=pedir("GET","/usuarios/"+usuario,null).datos();
        assertThat(u.get("correo").asText()).isEqualTo("ana@example.com");
        assertThat(u.get("nombres_usuario").asText()).isEqualTo("Ana Actualizada");
        assertThat(pedir("PUT","/peliculas/"+pelicula,Map.of("titulo","Nuevo titulo")).status()).isEqualTo(200);
        assertThat(pedir("PUT","/funciones/"+funcion,Map.of("precio",20)).status()).isEqualTo(200);
        assertThat(pedir("GET","/salas/pelicula/"+pelicula,null).datos().size()).isEqualTo(1);
        assertThat(pedir("DELETE","/peliculas/"+pelicula,null).status()).isEqualTo(409);
        assertThat(pedir("DELETE","/salas/"+sala,null).status()).isEqualTo(409);
    }

    @Test void trasladoDeBoletoEntreSalasEsTransaccional() throws Exception {
        var compra=crear("/ventas/con-boleto",boleto("A1","BOL-1")); int id=compra.get("detalle").get("id").asInt();
        int otraSala=crear("/salas",Map.of("id_pelicula",pelicula,"nombre_sala","Sala 2","capacidad",1)).get("id").asInt();
        int otraFuncion=crear("/funciones",Map.of("id_pelicula",pelicula,"id_sala",otraSala,"fecha_funcion","2026-12-01","hora","21:00","precio",22)).get("id").asInt();
        assertThat(pedir("PUT","/detalles-venta/"+id,Map.of("id_funcion",otraFuncion,"asiento","B1")).status()).isEqualTo(200);
        assertThat(disponibles(sala)).isEqualTo(2); assertThat(disponibles(otraSala)).isZero();
        assertThat(pedir("GET","/ventas/"+compra.get("venta").get("id").asInt(),null).datos().get("total").asDouble()).isEqualTo(22);
        assertThat(pedir("PUT","/funciones/"+otraFuncion,Map.of("id_sala",sala)).status()).isEqualTo(409);
        assertThat(pedir("DELETE","/detalles-venta/"+id,null).status()).isEqualTo(200);
        assertThat(disponibles(otraSala)).isEqualTo(1);
    }

    @Test void noReduceCapacidadBajoLosAsientosVendidos() throws Exception {
        crear("/ventas/con-boleto",boleto("A1","BOL-1")); crear("/ventas/con-boleto",boleto("A2","BOL-2"));
        assertThat(pedir("PUT","/salas/"+sala,Map.of("capacidad",1)).status()).isEqualTo(409);
        assertThat(pedir("POST","/ventas/con-boleto",boleto("A3","BOL-3")).status()).isEqualTo(409);
        assertThat(ventas.count()).isEqualTo(2);
        assertThat(pedir("PUT","/salas/"+sala,Map.of("capacidad",3)).status()).isEqualTo(200);
        assertThat(disponibles(sala)).isEqualTo(1);
    }

    @Test void dosComprasConcurrentesNoVendenElMismoAsiento() throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            CountDownLatch inicio=new CountDownLatch(1);
            var a=pool.submit(() -> { inicio.await(); return pedir("POST","/ventas/con-boleto",boleto("A1","CON-1")).status(); });
            var b=pool.submit(() -> { inicio.await(); return pedir("POST","/ventas/con-boleto",boleto("A1","CON-2")).status(); });
            inicio.countDown();
            assertThat(List.of(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS))).containsExactlyInAnyOrder(201,409);
            assertThat(ventas.count()).isEqualTo(1); assertThat(detalles.count()).isEqualTo(1); assertThat(disponibles(sala)).isEqualTo(1);
        } finally { pool.shutdownNow(); }
    }

    @Test void historialSeRegistraYLimpia() throws Exception {
        var lista=pedir("GET","/registros/",null).datos(); assertThat(lista.size()).isEqualTo(4);
        assertThat(lista.get(0).get("modulo").asText()).isEqualTo("Usuarios");
        assertThat(pedir("DELETE","/registros/",null).status()).isEqualTo(200);
        assertThat(pedir("GET","/registros/",null).datos().size()).isZero();
    }
}
