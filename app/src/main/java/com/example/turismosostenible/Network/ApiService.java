package com.example.turismosostenible.Network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

//Interfaz que define los métodos para realizar peticiones a la API de OpenStreetMap
public interface ApiService {

    //Metodo para obtener los puntos de interés cercanos a una ubicación
    @GET("search")
    Call<List<PuntoInteres>> getNearbyPois(
            @Query("q") String query,
            @Query("format") String format,
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("radius") int radius,
            @Query("limit") int limit
    );

    //Metodo para obtener la dirección de una ubicación a partir de sus coordenadas
    @GET("reverse")
    Call<PuntoInteres> getReverseGeocoding(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("format") String format
    );
}