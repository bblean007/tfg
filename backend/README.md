# MiniTfg Backend API

Backend simple en Node.js con SQLite para autenticación de usuarios.

## Requisitos
- Node.js >= 16
- Docker (opcional)

## Instalación Local
1.  Entra en la carpeta `backend`:
    ```bash
    cd backend
    ```
2.  Instala las dependencias:
    ```bash
    npm install
    ```
3.  Ejecuta el servidor:
    ```bash
    npm start
    ```
    El servidor correrá en `http://localhost:3000`.

## Endpoints API

### 1. Registro
- **URL:** `/register`
- **Método:** `POST`
- **Body:**
  ```json
  {
    "email": "usuario@test.com",
    "password": "password123"
  }
  ```
- **Respuesta:** JSON con token JWT.

### 2. Login
- **URL:** `/login`
- **Método:** `POST`
- **Body:**
  ```json
  {
    "email": "usuario@test.com",
    "password": "password123"
  }
  ```
- **Respuesta:** JSON con token JWT.

### 3. Validar Token (Me)
- **URL:** `/me`
- **Método:** `GET`
- **Headers:** `Authorization: Bearer <TU_TOKEN>`

## Despliegue en la Nube

### Opción A: Render (Gratis y Fácil)
1.  Sube este código a GitHub.
2.  Crea una cuenta en [Render](https://render.com/).
3.  Crea un "New Web Service".
4.  Conecta tu repositorio.
5.  Render detectará automáticamente que es una app Node.js.
6.  **Importante:** SQLite almacena los datos en un archivo local (`database.sqlite`). En los planes gratuitos de Render/Heroku, el sistema de archivos es efímero (se borra si la app se reinicia). Para persistencia real en producción, deberías usar un "Disk" (servicio de pago) o conectar una base de datos externa (PostgreSQL). **Para este TFG, ten en cuenta que los datos pueden borrarse al reiniciarse el servidor gratuito.**

### Opción B: Railway (Mejor para SQLite)
1.  En [Railway](https://railway.app/), crea un proyecto desde GitHub.
2.  Añade un "Volume" y móntalo en `/usr/src/app` para que el archivo `database.sqlite` no se pierda.

### Variables de Entorno
Configura estas variables en tu panel de control de la nube:
- `SECRET_KEY`: Una cadena larga y segura para firmar los tokens JWT.
