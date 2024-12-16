package com.example.turismosostenible.Network;

import com.google.gson.annotations.SerializedName;

//Clase que representa un punto de interés en la API de OpenStreetMap
public class PuntoInteres {

    //Variables
    private String name;
    private double lat;
    private double lon;
    private String type;

    //Variable que representa la dirección del punto de interés
    @SerializedName("address")
    private Address address;

    //Getters & Setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public double getLon() {
        return lon;
    }
    public void setLon(double lon) {
        this.lon = lon;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getCity() {
        return address != null ? address.getCity() : null;
    }
    public void setCity(String city) {
        if (address == null) {
            address = new Address();
        }
        address.setCity(city);
    }

    //Clase interna que representa la dirección de un punto de interés
    public static class Address {

        //Variables
        private String city;
        private String town;
        private String village;

        //Getters & Setters
        public String getCity() {
            return city != null ? city : (town != null ? town : village);
        }
        public void setCity(String city) {
            this.city = city;
        }
        public String getTown() {
            return town;
        }
        public void setTown(String town) {
            this.town = town;
        }
        public String getVillage() {
            return village;
        }
        public void setVillage(String village) {
            this.village = village;
        }
    }
}