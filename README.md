# Cinemax — Backend

API de cine con Spring Boot, Java 17, JPA y PostgreSQL. Este repositorio no incluye frontend.

## Ejecutar

1. Copiar `.env.example` a `.env` en la raíz y completar la conexión PostgreSQL.
2. Definir `CINEMAX_ADMIN_USUARIO` y una `CINEMAX_ADMIN_PASSWORD` propia de al menos 12 caracteres y un máximo de 72 bytes UTF-8.
3. Compilar y arrancar desde la raíz:

```powershell
.\mvnw.cmd verify
java -jar target/cinemax-0.0.1-SNAPSHOT.jar
```

Para desarrollo sin PostgreSQL:

```powershell
java -jar target/cinemax-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo
```

El perfil demo conserva una base H2 en `${user.home}/.cinemax`. No es la base PostgreSQL. No tiene una contraseña de acceso predeterminada.

La primera cuenta se crea una sola vez y su clave se guarda con BCrypt, coste 12. Si ya existe, cambiar la variable de entorno no sobrescribe su clave. Si no se configura ninguna clave y no existe cuenta, la API permanece protegida, pero no es posible iniciar sesión. Las cuentas de acceso están en `cuenta_acceso`; `usuario` representa a los clientes del cine y no concede acceso al sistema.

## Contrato de autenticación

El navegador debe conservar la cookie `JSESSIONID`. Para peticiones a otro origen permitido, usar `credentials: "include"` en fetch o `withCredentials: true` en Axios. No guardar contraseñas ni identificadores de sesión en localStorage.

| Método y ruta | Uso |
| --- | --- |
| `GET /api/auth/csrf` | Obtiene `{headerName, parameterName, token}` y prepara la sesión. Disponible sin autenticarse. |
| `POST /api/auth/login` | Valida `usuario` y `contrasena`, enviados como `application/x-www-form-urlencoded`, junto al header CSRF obtenido. |
| `GET /api/auth/me` | Devuelve `{usuario, roles}` para una sesión válida; devuelve 401 si no existe. |
| `POST /api/auth/logout` | Cierra la sesión y elimina la cookie. Requiere el header CSRF. |

Login y logout los procesan los filtros de Spring Security. El frontend no determina si una persona está autorizada.

Flujo del futuro frontend:

1. Obtener `/api/auth/csrf` y conservar la cookie.
2. Enviar el formulario a `/api/auth/login`, incluyendo el header indicado por `headerName` con el valor de `token`.
3. Después del login, volver a obtener `/api/auth/csrf`: el identificador de sesión y el token anterior cambian.
4. Enviar el token actualizado en cada POST, PUT o DELETE del sistema. Las peticiones GET también necesitan la cookie de sesión, excepto `/api/auth/csrf`.
5. Al cargar una pantalla, consultar `/api/auth/me`. Una redirección del frontend no sustituye la autorización del servidor.
6. Al salir, hacer POST a `/api/auth/logout`. Antes de otro login, obtener un token nuevo.

El login correcto responde 200 con `{"usuario":"nombre","roles":["ROLE_ADMIN"]}`. Nunca devuelve el hash ni la contraseña.

| Estado | Significado |
| --- | --- |
| 401 | Credenciales incorrectas, cuenta deshabilitada o sesión ausente/inválida. |
| 403 | CSRF ausente/inválido, origen CORS no permitido o acceso denegado. |
| 415 | El login no se envió como formulario. |
| 429 | Límite de intentos alcanzado; consultar `Retry-After`, expresado en segundos. |

Los errores de seguridad usan `{"detail":"mensaje"}`. Un usuario inexistente y una contraseña incorrecta reciben el mismo mensaje. Después de un 403 por token vencido, obtener un token nuevo; no reintentar automáticamente una operación de negocio que podría haberse ejecutado.

## Protección de la API

Todos los módulos del cine requieren una cuenta administradora autenticada. La cookie es HttpOnly, SameSite=Lax y expira con la sesión tras 20 minutos de inactividad. Se cambia el identificador al autenticar y solo se admiten cookies como mecanismo de transporte de sesión. Logout invalida la sesión en el servidor. Deshabilitar una cuenta revoca sus sesiones en la siguiente petición.

El login admite como máximo 5 intentos por dirección remota en una ventana de 300 segundos. Un login correcto reinicia su contador. El bloqueo se conserva aunque se cambie de cookie; el filtro no usa `X-Forwarded-For` para elegir la dirección. El almacenamiento está acotado y es local a cada proceso: reiniciar lo vacía. Para varias instancias se necesita un limitador compartido o un gateway. Un proxy puede agrupar usuarios bajo la misma dirección; su configuración debe revisarse al desplegar.

No hay registro público ni recuperación automática de contraseña. La administración de las cuentas es una operación del servidor/BD. No reutilizar claves de pruebas en un despliegue real.

## Producción

- Preparar PostgreSQL y ejecutar `src/main/resources/schema_cine.sql` con una cuenta administradora de BD antes de iniciar el perfil `prod`. El script crea las tablas faltantes; no es un sistema de migraciones para cambiar tablas existentes.
- Proporcionar credenciales mediante el entorno o un gestor de secretos. No subir `.env` al repositorio.
- Servir frontend y backend mediante HTTPS y definir `CORS_ORIGINS` con los orígenes exactos de confianza, separados por comas. No se admite `*` con cookies.
- El perfil `prod` exige cookies Secure, valida el esquema en lugar de modificarlo y desactiva Swagger/OpenAPI.

```powershell
java -jar target/cinemax-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

El perfil no instala certificados ni configura un proxy TLS. Usar HTTPS en el despliegue: la opción Secure de la cookie no cifra por sí sola la conexión. No combinar `prod` con `demo`.

En desarrollo, Swagger está en `/docs` y requiere una sesión administradora previamente autenticada. El esquema OpenAPI está en `/v3/api-docs`. El formulario de login se integra mediante el contrato descrito arriba.

## Pruebas

`mvnw.cmd verify` ejecuta pruebas con bases H2 en memoria, aisladas de los datos reales. Cubren autenticación, BCrypt, cookies, CSRF, CORS, logout, cuentas deshabilitadas, bloqueo de intentos, concurrencia de compras, consistencia de asientos y esquema SQL. El modo de compatibilidad de H2 no sustituye una validación del despliegue en PostgreSQL.
