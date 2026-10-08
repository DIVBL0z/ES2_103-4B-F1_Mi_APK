# IoT - FirstApp Android

Aplicación móvil nativa para Android desarrollada en **Kotlin** con **Android Studio** y **Gradle (Kotlin DSL)**.

---

## 📅 Registro de Actividades y Cambios

### Fecha: 8 de octubre de 2026

#### 1. Organización del código en paquetes
- El código Kotlin se reorganizó en los paquetes `models`, `views`, `adapters` y `utils`.
- Las pantallas existentes (`MainActivity`, `RegistroActivity`, `BienvenidaActivity`, `PreferenciasActivity`) pasaron a `views` y `Usuario` a `models`; el `AndroidManifest.xml` se actualizó con las nuevas rutas.

#### 2. CRUD de registros de temperatura de servidores
- **Modelo (`models/RegistroTemperatura.kt`):** `ubicacion` (texto), `temperatura` (numérico, °C) y `hora` (texto), con el id anotado con `@DocumentId`.
- **Colección de Firestore:** `registros_temperatura`.
- **Lectura en tiempo real (`views/ListaActivity.kt` y `adapters/RegistroAdapter.kt`):** la lista se actualiza sola mediante `addSnapshotListener`, que se activa en `onStart` y se libera en `onStop`. Cada fila muestra *Sala*, *Temperatura (°C)* y *Hora del registro*.
- **Crear y editar (`views/FormularioActivity.kt`):** un mismo formulario sirve para ambos casos. Valida que ningún campo esté vacío y que la temperatura sea un número antes de enviar nada a Firebase, mostrando el error con `.error`. Al editar se actualiza el mismo documento (mismo id).
- **Eliminar:** botón en cada fila con diálogo de confirmación.
- **Navegación:** nuevo botón *"Ver registros de temperatura"* en la pantalla de Bienvenida.
- **Dependencia nueva:** `androidx.recyclerview:recyclerview`.
- **Reglas (`firestore.rules`):** se agregó el bloque de `registros_temperatura` (lectura y escritura solo para usuarios autenticados).

#### 3. Alertas de temperatura
- **`utils/NotificadorTemperatura.kt`:** al guardar o editar un registro, se dispara una notificación si la temperatura supera el máximo (30 °C) o está bajo el mínimo (18 °C). Cada sala usa su propio id de notificación, así una alerta nueva reemplaza a la anterior de esa sala.
- Canal de notificaciones `alertas_temperatura` (Android 8+) y permiso `POST_NOTIFICATIONS`, que se solicita al abrir la lista (Android 13+).
- Al tocar la notificación se abre la lista de registros.

#### 4. Preferencias del negocio
- **Switch:** *"Recibir alertas de temperatura"*. Si está apagado, no se muestra ninguna notificación.
- **Spinner:** sala o ubicación monitoreada (Sala servidores 1, Sala servidores 2, Rack principal, UPS, Sala de red).
- Las preferencias se guardan en Firestore, en `/usuarios/{uid}/preferencias` (`notificaciones`, `salaMonitoreada`, `unidadTemperatura`), y el estado de las alertas también se guarda localmente (`SharedPreferences`, clave `alertas_activas`) para que el notificador lo lea al instante.

### Fecha: 24 de septiembre de 2026

#### 1. Integración de Firebase y Firebase Authentication
* **Configuración del Proyecto y Registro de Aplicación:**
  * Vinculación del proyecto de Firebase `iot-24-09-2026` mediante Firebase CLI.
  * Registro de la aplicación Android nativa con package name `com.example.firstapp` y App ID `1:172550727760:android:cef1ddbcb3ceeb0974c955`.
  * Generación y ubicación del archivo de configuración `app/google-services.json`.
  * Creación y despliegue de la configuración backend de autenticación en `firebase.json` activando el proveedor `emailPassword`.

#### 2. Configuración de Build y Dependencias Gradle
* **Catálogo de Versiones (`gradle/libs.versions.toml`):**
  * Plugin de Google Services: `com.google.gms.google-services` (v4.4.2).
  * Firebase Bill of Materials: `firebase-bom` (v33.10.0).
  * Librería de autenticación: `firebase-auth`.
* **Configuración de Gradle Scripts:**
  * Aplicación del plugin de Google Services en `build.gradle.kts` (raíz) y `app/build.gradle.kts`.
  * Inclusión de `platform(libs.firebase.bom)` e implementación de `libs.firebase.auth` en el módulo `:app`.
* **Permisos del Sistema (`AndroidManifest.xml`):**
  * Inclusión del permiso `android.permission.INTERNET` para permitir la comunicación segura con los servidores de Firebase Auth.

#### 3. Integración de Cloud Firestore y Creación de Perfiles de Usuario (Vía 1)
* **Aprovisionamiento de Cloud Firestore:**
  * Creación y despliegue de la base de datos `(default)` en el proyecto `iot-24-09-2026`.
  * Adición de la dependencia `firebase-firestore` mediante Version Catalogs en `gradle/libs.versions.toml` y `app/build.gradle.kts`.
  * Despliegue de reglas de seguridad estrictas en `firestore.rules` (validación de esquema, propiedad de datos `isOwner(userId)`, tipado y roles).
* **Modelo de Datos de Usuario (`Usuario.kt`):**
  * Representación estructurada del perfil en Firestore: `uid`, `correo`, `nombreCompleto`, `rut`, `telefono`, `rol` (*"usuario"*, *"tecnico"*, *"administrador"*), `fechaRegistro`, `activo` y `preferencias`.
* **Pantalla Dedicada de Registro (`RegistroActivity.kt` y `activity_registro.xml`):**
  * Formulario completo con validaciones: Nombre completo, RUT, Teléfono, Selector de Rol (`Spinner` con `@array/roles`), Correo electrónico, Contraseña y Confirmación de Contraseña.
  * Flujo transaccional dual: Crea el usuario en Firebase Authentication (`createUserWithEmailAndPassword`) y almacena el documento del perfil en `/usuarios/{uid}` en Cloud Firestore.
  * Declarada en `AndroidManifest.xml` y enlazada al botón *"Registrarse con Firebase"* de `MainActivity`.
* **Sincronización de Perfil y Preferencias:**
  * **Pantalla de Bienvenida (`BienvenidaActivity.kt` / `activity_bienvenida.xml`):** Consulta `/usuarios/{uid}` en Firestore para mostrar el nombre real del usuario, su rol asignado, correo y datos de contacto en tiempo real.
  * **Pantalla de Preferencias (`PreferenciasActivity.kt`):** Carga las preferencias del usuario desde Firestore y guarda los cambios (notificaciones, idioma, unidad de temperatura) directamente en la nube.

---

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
  * Firebase Android BoM & Firebase Authentication (Email/Password)
  * Google Play Services Plugin (Google Services Gradle Plugin)
  * Cloud Firestore (base de datos en tiempo real)
  * AndroidX RecyclerView
