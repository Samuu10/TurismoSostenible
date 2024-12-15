package com.example.turismosostenible.Network;

import java.util.List;

public class PoiResponse {
    private List<PointOfInterest> pois;

    public List<PointOfInterest> getPois() {
        return pois;
    }

    public void setPois(List<PointOfInterest> pois) {
        this.pois = pois;
    }
}