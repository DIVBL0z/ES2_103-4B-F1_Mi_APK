# IoT - FirstApp Android

Aplicación móvil nativa para Android desarrollada en **Kotlin** con **Android Studio** y **Gradle (Kotlin DSL)**.

---

## 📅 Registro de Actividades y Cambios

### Fecha: 28 de agosto de 2026

#### 1. Análisis y Diagnóstico Inicial del Repositorio
* Inspección de la arquitectura base del proyecto Android.
* Identificación de dependencias y configuración en `libs.versions.toml` y `app/build.gradle.kts` (SDK 24 a 37, Kotlin, Java 11).
* Explicación del ciclo de vida y punto de entrada (`AndroidManifest.xml` y `MainActivity.kt`).

#### 2. Desarrollo de la Interfaz de Usuario (UI - `activity_main.xml`)
Se diseñó la pantalla de inicio de sesión utilizando `ConstraintLayout`:
* **Logo institucional (`ImageView`):**
  * Recurso: `@drawable/logo_inacap`.
  * Dimensiones: Centrado horizontalmente con margen superior de `32dp`.
* **Título (`TextView`):**
  * Texto: `"Iniciar sesión"`.
  * Estilo: Negrita (`bold`), tamaño `24sp`, centrado debajo del logo.
* **Campo de Usuario (`EditText` - `edtUsuario`):**
  * Hint: `"Usuario"`.
  * Ancho completo (`0dp`) con márgenes laterales de `24dp`.
* **Campo de Contraseña (`EditText` - `edtPassword`):**
  * Hint: `"Contraseña"`.
  * Tipo de entrada: `textPassword` (ocultamiento automático de caracteres).
  * Márgenes laterales de `24dp` y separación superior de `16dp`.
* **Casilla de Verificación (`CheckBox` - `chkRecordarme`):**
  * Texto: `"Recordarme"`.
  * Posicionado debajo del campo de contraseña con margen alineado.
* **Botón de Acción (`Button` - `btnIngresar`):**
  * Texto: `"Ingresar"`.
  * Ancho completo con márgenes de `24dp`.

#### 3. Implementación de Lógica en Kotlin (`MainActivity.kt`)
* Vinculación de los elementos de la interfaz por sus IDs (`edtUsuario`, `edtPassword`, `chkRecordarme`, `btnIngresar`).
* Configuración del listener de clic (`setOnClickListener`) en `btnIngresar`.
* Lectura del estado de `chkRecordarme` y de los textos de usuario y contraseña.
* Validación de campos obligatorios: si alguno de los campos está vacío, se despliega un mensaje flotante (`Toast`) con el texto:
  > *"Completa usuario y contraseña"*

#### 4. Control de Versiones y Despliegue en GitHub
* Inicialización del repositorio Git local en la rama `main`.
* Configuración de reglas de exclusión en `.gitignore`.
* Creación del commit inicial con todos los recursos y código fuente.
* Vinculación con el repositorio remoto `https://github.com/DIVBL0z/IoT.git`.
* Integración y subida (`push`) de los cambios a GitHub.

---

## 🛠️ Tecnologías y Requisitos

* **Lenguaje:** Kotlin
* **Plataforma:** Android (Min SDK: 24 | Target SDK: 37 | Compile SDK: 37)
* **Gestor de Compilación:** Gradle Kotlin DSL (`.gradle.kts`) con Version Catalogs (`libs.versions.toml`)
* **Librerías principales:**
  * AndroidX Core KTX
  * AndroidX AppCompat
  * AndroidX ConstraintLayout
  * Google Material Components 3