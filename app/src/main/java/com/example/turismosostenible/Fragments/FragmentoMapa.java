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
        mapController.setZoom(6.0);
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
        new FetchNearbyPoisTask(customLat, customLon).execute("restaurant");
        new FetchNearbyPoisTask(customLat, customLon).execute("bar");
        new FetchNearbyPoisTask(customLat, customLon).execute("hotel");
        new FetchNearbyPoisTask(customLat, customLon).execute("museum");
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

    // Clase interna para buscar POIs cercanos en segundo plano
    private class FetchNearbyPoisTask extends AsyncTask<String, Void, List<PointOfInterest>> {
        private double lat;
        private double lon;
        private String query;

        public FetchNearbyPoisTask(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        protected List<PointOfInterest> doInBackground(String... queries) {
            query = queries[0];
            Call<List<PointOfInterest>> call = apiService.getNearbyPois(query, "json", lat, lon, 1000, 20);
            try {
                Response<List<PointOfInterest>> response = call.execute();
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

        @Override
        protected void onPostExecute(List<PointOfInterest> pois) {
            if (pois != null) {
                for (PointOfInterest poi : pois) {
                    if (poi == null) {
                        Log.e(TAG, "POI is null");
                        continue;
                    }

                    Marker poiMarker = new Marker(mapView);
                    GeoPoint poiLocation = new GeoPoint(poi.getLat(), poi.getLon());
                    poiMarker.setPosition(poiLocation);
                    poiMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                    String poiName = poi.getName();
                    if (poiName == null || poiName.isEmpty()) {
                        poiName = poi.getType() != null ? poi.getType() : "Establecimiento sin nombre";
                    }
                    poiMarker.setTitle(poiName);

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

                    if (poiIcon != null) {
                        Bitmap resizedIcon = resizeIcon(poiIcon, 50, 50);
                        poiMarker.setIcon(new BitmapDrawable(getResources(), resizedIcon));
                    } else {
                        poiMarker.setIcon(null);
                    }

                    poiMarker.setSubDescription(poiName);
                    poiMarker.setOnMarkerClickListener((marker, mapView) -> {
                        new FetchPoiDetailsTask().execute(poi.getLat(), poi.getLon());
                        return true;
                    });
                    mapView.getOverlays().add(poiMarker);
                }
                mapView.invalidate();
            }
        }
    }

    // Clase interna para obtener detalles del POI en segundo plano
    private class FetchPoiDetailsTask extends AsyncTask<Double, Void, PointOfInterest> {
        @Override
        protected PointOfInterest doInBackground(Double... coords) {
            double lat = coords[0];
            double lon = coords[1];
            Call<PointOfInterest> call = apiService.getReverseGeocoding(lat, lon, "json");
            try {
                Response<PointOfInterest> response = call.execute();
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

        @Override
        protected void onPostExecute(PointOfInterest poi) {
            if (poi != null) {
                showPoiDetailsDialog(poi);
            }
        }
    }

    private void showPoiDetailsDialog(PointOfInterest poi) {
        if (poi == null) {
            Log.e(TAG, "POI is null");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_poi_details, null);

        TextView tvPoiName = dialogView.findViewById(R.id.tv_poi_name);
        TextView tvPoiCity = dialogView.findViewById(R.id.tv_poi_city);

        if (tvPoiName != null) {
            String poiName = poi.getName();
            if (poiName == null || poiName.isEmpty()) {
                poiName = poi.getType() != null ? poi.getType() : "Establecimiento sin nombre";
            }
            tvPoiName.setText(poiName);
        } else {
            Log.e(TAG, "TextView for POI name is null");
        }

        if (tvPoiCity != null) {
            tvPoiCity.setText(poi.getCity() != null ? poi.getCity() : "Ciudad desconocida");
        } else {
            Log.e(TAG, "TextView for POI city is null");
        }

        builder.setView(dialogView)
                .setTitle("Detalles del Punto de Interés")
                .setPositiveButton("OK", null)
                .show();
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