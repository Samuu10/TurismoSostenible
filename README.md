# Aplicación para Turismo Sostenible

## Objetivo de la App

La aplicación "Turismo Sostenible" tiene como objetivo ayudar a los usuarios a encontrar puntos de interés turísticos cercanos a su ubicación actual. 
Utiliza la API de OpenStreetMap (OSM) para obtener información sobre lugares como restaurantes, bares, hoteles y museos, y los muestra en un mapa interactivo. 
La aplicación está diseñada para ser fácil de usar y proporcionar información relevante y precisa sobre los destinos turísticos.

## Interacción con la API de OpenStreetMap y Nominatim

En el enunciado se propone usar la API de Google Maps y la API de Places para obtener información sobre los puntos de interés.
Sin embargo, debido al límite de puntos que imponen estas APIs, la aplicación interactúa con la API de OpenStreetMap (OSM) y Nominatim para obtener los datos.  
Utiliza Retrofit para realizar las peticiones HTTP a la API y Gson para convertir las respuestas JSON en objetos Java.

### Funcionalidades principales:

1. **Búsqueda de puntos de interés cercanos**: La aplicación permite a los usuarios buscar puntos de interés cercanos a su ubicación actual utilizando la API de OSM.
2. **Geocodificación inversa**: La aplicación puede obtener la dirección de un punto de interés a partir de sus coordenadas utilizando la API de Nominatim.
3. **Visualización en mapa**: Los resultados de las búsquedas se muestran en un mapa interactivo utilizando la librería osmdroid.

## Estructura del Proyecto

### Clases JAVA

1. **ActividadPrincipal**:
   - Controla la actividad principal de la aplicación, donde se carga el fragmento del mapa y se gestiona la búsqueda de puntos de interés.
   - Inicializa `ApiService` para realizar peticiones a la API de OpenStreetMap.
   - Maneja la lógica para realizar búsquedas de puntos de interés cercanos y mostrar los resultados en el mapa.

2. **FragmentoMapa**:
   - Muestra un mapa con la ubicación actual del usuario y puntos de interés cercanos.
   - Configura el mapa utilizando la librería osmdroid.
   - Realiza búsquedas de puntos de interés cercanos y añade marcadores al mapa.
   - Muestra detalles de los puntos de interés en un diálogo al hacer clic en los marcadores.

3. **NetworkExecutor**:
   - Maneja la ejecución de tareas en segundo plano utilizando `ExecutorService`.
   - Proporciona un método para enviar tareas a un pool de hilos fijo.

4. **ApiService**:
   - Define los métodos para realizar peticiones a la API de OpenStreetMap.
   - Incluye métodos para obtener puntos de interés cercanos y realizar geocodificación inversa.

5. **RetrofitClient**:
   - Crea una instancia de Retrofit para realizar peticiones a la API de OpenStreetMap.
   - Configura Retrofit con la URL base de la API y un convertidor Gson.

6. **PuntoInteres**:
   - Representa un punto de interés en la API de OpenStreetMap.
   - Incluye atributos como nombre, latitud, longitud, tipo y dirección.
   - Contiene una clase interna `Address` para representar la dirección del punto de interés.

### Archivos XML

1. **actividad_principal.xml**:
   - Archivo de diseño para la actividad principal de la aplicación.
   - Contiene elementos como `TextView` para el título, `EditText` para la búsqueda, `Button` para iniciar la búsqueda y `FrameLayout` para el fragmento del mapa.

2. **fragmento_mapa.xml**:
   - Archivo de diseño para el fragmento que muestra el mapa.
   - Contiene un `MapView` para mostrar el mapa y los marcadores de puntos de interés.

3. **dialog_poi_details.xml**:
   - Archivo de diseño para el diálogo que muestra los detalles de un punto de interés.
   - Contiene elementos como `TextView` para mostrar el nombre y la ciudad del punto de interés.

4. **custom_location_arrow.xml**:
   - Archivo de diseño vectorial para el icono personalizado de la ubicación del usuario.
   - Define un icono en forma de flecha roja para representar la ubicación actual del usuario en el mapa.

## Link al repositorio:
https://github.com/Samuu10/TurismoSostenible.git
