# MiniTFG - App Educativa Android

Este proyecto es una aplicación educativa para Android desarrollada como parte de la asignatura de Desarrollo de Aplicaciones Móviles.

## Características

*   **Juego de Preguntas**: Soporte para múltiples asignaturas (Matemáticas, Lengua, Inglés, Conocimiento del medio).
*   **Sistema de Alarmas**: Configuración de recordatorios de estudio semanales.
*   **Rankings**: Sistema de puntuaciones integrado con Firebase Firestore.
*   **Autenticación**: Registro e inicio de sesión de usuarios con Firebase Auth.

## Configuración del Proyecto

*   Para ejecutar este proyecto, necesitas realizar un comando "npm start" dentro de la carpeta de backend.

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
