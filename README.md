# IoT - FirstApp Android

Aplicación móvil nativa para Android desarrollada en **Kotlin** con **Android Studio** y **Gradle (Kotlin DSL)**.

---

## 📅 Registro de Actividades y Cambios

### Fecha: 3 de septiembre de 2026

#### 1. Mejoras en la Pantalla de Login (`MainActivity.kt` y `activity_main.xml`)
* **Botón de Limpiar Formulario (`btnLimpiar`):**
  * Agregado debajo del botón "Ingresar" con el atributo `android:onClick="onLimpiarClick"`.
  * Vacía los campos `edtUsuario` y `edtPassword`, desmarca `chkRecordarme`, restablece la visibilidad de la contraseña y remueve mensajes de error previos.
* **Alternancia de Visibilidad de Contraseña (`btnMostrarPassword`):**
  * `ImageButton` con fondo transparente posicionado en el extremo derecho de `edtPassword`.
  * Recursos vectoriales: `@drawable/ic_visibility_24` (ojo abierto) y `@drawable/ic_visibility_off_24` (ojo tachado).
  * Función booleana `alternarVisibilidadPassword` que conmuta entre `HideReturnsTransformationMethod` y `PasswordTransformationMethod` manteniendo la posición del cursor al final del texto.
* **Contador de Intentos Fallidos:**
  * Variable `intentosFallidos: Int = 0` declarada a nivel de clase.
  * Se incrementa automáticamente en cada intento fallido de validación.
* **Validaciones Avanzadas y Errores Directos:**
  * Validación de formato de correo electrónico mediante `Patterns.EMAIL_ADDRESS`.
  * Validación de longitud mínima para la contraseña (al menos 6 caracteres).
  * Mensajes de error aplicados directamente en las vistas mediante la propiedad `.error` (`edtUsuario.error` y `edtPassword.error`) en lugar de mensajes genéricos.
* **Navegación Explícita:**
  * Al superar exitosamente las validaciones, se inicia `BienvenidaActivity` mediante un `Intent`, transfiriendo el correo/usuario como dato extra (`EXTRA_USUARIO`).

#### 2. Pantalla de Bienvenida (`BienvenidaActivity.kt` y `activity_bienvenida.xml`)
* Diseño con `ConstraintLayout` que presenta un mensaje de bienvenida centrado en pantalla (`tvBienvenida`).
* Botón de navegación a preferencias (`btnPreferencias` con `android:onClick="onPreferenciasClick"`).
* Método `onPreferenciasClick`: abre `PreferenciasActivity` transfiriendo el usuario recibido.

#### 3. Pantalla de Preferencias de Usuario (`PreferenciasActivity.kt` y `activity_preferencias.xml`)
* **Estructura:** Contenedor `LinearLayout` vertical a ancho completo ubicado debajo del título.
* **Componentes integrados:**
  * **Notificaciones:** Casilla tipo switch (`Switch` - `swNotificaciones`) con el texto *"Recibir notificaciones"*.
  * **Idioma:** Selector desplegable (`Spinner` - `spIdioma`) enlazado a un nuevo arreglo de recursos (`string-array` `idiomas` en `strings.xml` con *"Español"* e *"Inglés"*).
  * **Unidad de temperatura:** Grupo de opciones excluyentes (`RadioGroup` - `rgUnidad`) con botones `rbCelsius` (*"Celsius"*, marcado por defecto) y `rbFahrenheit` (*"Fahrenheit"*).
  * **Simulación de guardado asíncrono:**
    * Indicador de progreso pequeño (`ProgressBar` - `pbGuardando`, estilo `progressBarStyleSmall`, inicialmente `android:visibility="gone"`).
    * Botón de acción (`Button` - `btnGuardarPreferencia` con `android:onClick="onGuardarPreferenciaClick"`).
    * Método `onGuardarPreferenciaClick`: hace visible el `ProgressBar`, detecta la opción elegida en el `RadioGroup`, programa una espera de 1 segundo mediante `Handler(Looper.getMainLooper()).postDelayed`, oculta el `ProgressBar` y despliega un `Toast` confirmando la unidad seleccionada.
  * **Cierre de sesión seguro:**
    * Botón `btnCerrarSesion` con `android:onClick="onCerrarSesionClick"`.
    * Limpieza completa de la pila de actividades mediante `Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK`, cerrando todas las vistas abiertas y retornando de forma directa a la pantalla de inicio de sesión (`MainActivity`).

#### 4. Documentación y Limpieza del Proyecto
* Comentarios explicativos exhaustivos línea por línea en todo el código Kotlin y archivos XML.
* Registro de todas las actividades (`MainActivity`, `BienvenidaActivity`, `PreferenciasActivity`) en `AndroidManifest.xml`.
* Actualización de `.gitignore` para omitir por completo el directorio de configuración local `.idea/`.

---

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