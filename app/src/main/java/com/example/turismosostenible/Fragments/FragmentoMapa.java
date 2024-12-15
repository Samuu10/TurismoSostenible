package com.example.turismosostenible.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.turismosostenible.Network.PointOfInterest;
import com.example.turismosostenible.Network.ApiService;
import com.example.turismosostenible.Network.RetrofitClient;
import com.example.turismosostenible.Network.PoiResponse;
import com.example.turismosostenible.R;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentoMapa extends Fragment {

    private static final String TAG = "FragmentoMapa";
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmento_mapa, container, false);

        // Configuramos el contexto de la librería osmdroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        mapView = view.findViewById(R.id.map);
        if (mapView != null) {
            configureMap();
        } else {
            Log.e(TAG, "MapView is null");
        }

        apiService = RetrofitClient.getClient().create(ApiService.class);

        return view;
    }

    private void configureMap() {
        // Configuramos el mapa
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Añadimos la capa de ubicación
        locationOverlay = new MyLocationNewOverlay(mapView);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();

        // Convert Drawable to Bitmap and set as person icon
        Drawable drawable = getResources().getDrawable(R.drawable.custom_location_arrow, null);
        if (drawable != null) {
            locationOverlay.setPersonIcon(drawableToBitmap(drawable));
            Log.d(TAG, "Custom location arrow set successfully.");
        } else {
            Log.e(TAG, "Custom location arrow drawable is null");
        }

        mapView.getOverlays().add(locationOverlay);

        // Centramos el mapa en la ubicación actual del usuario
        IMapController mapController = mapView.getController();
        mapController.setZoom(5.0);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationOverlay.runOnFirstFix(() -> {
                GeoPoint location = locationOverlay.getMyLocation();
                if (location != null) {
                    Log.d(TAG, "Location obtained: " + location.getLatitude() + ", " + location.getLongitude());
                    fetchNearbyPois(location.getLatitude(), location.getLongitude(), "restaurant");
                } else {
                    Log.e(TAG, "Location is null");
                }
            });
        } else {
            Log.e(TAG, "Location permission not granted");
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
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

    private void fetchNearbyPois(double lat, double lon, String query) {
        Call<List<PointOfInterest>> call = apiService.getNearbyPois(query, "json", lat, lon, 10000, 10);
        call.enqueue(new Callback<List<PointOfInterest>>() {
            @Override
            public void onResponse(Call<List<PointOfInterest>> call, Response<List<PointOfInterest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PointOfInterest> pois = response.body();
                    Log.d(TAG, "POIs received: " + pois.size());
                    for (PointOfInterest poi : pois) {
                        Log.d(TAG, "POI: " + poi.getName() + ", " + poi.getLat() + ", " + poi.getLon());
                        Marker poiMarker = new Marker(mapView);
                        poiMarker.setPosition(new GeoPoint(poi.getLat(), poi.getLon()));
                        poiMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        poiMarker.setTitle(poi.getName());
                        mapView.getOverlays().add(poiMarker);
                    }
                    mapView.invalidate();
                } else {
                    Log.e(TAG, "API Response Error: " + response.message());
                    Log.e(TAG, "Response Code: " + response.code());
                    Log.e(TAG, "Response Body: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<List<PointOfInterest>> call, Throwable t) {
                Log.e(TAG, "API call error: ", t);
            }
        });
    }

}