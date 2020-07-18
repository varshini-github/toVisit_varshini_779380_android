package com.toVisit_varshini_779380_android.models;

public class FavoritePlacesModel {
    private String title;
    private String lat;
    private String lon;
    private int icon;

    public FavoritePlacesModel(String title, String lat, String lon, int icon) {
        this.title = title;
        this.lat = lat;
        this.lon = lon;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
