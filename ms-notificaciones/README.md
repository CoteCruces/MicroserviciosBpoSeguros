# ms-notificaciones

Microservicio Spring Boot 4.1 (JDK 21, Maven) de BPO Seguros. **Una sola responsabilidad:** enviar correos reales
desde `bpo.seguros2026@gmail.com` hacia el correo del cliente (clave temporal al crear cuenta y al recuperarla).
No tiene base de datos: solo recibe una petición HTTP y envía el correo por SMTP de Gmail.

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| `ms-notificaciones` | 8084 | Envío real de correos |

## Estructura 

```
src/main/java/cl/bposeguros/ms_notificaciones/
├── MsNotificacionesApplication.java      Punto de entrada (@SpringBootApplication)
├── controller/NotificacionController.java  Recibe la petición HTTP (capa delgada)
├── service/CorreoService.java              Valida, arma el texto y envía el correo
└── dto/
    ├── ClaveTemporalRequest.java           JSON de entrada (record)
    └── RespuestaDto.java                   JSON de salida (record)
src/main/resources/application.properties   Puerto, SMTP de Gmail y variables de entorno
```

## Cómo levantarlo (Visual Studio Code)

1. Requisitos: JDK 21 o superior, y las extensiones **Extension Pack for Java** y **Spring Boot Extension Pack**.
2. Crear una **contraseña de aplicación** de Gmail para `bpo.seguros2026@gmail.com`
   (Cuenta de Google → Seguridad → Verificación en 2 pasos activada → Contraseñas de aplicaciones). Son 16 letras, sin espacios.
3. En una terminal de VS Code (PowerShell), dentro de la carpeta del proyecto:

```powershell
$env:MAIL_USERNAME="bpo.seguros2026@gmail.com"
$env:MAIL_PASSWORD="las16letrassinespacios"
.\mvnw spring-boot:run
```

Las variables viven solo en esa terminal; la contraseña **nunca** se escribe en el código ni se sube a GitHub.

### Alternativa: botón Run de VS Code

Crear `.vscode/launch.json` (esa carpeta está en `.gitignore`, así que no se sube):

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "MsNotificacionesApplication",
      "request": "launch",
      "mainClass": "cl.bposeguros.ms_notificaciones.MsNotificacionesApplication",
      "env": {
        "MAIL_USERNAME": "bpo.seguros2026@gmail.com",
        "MAIL_PASSWORD": "las16letrassinespacios"
      }
    }
  ]
}
```

## Endpoint — `http://localhost:8084`

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| POST | `/api/notificaciones/clave-temporal` | `{"correo","claveTemporal","motivo","rol"}` | 202 `{"mensaje":"Correo enviado"}` · 400 datos inválidos · 502 Gmail falló |

`motivo` es `CREACION` o `RECUPERACION` (cambia el asunto y la introducción del correo).
`rol` es **opcional**: `ASEGURADO` (por defecto), `LIQUIDADOR` o `ANALISTA`. Solo cambia el texto del correo de bienvenida
(un liquidador no recibe "para que sigas tus siniestros", sino "para que gestiones los siniestros que se te asignen").

## Probarlo

PowerShell (con tu propio correo):

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8084/api/notificaciones/clave-temporal `
  -ContentType "application/json" `
  -Body '{"correo":"TU_CORREO@gmail.com","claveTemporal":"Prueba#2026","motivo":"CREACION"}'
```

Casos de error para probar en Postman:

- Correo sin `@` → **400** "El correo no es válido"
- `motivo` distinto de CREACION/RECUPERACION → **400**
- `rol` distinto de ASEGURADO/LIQUIDADOR/ANALISTA → **400**
- Contraseña de aplicación incorrecta → **502** "No se pudo enviar el correo. Inténtalo más tarde"

## Desde la app Android

- Emulador: `http://10.0.2.2:8084/` (`10.0.2.2` = el `localhost` del computador).
- Celular físico: la IP del computador en la misma red WiFi.
- Como es `http`, la app debe permitir tráfico sin cifrar (`android:usesCleartextTraffic="true"`).
- En el diseño final, la app no llama directo a este servicio: lo llamará `msUsuarios` al crear la cuenta o recuperar la clave.
