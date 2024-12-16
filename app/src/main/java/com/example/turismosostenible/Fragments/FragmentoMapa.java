package com.example.turismosostenible.Fragments;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.turismosostenible.Network.PuntoInteres;
import com.example.turismosostenible.Network.ApiService;
import com.example.turismosostenible.Network.RetrofitClient;
import com.example.turismosostenible.R;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import retrofit2.Call;
import retrofit2.Response;
import java.util.List;

//Clase que representa un fragmento que muestra un mapa con la ubicación actual del usuario y puntos de interés cercanos
public class FragmentoMapa extends Fragment {

    //Variables
    private static final String TAG = "FragmentoMapa";
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private ApiService apiService;
    private GeoPoint customLocation;

    //Metodo para crear la vista del fragmento
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmento_mapa, container, false);

        //Configuramos el contexto de la librería osmdroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        //Inicializamos el servicio de Retrofit antes de configurarlo
        apiService = RetrofitClient.getClient().create(ApiService.class);

        //Obtenemos la referencia al mapa y lo configuramos
        mapView = view.findViewById(R.id.map);
        if (mapView != null) {
            configureMap();
        } else {
            Log.e(TAG, "MapView is null");
        }

        return view;
    }

    //Metodo para configurar el mapa
    private void configureMap() {
        //Configuración principal del mapa
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        //Como la ubicación real de los dispositivos virtuales es en San Francisco, simulamos la ubicación del usuario en la Universidad Alfonso X el Sabio
        double customLat = 40.448031;
        double customLon = -3.796295;

        //Asignamos las coordenadas a customLocation y centramos el mapa en esa ubicación
        customLocation = new GeoPoint(customLat, customLon);

        if (customLocation == null) {
            Log.e(TAG, "Custom location is null!");
            return;
        }

        //Creamos un controlador de mapa y lo configuramos
        IMapController mapController = mapView.getController();
        mapController.setZoom(6.0);
        mapController.setCenter(customLocation);

        //Creamos un marcador personalizado para la ubicación del usuario
        Marker customMarker = new Marker(mapView);
        customMarker.setPosition(customLocation);
        customMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        customMarker.setTitle("Tu ubicación actual");
        customMarker.setIcon(getResources().getDrawable(R.drawable.custom_location_arrow, null));

        mapView.getOverlays().add(customMarker);
        mapView.invalidate();

        //Activamos las búsquedas de POIs cercanos usando la ubicación actual del usuario como referencia
        new BuscarPuntoInteresCercanoTask(customLat, customLon).execute("restaurant");
        new BuscarPuntoInteresCercanoTask(customLat, customLon).execute("bar");
        new BuscarPuntoInteresCercanoTask(customLat, customLon).execute("hotel");
        new BuscarPuntoInteresCercanoTask(customLat, customLon).execute("museum");
    }

    //Metodo para redimensionar un icono a un tamaño específico con Bitmap
    private Bitmap resizeIcon(Drawable drawable, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    //Clase interna para buscar puntos de interés cercanos en segundo plano
    private class BuscarPuntoInteresCercanoTask extends AsyncTask<String, Void, List<PuntoInteres>> {

        //Variables
        private double lat;
        private double lon;
        private String query;

        //Constructor
        public BuscarPuntoInteresCercanoTask(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        //Metodo que se encarga de realizar la búsqueda de puntos de interés en segundo plano y que recibe como parámetro el término de búsqueda
        @Override
        protected List<PuntoInteres> doInBackground(String... queries) {
            query = queries[0];
            //Se realiza la llamada a la API para obtener los puntos de interés cercanos a la ubicación actual
            Call<List<PuntoInteres>> call = apiService.getNearbyPois(query, "json", lat, lon, 1000, 20);
            //Se gestiona la respuesta de la Api y se devuelve la lista de puntos de interés encontrados
            try {
                Response<List<PuntoInteres>> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body();
                } else {
                    Log.e(TAG, "API Response Error: " + response.message());
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "API call error: ", e);
                return null;
            }
        }

        //Metodo que se encarga de añadir los marcadores de los resultados de la búsqueda al mapa
        @Override
        protected void onPostExecute(List<PuntoInteres> pois) {
            //Si se han encontrado puntos de interés, se recorren y se añaden al mapa
            if (pois != null) {
                for (PuntoInteres poi : pois) {
                    if (poi == null) {
                        Log.e(TAG, "POI is null");
                        continue;
                    }

                    //Creamos un marcador para el punto de interés y lo añadimos al mapa
                    Marker poiMarker = new Marker(mapView);
                    GeoPoint poiLocation = new GeoPoint(poi.getLat(), poi.getLon());
                    poiMarker.setPosition(poiLocation);
                    poiMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                    //Si la API no devuelve un nombre para el punto de interés, se asigna un nombre genérico
                    String poiName = poi.getName();
                    if (poiName == null || poiName.isEmpty()) {
                        poiName = poi.getType() != null ? poi.getType() : "Establecimiento sin nombre";
                    }
                    poiMarker.setTitle(poiName);

                    //Establecemos un icono personalizado para el marcador en función del tipo de punto de interés
                    Drawable poiIcon = null;
                    switch (query) {
                        case "restaurant":
                            poiIcon = getResources().getDrawable(R.drawable.ic_restaurant, null);
                            break;
                        case "bar":
                            poiIcon = getResources().getDrawable(R.drawable.ic_bar, null);
                            break;
                        case "hotel":
                            poiIcon = getResources().getDrawable(R.drawable.ic_hotel, null);
                            break;
                        case "museum":
                            poiIcon = getResources().getDrawable(R.drawable.ic_museum, null);
                            break;
                    }

                    //Si se ha obtenido un icono, se redimensiona y se asigna al marcador
                    if (poiIcon != null) {
                        Bitmap resizedIcon = resizeIcon(poiIcon, 50, 50);
                        poiMarker.setIcon(new BitmapDrawable(getResources(), resizedIcon));
                    } else {
                        poiMarker.setIcon(null);
                    }

                    //Establecemos un subtitulo para el marcador con el nombre del punto de interés
                    poiMarker.setSubDescription(poiName);
                    poiMarker.setOnMarkerClickListener((marker, mapView) -> {
                        new DetallesPuntoInteresTask().execute(poi.getLat(), poi.getLon());
                        return true;
                    });
                    mapView.getOverlays().add(poiMarker);
                }
                mapView.invalidate();
            }
        }
    }

    //Clase interna para obtener detalles de los puntos de interés en segundo plano
    private class DetallesPuntoInteresTask extends AsyncTask<Double, Void, PuntoInteres> {

        //Metodo que se encarga de obtener los detalles de un punto de interés en segundo plano y que recibe como parámetro las coordenadas del punto de interés
        @Override
        protected PuntoInteres doInBackground(Double... coords) {
            double lat = coords[0];
            double lon = coords[1];
            //Se realiza la llamada a la API para obtener la dirección del punto de interés a partir de sus coordenadas
            Call<PuntoInteres> call = apiService.getReverseGeocoding(lat, lon, "json");
            //Se gestiona la respuesta de la Api y se devuelve el punto de interés con los detalles obtenidos
            try {
                Response<PuntoInteres> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body();
                } else {
                    Log.e(TAG, "Reverse Geocoding API Response Error: " + response.message());
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Reverse Geocoding API call error: ", e);
                return null;
            }
        }

        //Metodo que se encarga de mostrar un diálogo con los detalles del punto de interés
        @Override
        protected void onPostExecute(PuntoInteres poi) {
            if (poi != null) {
                mostrarDialogoDetalles(poi);
            }
        }
    }

    //Metodo para mostrar un diálogo con los detalles de un punto de interés al clicar sobre su marcador
    private void mostrarDialogoDetalles(PuntoInteres poi) {
        if (poi == null) {
            Log.e(TAG, "POI is null");
            return;
        }

        //Creamos un diálogo personalizado con los detalles del punto de interés
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_poi_details, null);
        TextView tvPoiName = dialogView.findViewById(R.id.tv_poi_name);
        TextView tvPoiCity = dialogView.findViewById(R.id.tv_poi_city);

        //Mostramos el nombre del punto de interés en el diálogo
        if (tvPoiName != null) {
            String poiName = poi.getName();
            if (poiName == null || poiName.isEmpty()) {
                poiName = poi.getType() != null ? poi.getType() : "Establecimiento sin nombre";
            }
            tvPoiName.setText(poiName);
        } else {
            Log.e(TAG, "TextView for POI name is null");
        }

        //Mostramos la ciudad del punto de interés en el diálogo
        if (tvPoiCity != null) {
            tvPoiCity.setText(poi.getCity() != null ? poi.getCity() : "Ciudad desconocida");
        } else {
            Log.e(TAG, "TextView for POI city is null");
        }

        //Mostramos el diálogo
        builder.setView(dialogView)
                .setTitle("Detalles del Punto de Interés")
                .setPositiveButton("OK", null)
                .show();
    }

    //Metodo para reanudar el mapa
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    //Metodo para pausar el mapa
    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    //Metodo para destruir el mapa
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach();
        }
    }

    //Metodo para añadir los resultados de la búsqueda al mapa
    public void añadirResultadosBusquedaMapa(List<PuntoInteres> pois) {
        if (mapView == null) {
            Log.e(TAG, "MapView is null");
            return;
        }

        //Recorremos los puntos de interés encontrados y añadimos un marcador para cada uno
        for (PuntoInteres poi : pois) {
            if (poi == null) {
                Log.e(TAG, "POI is null");
                continue;
            }

            //Creamos un marcador para el punto de interés y lo añadimos al mapa
            Marker poiMarker = new Marker(mapView);
            GeoPoint poiLocation = new GeoPoint(poi.getLat(), poi.getLon());
            poiMarker.setPosition(poiLocation);
            poiMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            //Si la API no devuelve un nombre para el punto de interés, se asigna un nombre genérico
            String poiName = poi.getName();
            if (poiName == null || poiName.isEmpty()) {
                poiName = poi.getType() != null ? poi.getType() : "Establecimiento sin nombre";
            }
            poiMarker.setTitle(poiName);

            poiMarker.setOnMarkerClickListener((marker, mapView) -> {
                mostrarDialogoDetalles(poi);
                return true;
            });

            mapView.getOverlays().add(poiMarker);
        }
        mapView.invalidate();
    }
}