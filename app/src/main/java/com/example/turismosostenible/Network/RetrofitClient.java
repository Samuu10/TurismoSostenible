package com.example.turismosostenible.Network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//Clase que crea una instancia de Retrofit para realizar peticiones a la API de OpenStreetMap
public class RetrofitClient {

    //Variable
    private static Retrofit retrofit = null;

    //Metodo para obtener una instancia de Retrofit con la URL base de la API de OpenStreetMap
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://nominatim.openstreetmap.org/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}