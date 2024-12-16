package com.example.turismosostenible.Network;

import java.util.List;

public class PoiResponse {
    private List<PuntoInteres> pois;

    public List<PuntoInteres> getPois() {
        return pois;
    }

    public void setPois(List<PuntoInteres> pois) {
        this.pois = pois;
    }
}