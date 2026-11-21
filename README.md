# MicroserviciosBpoSeguros
Microservicios para la app de BpoSeguros Proyecto semestral

pasos para hacer correr la app desde el celu:

1.Crear la siguiente bd en xampp: bposeguros_usuarios y con este desplegable de la derecha (elegir utf8mb4_unicode_ci)  y Crear

2.Abrir una nueva terminal con el + y escribir comando 
cd ms-notificaciones

3.Pegar este comando:

$env:MAIL_USERNAME="bpo.seguros2026@gmail.com"
$env:MAIL_PASSWORD="las16letrassinespacios"
.\mvnw spring-boot:run

4.Enter

5.Sin cerrar esa terminal abrir otra, y posicionarse en el ms-usuario
con el comando cd ms-usuarios    ENTER

6.Arrancar con: .\mvnw spring-boot:run    ENTER

7.Comprobar que se creaon las tablas en el xampp en usuarios deben verse las 15 filas de los precargados

8.Abrir una tercera terminal y pegar este comando:

Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/usuarios/login -ContentType "application/json; charset=utf-8" -Body '{"rut":"16.234.567-2","contrasena":"Tem_sin2026%"}'

9.Responderá con los datos de Paulina Fernández.

10.Ir a la pestaña puertos y agregar el 8084 debe ser publico

11.Copiar la direccion en el ClienteRetrofit del Android

12.Conectar el celu a android



Microservicio	          Puerto	Base de datos MySQL	          Responsabilidad

ms-usuarios	               8081	    bposeguros_usuarios	          Usuarios, roles, login, claves, bloquear/habilitar

ms-catalogos               8082	    bposeguros_catalogos	      Comunas, compañías, tipos de siniestro

ms-siniestros-documentos   8083	    bposeguros_siniestros	      Siniestros, asignaciones, gestiones, solicitudes y 
                                                                  archivos, avisos

ms-notificaciones          8084	    no usa BD	Enviar correos




¿CÓMO PROBAR CADA COSA EN LA TERMINAL?

# Catálogos
Invoke-RestMethod "http://localhost:8082/api/companias?linea=HOGAR"
Invoke-RestMethod "http://localhost:8082/api/tipos-siniestro?linea=VEHICULO"

# Siniestros
Invoke-RestMethod "http://localhost:8083/api/siniestros?liquidadorId=9"
Invoke-RestMethod "http://localhost:8083/api/siniestros?sinAsignar=true"
Invoke-RestMethod "http://localhost:8083/api/siniestros?aseguradoId=1&limite=2&orden=reporte"

# Punto 3: casos activos y tiempo de Paulina (id 9)
Invoke-RestMethod "http://localhost:8083/api/liquidadores/9/casos-activos"

# Punto 6: historial de Rodrigo (id 11): últimos 3, y por rango
Invoke-RestMethod "http://localhost:8083/api/liquidadores/11/historial?limite=3"
Invoke-RestMethod "http://localhost:8083/api/liquidadores/11/historial?desde=2026-07-01&hasta=2026-07-31"

# Punto 4: bloquear a Paulina (id 9) y liberar sus casos. El analista Natalia tiene id 14
Invoke-RestMethod -Method Put -Uri http://localhost:8081/api/usuarios/9/estado -ContentType "application/json; charset=utf-8" -Body '{"solicitanteId":14,"activo":false}'
Invoke-RestMethod -Method Post -Uri http://localhost:8083/api/liquidadores/9/liberar-casos -ContentType "application/json; charset=utf-8" -Body '{"solicitanteId":14}'
Invoke-RestMethod "http://localhost:8083/api/siniestros?porReasignar=true"

# Reasignar el caso 1 (Vehículos) a Rodrigo (id 11, equipo Vehículos)
Invoke-RestMethod -Method Put -Uri http://localhost:8083/api/siniestros/1/asignacion -ContentType "application/json; charset=utf-8" -Body '{"solicitanteId":14,"liquidadorId":11}'