# ms-usuarios

Microservicio Spring Boot 4.1 (JDK 21, Maven, JPA, MySQL) de BPO Seguros. **Una sola responsabilidad:** las personas que usan la app
(asegurados, liquidadores y analistas): crear cuentas, iniciar sesión, claves y bloqueo de liquidadores.
Tiene su **propia base de datos** (`bposeguros_usuarios`) 

| Servicio | Puerto | Base de datos | Responsabilidad |
|---|---|---|---|
| `ms-usuarios` | 8081 | `bposeguros_usuarios` | Cuentas, login, roles, claves, bloqueo de liquidadores |
| `ms-notificaciones` | 8084 | — | Envío real de correos desde `bpo.seguros2026@gmail.com` |

## Estructura

```
src/main/java/cl/bposeguros/ms_usuarios/
├── MsUsuariosApplication.java        Punto de entrada (@SpringBootApplication)
├── model/
│   ├── Rol.java                      Entidad: 1 Asegurado, 2 Liquidador, 3 Analista (ids fijos = los de la app)
│   └── Usuario.java                  Entidad: tabla "usuarios" (misma información que la app tenía en SQLite)
├── repository/                       Interfaces JpaRepository: consultas a la BD sin escribir SQL
├── dto/                              JSON de entrada y salida (records de Java). UsuarioResponse NUNCA lleva la clave
├── service/
│   ├── UsuarioService.java           Reglas de negocio (quién puede qué, validaciones, enviar correo)
│   ├── Validaciones.java             RUT (módulo 11), correo, nombre, teléfono, fecha, clave segura
│   ├── ClaveService.java             Clave temporal con SecureRandom + hash PBKDF2 con sal
│   └── AlmacenamientoService.java    Fotos de perfil
├── client/NotificacionClient.java    WebClient hacia ms-notificaciones 
├── controller/UsuarioController.java Capa delgada: recibe HTTP y llama al servicio
└── config/DatosIniciales.java        Precarga los 3 roles y los 15 usuarios de la app (solo si la BD está vacía)
```

## Cómo levantarlo (Visual Studio Code)

1. Requisitos: JDK 21, extensiones **Extension Pack for Java** y **Spring Boot Extension Pack**, y **MySQL** corriendo en el puerto 3306
   (Laragon, XAMPP o MySQL Server; usuario `root` sin clave).
   La base de datos se crea sola (`createDatabaseIfNotExist=true`).
2. **Primero** levanta `ms-notificaciones` (con `MAIL_USERNAME` y `MAIL_PASSWORD`. Sin él, crear cuentas y recuperar claves responde 503.
3. En otra terminal de VS Code, dentro de la carpeta `ms-usuarios`:

```powershell
.\mvnw spring-boot:run
```

Al iniciar por primera vez se cargan los **3 roles** y los **15 usuarios** que ya tenía la app, con los mismos ids (1 al 15).
Todos tienen la clave de demostración `Tem_sin2026%` (guardada como hash, no en texto). Andrés Bravo (id 13, liquidador) parte **bloqueado**.

## Endpoints — `http://localhost:8081`

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| GET | `/api/usuarios?rol=2&buscar=texto` | — | Lista (ambos filtros opcionales) |
| GET | `/api/usuarios/{id}` | — | Usuario · 404 |
| POST | `/api/usuarios/asegurados` | `{"solicitanteId","rut","correo"}` | 201 · 400 · 403 (no es liquidador) · 409 RUT/correo repetido · 502/503 correo falló |
| POST | `/api/usuarios/liquidadores` | `{"solicitanteId","rut","nombre","correo","equipo"}` | 201 · 400 · 403 (no es analista) · 409 · 502/503 |
| POST | `/api/usuarios/login` | `{"rut","contrasena"}` | 200 usuario · 401 incorrectos · 403 cuenta bloqueada |
| POST | `/api/usuarios/recuperar-clave` | `{"correo"}` | 202 (siempre igual, exista o no el correo) · 400 · 502/503 |
| PUT | `/api/usuarios/{id}/perfil` | `{"nombre","correo","telefono","comuna","fechaNacimiento"}` | 200 · 400 · 404 · 409 |
| PUT | `/api/usuarios/{id}/contrasena` | `{"actual","nueva"}` | 204 · 400 · 401 |
| PUT | `/api/usuarios/{id}/estado` | `{"solicitanteId","activo"}` | 200 · 403 · 404 (`activo:false` bloquea, `true` habilita) |
| POST | `/api/usuarios/{id}/avatar` | multipart, campo `archivo` | 200 usuario con `avatarUrl` |
| GET | `/api/usuarios/{id}/avatar` | — | La imagen · 404 |



Respuesta de un usuario (nunca incluye la contraseña):

```json
{
  "id": 9, "rut": "162345672", "nombre": "Paulina Fernández Ruiz", "correo": "paulina.fernandez@andes.ejemplo",
  "comuna": "", "telefono": "", "fechaNacimiento": "", "codigo": "LIQ-01", "equipo": "Vehículos",
  "activo": true, "claveTemporal": false, "perfilCompleto": false,
  "rolId": 2, "rolNombre": "Liquidador", "avatarUrl": null
}
```

## Probarlo (PowerShell)

Ids de la precarga: analista Natalia = **14**, liquidadora Paulina = **9**.

```powershell
# 1) El analista (id 14) crea un liquidador. Usa TU correo real para ver llegar el mensaje.
Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/usuarios/liquidadores -ContentType "application/json; charset=utf-8" `
  -Body '{"solicitanteId":14,"rut":"12.345.678-5","nombre":"Pedro Soto Rivas","correo":"TU_CORREO@gmail.com","equipo":"Hogar"}'

# 2) Bloquearlo (suponiendo que quedó con id 16) y comprobar que ya no puede entrar
Invoke-RestMethod -Method Put -Uri http://localhost:8081/api/usuarios/16/estado -ContentType "application/json" -Body '{"solicitanteId":14,"activo":false}'

# 3) Una liquidadora (id 9) NO puede bloquear: debe responder 403
Invoke-RestMethod -Method Put -Uri http://localhost:8081/api/usuarios/16/estado -ContentType "application/json" -Body '{"solicitanteId":9,"activo":true}'

# 4) Login con una cuenta precargada
Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/usuarios/login -ContentType "application/json" -Body '{"rut":"16.234.567-2","contrasena":"Tem_sin2026%"}'

# 5) Recuperar clave solo con el correo (cualquier rol)
Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/usuarios/recuperar-clave -ContentType "application/json" -Body '{"correo":"TU_CORREO@gmail.com"}'
```

(El RUT `12.345.678-5` es válido por el módulo 11; si ya lo usaste, prueba con otro.)




## Desde la app Android

- Emulador: `http://10.0.2.2:8081/` (`10.0.2.2` = el `localhost` del computador). Celular físico: la IP del computador en la misma red WiFi.
- Como es `http`, la app debe permitir tráfico sin cifrar (`android:usesCleartextTraffic="true"`;).
- La app **no** llama a `ms-notificaciones`: lo llama `ms-usuarios` (cuando la app migre al servidor).


