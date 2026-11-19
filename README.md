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
10.Ir a la pertaña puertos y agregar el 8084 debe ser publico
11.Copiar la direccion en el ClienteRetrofit del Android
12.Conectar el celu a android
