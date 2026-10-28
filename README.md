# Recetario con Origen

App Android para guardar tus recetas de cocina y ver **de dónde viene cada plato** en un mapa.

## ¿Qué se puede hacer?

- **Crear una cuenta e iniciar sesión** con correo y contraseña.
- **Guardar recetas** a mano: nombre, ingredientes, pasos, tiempo, porciones y foto.
- **Importar una receta desde un link** (por ejemplo, un video de YouTube). La IA de Gemini arma un borrador que después podés editar.
- **Elegir el origen del plato** (país, ciudad o restaurante) buscándolo en un mapa.
- **Ver todas tus recetas en el mapa**, cada una en el lugar de donde viene.
- **Registrar tus intentos de cocinar** una receta, con fotos por paso y una nota.
- **Explorar** recetas públicas con buscador y filtros.
- **Recordatorios** para cocinar una receta (notificaciones).
- **Mi cuenta**: cambiar tu nombre, ver los permisos y cerrar sesión.
- Idioma **español o inglés** y tema **claro u oscuro**.

## ¿Qué se usó?

| Para qué | Qué se usó |
|---|---|
| Lenguaje | Kotlin |
| Pantallas | Jetpack Compose |
| Moverse entre pantallas | Navigation 3 |
| Armar las clases entre sí | Hilt |
| Cuentas de usuario | Firebase Authentication |
| Guardar las recetas en la nube | Firebase Firestore |
| IA para importar recetas | API de Gemini (Google) |
| Mapa | OpenStreetMap (osmdroid), no necesita clave |
| Buscar lugares | Nominatim (OpenStreetMap) con Retrofit y OkHttp |
| Ubicación del teléfono | Google Play Services Location |
| Fotos | Coil (se guardan en el teléfono) |
| Recordatorios | Notificaciones del sistema Android |

Requisitos del teléfono: **Android 8.0 o más nuevo** (minSdk 26).

## ¿Cómo está ordenado el proyecto?

El proyecto se divide en módulos, cada uno con una sola tarea:

```
app/                 → arranca la app y conecta todo
features/auth        → pantalla de acceso (login y registro)
features/recipes     → recetas: lista, detalle, formulario, intentos, explorar
features/map         → pantalla del mapa
features/account     → pantalla Mi cuenta
core/domain          → las reglas de la app (no sabe nada de Android ni de Firebase)
core/data            → guarda y busca los datos (Firestore, Gemini, Nominatim, ubicación)
core/firebase        → habla directo con Firebase Authentication
core/ui              → tema, colores y textos compartidos
```

## Cómo correr la app

### Lo que necesitás

- **Android Studio** (versión reciente).
- Un emulador o un celular Android con depuración USB.
- Internet.

### Pasos

1. Clonar el repositorio y abrirlo con Android Studio:
   ```
   git clone https://github.com/FrancoGomez31/RecetarioAM.git
   ```
2. Poner el archivo `google-services.json` en la carpeta `app/` (ver **Firebase** más abajo).
3. Poner la clave de Gemini en `local.properties` (ver **Clave de Gemini** más abajo).
4. Esperar a que Android Studio termine de sincronizar (**Sync Now** si aparece).
5. Elegir el emulador o el celular y tocar el botón verde **Run ▶**.

> Los archivos `google-services.json` y `local.properties` **no están en el repositorio** a propósito, porque tienen claves privadas. Hay que crearlos en cada computadora.

## Clave de Gemini (para importar recetas con IA)

Esta clave es gratis y sirve solo para la función **"Importar desde link"**. Sin ella, **todo lo demás anda igual**; solo falla esa función.

1. Entrá a <https://aistudio.google.com/apikey> con tu cuenta de Google.
2. Tocá **Create API key** (Crear clave de API) y elegí un proyecto (o creá uno nuevo).
3. Copiá la clave que aparece.
4. En la carpeta principal del proyecto, abrí (o creá) el archivo `local.properties` y agregá esta línea, **sin comillas ni espacios**:
   ```
   gemini.api.key=PEGAR_ACA_TU_CLAVE
   ```
5. En Android Studio: **File → Sync Project with Gradle Files** y volvé a correr la app.

## Integrantes

- _Benjamin Duré_
- _Franco Gómez_
