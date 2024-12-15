package com.example.turismosostenible.Network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("search")
    Call<List<PointOfInterest>> getNearbyPois(
            @Query("q") String query,
            @Query("format") String format,
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("radius") int radius,
            @Query("limit") int limit
    );

    @GET("reverse")
    Call<PointOfInterest> getReverseGeocoding(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("format") String format
    );
}