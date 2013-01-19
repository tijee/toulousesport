package com.thomasgallinari.toulousesports;

public class SportFacility {

    public String sport;
    public String name;
    public double lat;
    public double lng;

    public SportFacility(String sport, String name, double lat, double lng) {
	this.sport = sport;
	this.name = name;
	this.lat = lat;
	this.lng = lng;
    }
}
