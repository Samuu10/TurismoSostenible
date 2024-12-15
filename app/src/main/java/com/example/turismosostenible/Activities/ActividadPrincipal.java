package com.example.turismosostenible.Activities;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.turismosostenible.Fragments.FragmentoMapa;
import com.example.turismosostenible.R;

//Actividad principal de la aplicacion en la que se muestra el mapa y se pueden realizar busquedas
public class ActividadPrincipal extends AppCompatActivity {

    //Variables
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);

        //Establecemos el SearchView
        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint("Buscar...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                new SearchTask().execute(newText);
                return true;
            }
        });

        loadFragment(new FragmentoMapa());
    }

    //Metodo para cargar un fragmento en el contenedor de fragmentos
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    //Metodo para gestionar la respuesta de los permisos de ubicación
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            new HandlePermissionsTask().execute(grantResults);
        }
    }

    //Clase interna para manejar los permisos en segundo plano
    private class HandlePermissionsTask extends AsyncTask<int[], Void, Boolean> {
        @Override
        protected Boolean doInBackground(int[]... grantResults) {
            return grantResults.length > 0 && grantResults[0][0] == PackageManager.PERMISSION_GRANTED;
        }

        @Override
        protected void onPostExecute(Boolean permissionGranted) {
            if (permissionGranted) {
                //Si se conceden los permisos, se carga el fragmento del mapa
                loadFragment(new FragmentoMapa());
            } else {
                //Si no se conceden los permisos, se muestra un mensaje de error
                Toast.makeText(ActividadPrincipal.this, "Location permission is required to show the map", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Clase interna para buscar establecimientos en segundo plano
    private class SearchTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String newText = params[0];
            // Aquí puedes agregar la lógica de búsqueda en segundo plano
            // Por ejemplo, filtrar una lista de elementos
            return newText != null && !newText.isEmpty();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                // Actualiza la UI con los resultados de la búsqueda
                Toast.makeText(ActividadPrincipal.this, "Search text changed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}