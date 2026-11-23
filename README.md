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


Base de datos para probar:
    "(1, 'Camila Andrea Rojas Fuentes', '158423715', 'camila.rojas@correo.ejemplo', '', 'Maipú', '', '', 1, 1), " +
    "(2, 'Jorge Luis Pizarro Contreras', '94185521', 'jorge.pizarro@correo.ejemplo', '', 'Puente Alto', '', '', 1, 1), " +
    "(3, 'Valentina Soto Muñoz', '192058848', 'vale.soto@correo.ejemplo', '', 'Ñuñoa', '', '', 1, 1), " +
    "(4, 'Héctor Manuel Araya Díaz', '78362197', 'h.araya@correo.ejemplo', '', 'Viña del Mar', '', '', 1, 1), " +
    "(5, 'Daniela Paz Vergara Lagos', '175520937', 'daniela.vergara@correo.ejemplo', '', 'La Florida', '', '', 1, 1), " +
    "(6, 'Ricardo Ignacio Espinoza Tapia', '136647083', 'r.espinoza@correo.ejemplo', '', 'Concepción', '', '', 1, 1), " +
    "(7, 'Francisca Belén Morales Henríquez', '201184657', 'fran.morales@correo.ejemplo', '', 'Quilicura', '', '', 1, 1), " +
    "(8, 'Luis Alberto Carrasco Peña', '112903372', 'luis.carrasco@correo.ejemplo', '', 'Rancagua', '', '', 1, 1), " +
    "(9, 'Paulina Fernández Ruiz', '162345672', 'paulina.fernandez@andes.ejemplo', '', '', 'LIQ-01', 'Vehículos', 1, 2), " +
    "(10, 'Matías Gallardo Silva', '138765431', 'matias.gallardo@andes.ejemplo', '', '', 'LIQ-02', 'Hogar', 1, 2), " +
    "(11, 'Rodrigo Salinas Ortega', '183459120', 'rodrigo.salinas@andes.ejemplo', '', '', 'LIQ-03', 'Vehículos', 1, 2), " +
    "(12, 'Constanza Ibarra Núñez', '145678900', 'constanza.ibarra@andes.ejemplo', '', '', 'LIQ-04', 'Hogar', 1, 2), " +
    "(13, 'Andrés Bravo Medina', '11223344K', 'andres.bravo@andes.ejemplo', '', '', 'LIQ-05', 'Vehículos', 0, 2), " +
    "(14, 'Natalia Reyes Campos', '198761230', 'natalia.reyes@andes.ejemplo', '', '', 'ADM-01', 'Administración', 1, 3), " +
    "(15, 'Felipe Cortés Valdés', '109876542', 'felipe.cortes@andes.ejemplo', '', '', 'ADM-02', 'Administración', 1, 3)"

    