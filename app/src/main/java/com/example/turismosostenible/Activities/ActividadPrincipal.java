package com.example.turismosostenible.Activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.turismosostenible.Fragments.FragmentoMapa;
import com.example.turismosostenible.Network.PointOfInterest;
import com.example.turismosostenible.Network.ApiService;
import com.example.turismosostenible.Network.RetrofitClient;
import com.example.turismosostenible.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class ActividadPrincipal extends AppCompatActivity {

    private EditText searchEditText;
    private Button searchButton;
    private FragmentoMapa fragmentoMapa;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchEditText.getText().toString();
                if (!query.isEmpty()) {
                    new SearchTask().execute(query);
                } else {
                    Toast.makeText(ActividadPrincipal.this, "Por favor, ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fragmentoMapa = new FragmentoMapa();
        loadFragment(fragmentoMapa);
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private class SearchTask extends AsyncTask<String, Void, List<PointOfInterest>> {
        @Override
        protected List<PointOfInterest> doInBackground(String... params) {
            String query = params[0];
            Call<List<PointOfInterest>> call = apiService.getNearbyPois(query, "json", 40.448031, -3.796295, 1000, 20);
            try {
                Response<List<PointOfInterest>> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body();
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<PointOfInterest> result) {
            if (result != null && !result.isEmpty()) {
                fragmentoMapa.addSearchResultsToMap(result);
            } else {
                Toast.makeText(ActividadPrincipal.this, "No se encontraron resultados", Toast.LENGTH_SHORT).show();
            }
        }
    }
}