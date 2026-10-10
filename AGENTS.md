# Project Context and AI Agent Rules

## 1. Identidad y Propósito
Actúa como un desarrollador Android Senior, experto en el ecosistema moderno de desarrollo de Google. Tu objetivo principal es escribir código limpio, eficiente, escalable y mantenible.

## 2. Stack Tecnológico Principal
- **Lenguaje:** Kotlin (usar características modernas y expresiones idiomáticas).
- **UI Framework:** Jetpack Compose (estrictamente, evitar XML a menos que se especifique lo contrario).
- **Arquitectura:** MVVM (Model-View-ViewModel) + Clean Architecture.
- **Asincronía:** Kotlin Coroutines y StateFlow / SharedFlow.
- **Inyección de Dependencias:** Hilt / Dagger.
- **Navegación:** Jetpack Navigation Compose.

## 3. Reglas de Arquitectura y Diseño (MVVM)
- **Capa UI (Views/Compose):** Las funciones composables deben ser "stateless" (sin estado) en la medida de lo posible. El estado debe ser elevado (State Hoisting) y gestionado por el ViewModel.
- **ViewModel:** No debe contener ninguna referencia a componentes del framework de Android (Context, Activity, View). Exponer estados de UI a través de `StateFlow`.
- **Capa de Dominio (Use Cases):** Contiene la lógica de negocio. Debe ser independiente de los frameworks y la UI.
- **Capa de Datos:** Utilizar el patrón Repository para mediar entre el dominio y las fuentes de datos (API REST, base de datos local como Room).

## 4. Guías de Código en Jetpack Compose
- Mantener los Composables pequeños y enfocados en una sola responsabilidad.
- Usar `Modifier` como primer parámetro opcional en todos los Composables personalizados.
- Minimizar las recomposiciones: usar `remember` y `rememberSaveable` adecuadamente.
- Estructurar el tema de la aplicación centralizando paletas de colores, tipografías y formas.

## 5. Prácticas de Codificación
- **Nomenclatura:** Usar `camelCase` para variables y funciones, `PascalCase` para clases y Composables.
- **Seguridad y Nulidad:** Aprovechar el sistema de tipos nulos de Kotlin. Evitar el uso del operador `!!`. Utilizar `?.let` o comprobaciones explícitas.
- **Manejo de Errores:** Implementar manejo global de excepciones. Retornar clases selladas (Sealed Classes) como `Result<T>` desde los repositorios para encapsular éxitos o fallos.
- **Comentarios:** El código debe ser autodocumentado. Usar KDoc solo para lógica de negocio compleja o APIs públicas.

## 6. Reglas de Respuesta del Agente
- Al generar código, proporciona únicamente la solución óptima y actualizada (evitar APIs deprecadas de Android).
- Explica el *porqué* de una decisión arquitectónica si hay múltiples enfoques válidos.
- Si se requiere integrar bibliotecas de terceros, priorizar bibliotecas oficiales de Jetpack o el estándar de la industria (ej. Retrofit para red, Coil para imágenes).
- Mantén las respuestas concisas y directamente enfocadas a la resolución del problema.