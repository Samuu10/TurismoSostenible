package com.example.turismosostenible.Fragments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.turismosostenible.Network.PointOfInterest;
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
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class FragmentoMapa extends Fragment {

    private static final String TAG = "FragmentoMapa";
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private ApiService apiService;
    private GeoPoint customLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmento_mapa, container, false);

        // Configuramos el contexto de la librería osmdroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        // Inicializamos el servicio de Retrofit antes de configurarlo
        apiService = RetrofitClient.getClient().create(ApiService.class);

        mapView = view.findViewById(R.id.map);
        if (mapView != null) {
            configureMap(); // Configuramos el mapa después de inicializar apiService
        } else {
            Log.e(TAG, "MapView is null");
        }

        return view;
    }

    private void configureMap() {
        // Configuramos el mapa
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Usamos las coordenadas de la Universidad Alfonso X el Sabio
        double customLat = 40.448031;  // Latitud: Villanueva de la Cañada, Universidad Alfonso X el Sabio
        double customLon = -3.796295;  // Longitud

        // Asignar las coordenadas a customLocation
        customLocation = new GeoPoint(customLat, customLon);

        if (customLocation == null) {
            Log.e(TAG, "Custom location is null!");
            return; // Evita continuar si no está bien inicializado
        }

        IMapController mapController = mapView.getController();
        mapController.setZoom(5.0);
        mapController.setCenter(customLocation);

        // Crear un marcador para la ubicación personalizada
        Marker customMarker = new Marker(mapView);
        customMarker.setPosition(customLocation);
        customMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        customMarker.setTitle("Tu ubicación actual");
        customMarker.setIcon(getResources().getDrawable(R.drawable.custom_location_arrow, null)); // Ícono rojo

        mapView.getOverlays().add(customMarker); // Añadir el marcador al mapa
        mapView.invalidate(); // Refrescar el mapa

        // Activar búsqueda de POIs cercanos usando la ubicación actual
        fetchNearbyPois(customLat, customLon, "restaurant");
        fetchNearbyPois(customLat, customLon, "bar");
        fetchNearbyPois(customLat, customLon, "hotel");
        fetchNearbyPois(customLat, customLon, "museum");
    }

    // Función para redimensionar un ícono (Bitmap)
    private Bitmap resizeIcon(Drawable drawable, int width, int height) {
        // Convierte Drawable a Bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    // Función para establecer los íconos de los POIs con el tamaño ajustado
    private void fetchNearbyPois(double lat, double lon, String query) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is not initialized!");
            return;
        }

        Call<List<PointOfInterest>> call = apiService.getNearbyPois(query, "json", lat, lon, 1000, 20);
        call.enqueue(new Callback<List<PointOfInterest>>() {
            @Override
            public void onResponse(Call<List<PointOfInterest>> call, Response<List<PointOfInterest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PointOfInterest> pois = response.body();
                    Log.d(TAG, "POIs received: " + pois.size());
                    for (PointOfInterest poi : pois) {
                        Marker poiMarker = new Marker(mapView);
                        GeoPoint poiLocation = new GeoPoint(poi.getLat(), poi.getLon());
                        poiMarker.setPosition(poiLocation);
                        poiMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        poiMarker.setTitle(poi.getName());

                        // Variable para almacenar el icono según el tipo de POI
                        Drawable poiIcon = null;

                        // Establecemos el ícono según el tipo de POI
                        switch (query) {
                            case "restaurant":
                                poiIcon = getResources().getDrawable(R.drawable.ic_restaurant, null); // Ícono restaurante
                                break;
                            case "bar":
                                poiIcon = getResources().getDrawable(R.drawable.ic_bar, null); // Ícono bar
                                break;
                            case "hotel":
                                poiIcon = getResources().getDrawable(R.drawable.ic_hotel, null); // Ícono hotel
                                break;
                            case "museum":
                                poiIcon = getResources().getDrawable(R.drawable.ic_museum, null); // Ícono museo
                                break;
                        }

                        if (poiIcon != null) {
                            Bitmap resizedIcon = resizeIcon(poiIcon, 50, 50); // Redimensionamos el ícono
                            poiMarker.setIcon(new BitmapDrawable(getResources(), resizedIcon)); // Establecer el ícono redimensionado
                        } else {
                            // Si no se especifica un icono, se usará el marcador predeterminado de OSM (sin icono personalizado)
                            poiMarker.setIcon(null);  // No es necesario hacer nada si usas el icono predeterminado
                        }

                        poiMarker.setSubDescription(poi.getName());
                        mapView.getOverlays().add(poiMarker);
                    }
                    mapView.invalidate();
                } else {
                    Log.e(TAG, "API Response Error: " + response.message());
                    Log.e(TAG, "Response Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<PointOfInterest>> call, Throwable t) {
                Log.e(TAG, "API call error: ", t);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}
