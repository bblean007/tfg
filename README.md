# StudySprint - App Educativa Android

Este proyecto es una aplicación educativa para Android desarrollada como parte de la asignatura de Desarrollo de Aplicaciones Móviles.

## Características

*   **Juego de Preguntas**: Soporte para múltiples asignaturas (Matemáticas, Lengua, Inglés, Conocimiento del medio).
*   **Sistema de Alarmas**: Configuración de recordatorios de estudio semanales.
*   **Rankings**: Sistema de puntuaciones integrado con Firebase Firestore.
*   **Autenticación**: Registro e inicio de sesión de usuarios con Firebase Auth.

## Configuración del Proyecto

Para ejecutar este proyecto, necesitas configurar Firebase:

1.  Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
2.  Añade una aplicación Android con el paquete `com.example.minitfg`.
3.  Descarga el archivo `google-services.json`.
4.  Coloca el archivo `google-services.json` en la carpeta `app/` del proyecto.
5.  Habilita **Authentication** (Email/Password) y **Firestore Database** en la consola de Firebase.

## Requisitos

*   Android Studio
*   JDK 11 o superior
*   Dispositivo Android o Emulador (API 24+)

## Estructura del Proyecto

*   `MainActivity`: Pantalla principal y menú.
*   `GameActivity`: Lógica del juego y preguntas.
*   `AlarmConfigActivity`: Configuración de alarmas.
*   `RankingsActivity`: Visualización de puntuaciones.
*   `LoginActivity` / `RegisterActivity`: Gestión de usuarios.

## Notas

Este repositorio no incluye el archivo `google-services.json` por razones de seguridad.
