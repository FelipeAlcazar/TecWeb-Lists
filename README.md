# SplitList

Este proyecto es una aplicación web para la gestión de lista de la compra con objetivo de aprendiaje.

Incluye un frontend desarrollado en Angular y dos backends desarrollados en Java con Spring Boot. El proyecto permite a los usuarios crear listas de compras, agregar productos, invitar a otros usuarios a colaborar en las listas y realizar pagos.

<img width="300" height="300" alt="splitlist" src="https://github.com/user-attachments/assets/f5cf94bc-baf5-4cf8-8063-7140ab07cf31" />

## Estructura del Proyecto

### BackendListas/
Este directorio contiene el backend para la gestión de listas de compras.

### backendUsuarios/
Este directorio contiene el backend para la gestión de usuarios y pagos.

### Frontend/
Este directorio contiene el frontend de la aplicación desarrollado en Angular.

### Test_Jmeter/
Este directorio contiene archivos de prueba para JMeter.

## Pruebas con Selenium

El archivo [`backendUsuarios/src/test/java/SeleniumTest.java`](backendUsuarios/src/test/java/SeleniumTest.java) contiene pruebas automatizadas utilizando Selenium. Estas pruebas verifican el flujo completo de registro, inicio de sesión, creación de listas, y la colaboración entre usuarios.

Para ejecutar las pruebas de Selenium:
1. Asegúrate de tener el driver de Selenium configurado correctamente.
2. Ejecuta las pruebas utilizando tu entorno de desarrollo o una herramienta de integración continua.

## Pruebas con JMeter

El directorio [`Test_Jmeter`](Test_Jmeter) contiene archivos de prueba para JMeter que permiten verificar el rendimiento y la funcionalidad de la aplicación.

### Archivos de Prueba

- **tweb.jmx**: Archivo de prueba de JMeter que define los escenarios de prueba.
- **usuarios.csv**: Archivo CSV que contiene datos de prueba para ser utilizados en los escenarios de prueba.

### Ejecución de Pruebas

Para ejecutar las pruebas con JMeter, sigue estos pasos:

1. Abre JMeter en tu máquina.
2. Carga el archivo de prueba [`tweb.jmx`](Test_Jmeter/tweb.jmx) en JMeter.
3. Asegúrate de que el archivo [`usuarios.csv`](Test_Jmeter/usuarios.csv) esté en la ubicación correcta y configurado en el plan de prueba.
4. Configura los parámetros necesarios, como la URL del servidor y los hilos de usuarios.
5. Ejecuta las pruebas para verificar el rendimiento y la funcionalidad de la aplicación.

## Instrucciones de Ejecución

### Backend
1. Navega a los directorios [`BackendListas`](BackendListas) y [`backendUsuarios`](backendUsuarios).
2. Ejecuta `mvn spring-boot:run` para iniciar los servidores backend.

### Frontend
1. Navega al directorio [`Frontend`](Frontend).
2. Ejecuta `npm install` para instalar las dependencias.
3. Ejecuta `ng serve` para iniciar el servidor de desarrollo de Angular.

## Bases de Datos MySQL para pruebas localizadas

### BackendListas
- Nombre de la base de datos: `lista`
- Configuración de conexión: 
  - URL: `jdbc:mysql://localhost:3306/lista?serverTimezone=UTC`
  - Usuario: `listacompra`
  - Contraseña: `listacompra`

### backendUsuarios
- Nombre de la base de datos: `usuarioslistacompra`
- Configuración de conexión: 
  - URL: `jdbc:mysql://localhost:3306/usuarioslistacompra?serverTimezone=UTC`
  - Usuario: `listacompra`
  - Contraseña: `listacompra`

## Desarrolladores

- Felipe Álcazar Gómez
- Alonso Crespo Fernández
