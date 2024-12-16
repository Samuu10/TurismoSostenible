package com.example.turismosostenible.Activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.turismosostenible.Fragments.FragmentoMapa;
import com.example.turismosostenible.Network.PuntoInteres;
import com.example.turismosostenible.Network.ApiService;
import com.example.turismosostenible.Network.RetrofitClient;
import com.example.turismosostenible.R;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

//Clase principal de la aplicación en la que se carga el  fragmento del mapa y que gestiona las busqueda de puntos de interés
public class ActividadPrincipal extends AppCompatActivity {

    //Variables
    private EditText searchEditText;
    private Button searchButton;
    private FragmentoMapa fragmentoMapa;
    private ApiService apiService;

    //Metodo para crear la actividad
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);

        //Se crea una instancia de la interfaz ApiService y se inicializa con el cliente de Retrofit
        apiService = RetrofitClient.getClient().create(ApiService.class);

        //Inicialización de las variables
        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);

        //Se añade un listener al botón de búsqueda para que al pulsarlo se realice una búsqueda de puntos de interés cercanos
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString();
            if (!query.isEmpty()) {
                new SearchTask().execute(query);
            } else {
                Toast.makeText(ActividadPrincipal.this, "Por favor, ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
            }
        });

        //Se carga el fragmento del mapa
        fragmentoMapa = new FragmentoMapa();
        loadFragment(fragmentoMapa);
    }

    //Metodo para cargar un fragmento en el contenedor de fragmentos de la actividad
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    //Clase interna que extiende de AsyncTask y que se encarga de realizar la búsqueda de puntos de interés en segundo plano
    private class SearchTask extends AsyncTask<String, Void, List<PuntoInteres>> {

        //Metodo que se encarga de realizar la búsqueda de puntos de interés en segundo plano y que recibe como parámetro el término de búsqueda
        @Override
        protected List<PuntoInteres> doInBackground(String... params) {
            String query = params[0];
            //Se realiza la llamada a la API para obtener los puntos de interés cercanos a la ubicación actual
            Call<List<PuntoInteres>> call = apiService.getNearbyPois(query, "json", 40.448031, -3.796295, 1000, 20);
            //Se gestiona la respuesta de la Api y se devuelve la lista de puntos de interés encontrados
            try {
                Response<List<PuntoInteres>> response = call.execute();
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

        //Metodo que se encarga de añadir los marcadores de los resultados de la búsqueda al mapa
        @Override
        protected void onPostExecute(List<PuntoInteres> result) {
            if (result != null && !result.isEmpty()) {
                fragmentoMapa.añadirResultadosBusquedaMapa(result);
            } else {
                Toast.makeText(ActividadPrincipal.this, "No se encontraron resultados", Toast.LENGTH_SHORT).show();
            }
        }
    }
}