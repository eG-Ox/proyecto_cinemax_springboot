package com.iestp.sistemagestion;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import static org.assertj.core.api.Assertions.*;

class EsquemaSqlTests {
    @Test void elScriptIncluyeLasOchoTablasYSePuedeEjecutarDosVeces() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:esquema;MODE=PostgreSQL", "sa", "")) {
            var script = new ClassPathResource("schema_cine.sql");
            ScriptUtils.executeSqlScript(connection, script);
            ScriptUtils.executeSqlScript(connection, script);
            var nombres = new HashSet<String>();
            try (var tablas = connection.getMetaData().getTables(null, "PUBLIC", "%", new String[]{"TABLE"})) {
                while (tablas.next()) nombres.add(tablas.getString("TABLE_NAME").toLowerCase(Locale.ROOT));
            }
            assertThat(nombres).contains("usuario", "cuenta_acceso", "pelicula", "sala", "funcion", "venta", "detalle_venta", "registro_cinemax");
            try (var sql = connection.createStatement()) {
                sql.executeUpdate("INSERT INTO usuario(nombres_usuario, correo) VALUES ('Prueba', 'prueba@example.com')");
                assertThatThrownBy(() -> sql.executeUpdate("INSERT INTO usuario(nombres_usuario, correo) VALUES ('Otro', 'prueba@example.com')"))
                    .isInstanceOf(SQLException.class);
                assertThatThrownBy(() -> sql.executeUpdate("INSERT INTO venta(id_usuario, fecha_compra) VALUES (9999, CURRENT_TIMESTAMP)"))
                    .isInstanceOf(SQLException.class);
            }
        }
    }
}
