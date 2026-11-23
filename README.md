# Pasa la Letra

Juego de palabras por turnos con voz: di una palabra y el siguiente jugador empieza por su última letra.

App Android nativa (Kotlin + Jetpack Compose), de 2 a 8 jugadores en un solo teléfono.

## Cómo conseguir la APK

Cada vez que se sube un cambio a `main`, GitHub compila la app sola (pestaña **Actions**) y publica la APK en **Releases**.

1. Desde el celular, abre `https://github.com/bryanmanuel11-droid/pasa-la-letra/releases/latest`.
2. Descarga el archivo `PasaLaLetra-1.0.X.apk`.
3. Ábrelo. Si Android pregunta, permite **instalar apps desconocidas** para el navegador.
4. Las versiones nuevas se instalan encima de la anterior sin perder los jugadores guardados.

## Qué incluye

- Modos **Eliminación** (con 1, 2 o 3 vidas) y **Liga** (5, 10 o 15 rondas).
- Tiempo por turno: 10, 15, 20 o 30 segundos.
- Micrófono con reconocimiento de voz de Android en español (es-CO). Opción de escribir la palabra si no hay voz.
- La app comprueba la letra, las palabras repetidas y el tiempo; el grupo vota si la palabra existe.
- Las tildes no cuentan, y si una palabra acaba en X o W se pasa la letra anterior.
- Sonido, vibración y jugadores guardados en el teléfono.

## Abrirla en Android Studio (opcional)

Abre la carpeta del proyecto en Android Studio y pulsa ▶ con el celular conectado.

## Firma

La APK se firma con `keystore/pasaletra.jks` (contraseña `pasaletra`). Es una llave solo para instalar la app directamente; no se usa para Google Play.
