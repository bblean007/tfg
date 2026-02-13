# Documentación del Proyecto miniTFG

Este documento describe las mejoras implementadas en la aplicación móvil miniTFG, centradas en la seguridad, accesibilidad y experiencia de usuario.

## 1. Sistema de Autenticación Mejorado

### Registro de Usuarios
- **Campos obligatorios**: Email, Nombre de Usuario y Contraseña.
- **Validación en tiempo real**: Los campos se validan mientras el usuario escribe, proporcionando feedback inmediato mediante `TextInputLayout`.
- **Requisitos de seguridad**:
    - Email: Formato válido.
    - Usuario: Al menos 3 caracteres alfanuméricos sin espacios.
    - Contraseña: Mínimo 8 caracteres, incluyendo mayúscula, minúscula y número.

### Inicio de Sesión (Login)
- **Identificador Dual**: Permite entrar con email o nombre de usuario.
- **Seguridad contra ataques**: Límite de 5 intentos fallidos. Tras el quinto error, la cuenta se bloquea durante 5 minutos.
- **Persistencia**: Uso de JWT (JSON Web Tokens) para mantener la sesión activa de forma segura.
- **Gestión de Sesión**: Un interceptor global en Retrofit maneja automáticamente el cierre de sesión si el token expira (error 401).

### Recuperación de Contraseña
- Funcionalidad para solicitar un correo de recuperación desde la pantalla de login.

## 2. Sistema de Ayuda Emergente

Se ha implementado un sistema de ayuda contextual accesible desde todas las pantallas principales mediante un botón flotante (FAB) con el icono `help.jpg`.

- **Contextual**: Cada pantalla muestra información específica sobre sus funciones.
- **Animaciones**: Los diálogos de ayuda aparecen y desaparecen con transiciones suaves de desvanecimiento (Fade In/Out).
- **Accesibilidad**: Descripciones claras y formato de lista para facilitar la lectura.

## 3. Requisitos Técnicos y Seguridad

### Backend
- **Encriptación**: Las contraseñas se almacenan usando `bcrypt` con 10 rondas de salting.
- **Logs de Auditoría**: Cada evento importante (registro, login exitoso, fallo de login, bloqueo) se registra en una tabla `audit_logs` con marca de tiempo.
- **JWT**: Autenticación basada en tokens firmados para todas las peticiones protegidas.

### Android
- **Material Design**: Uso de componentes Material (Buttons, TextInputLayout, FloatingActionButton) para una UI moderna y accesible.
- **Estado Global**: Gestión centralizada de la sesión a través de `SessionManager` y `ApiClient`.

## 4. Pruebas Unitarias

Se han incluido pruebas unitarias para validar la lógica crítica:
- `ValidationUtilsTest`: Verifica las reglas de validación de email, usuario y contraseña.
- `SessionManagerTest`: Asegura que la sesión se guarde y recupere correctamente de `SharedPreferences`.
- `ShakeDetectorTest`: Valida la detección de movimiento para el control de alarmas.

## 5. Manual de Usuario Rápido

1. **Registro**: Crea tu cuenta desde la pantalla inicial. Asegúrate de cumplir los requisitos de contraseña.
2. **Login**: Entra con tus credenciales. Si olvidas tu contraseña, usa el enlace inferior.
3. **Ayuda**: Si tienes dudas en cualquier pantalla, pulsa el botón redondo con el icono de interrogación en la esquina inferior derecha.
4. **Juego**: Selecciona una asignatura y responde preguntas. ¡Recuerda descansar la vista cada 5 minutos!
5. **Ajustes**: Cambia al Modo Oscuro si prefieres una interfaz más oscura.
6. **Alarmas**: Configura tus recordatorios de estudio y actívalos/desactívalos rápidamente agitando el teléfono en el menú principal.
