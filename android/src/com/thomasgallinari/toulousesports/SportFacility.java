package com.thomasgallinari.toulousesports;

public class SportFacility {

    public String sport;
    public String name;
    public double lat;
    public double lng;
    public String address;

    public SportFacility(String sport, String name, double lat, double lng,
	    String address) {
	this.sport = sport;
	this.name = name;
	this.lat = lat;
	this.lng = lng;
	this.address = address;
    }
}
