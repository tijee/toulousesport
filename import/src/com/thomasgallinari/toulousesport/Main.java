package com.thomasgallinari.toulousesport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Main {

    private static final String COLUMN_DELIMITER = ";";

    private static final int COLUMN_NAME = 1;
    private static final int COLUMN_ADDRESS = 2;
    private static final int COLUMN_TYPE = 3;
    private static final int COLUMN_ACTIVITIES = 4;

    private static final long MIN_ELAPSED_MS_BETWEEN_GOOGLE_CALLS = 250;

    private static long lastGoogleCall = 0;

    private static final String[][] SPORTS = { { "football", "Football" },
	    { "boulodrome", "Pétanque" }, { "basket", "Basket" },
	    { "volley", "Volley" }, { "hand", "Handball" },
	    { "badmington", "Badmington" }, { "baseball", "Baseball" },
	    { "rugby", "Rugby" }, { "hockey", "Hockey" },
	    { "piste", "Athlétisme" }, { "dojo", "Combat" },
	    { "combat", "Combat" }, { "judo", "Judo" },
	    { "patinoire", "Patinage" }, { "piscine", "Natation" },
	    { "pelote", "Pelote" }, { "roller", "Roller" },
	    { "escrime", "Escrime" }, { "bi-cross", "Bi-cross" },
	    { "skate", "Skate" }, { "gymnastique", "Gymnastique" },
	    { "escalade", "Escalade" }, { "tennis", "Tennis" },
	    { "danse", "Danse" }, { "boxe", "Boxe" }, { "cricket", "Cricket" },
	    { "aviron", "Aviron" }, { "kayak", "Kayak" },
	    { "plongée", "Plongée" }, { "musculation", "Musculation" },
	    { "water-polo", "Water-polo" }, { "ping-pong", "Tennis de table" } };

    public static void main(String[] args) {
	if (args.length == 0) {
	    System.err.println("Veuilez saisir le fichier à importer");
	    System.exit(1);
	}

	ArrayList<SportFacility> sportFacilities = new ArrayList<Main.SportFacility>();
	// read input file
	System.out.println("Import des données");
	try {
	    DataInputStream dataInputStream = new DataInputStream(
		    new FileInputStream(args[0]));
	    BufferedReader bufferedReader = new BufferedReader(
		    new InputStreamReader(dataInputStream, "ISO-8859-1"));
	    boolean firstLine = true;
	    String line;
	    while ((line = bufferedReader.readLine()) != null) {
		if (firstLine) {
		    // ignore first line (headers)
		    firstLine = false;
		    continue;
		}
		String[] columns = line.split(COLUMN_DELIMITER, -1);
		// name
		String name = columns[COLUMN_NAME];
		// address
		String address = columns[COLUMN_ADDRESS];
		double[] latlng = getLatLngFromAddress(address + " Toulouse");
		if (latlng == null) {
		    continue;
		}
		// sport
		String[] sports = getSportsFromString(columns[COLUMN_TYPE]
			+ " " + columns[COLUMN_ACTIVITIES]);
		for (String sport : sports) {
		    sportFacilities.add(new SportFacility(sport, name,
			    latlng[0], latlng[1], address));
		}
	    }
	    dataInputStream.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	// write output file
	System.out.println("Génération du fichier");
	try {
	    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
		    "tlsp0rt.csv"));
	    for (SportFacility sportFacility : sportFacilities) {
		bufferedWriter.write(sportFacility.sport + COLUMN_DELIMITER
			+ sportFacility.name + COLUMN_DELIMITER
			+ sportFacility.lat + COLUMN_DELIMITER
			+ sportFacility.lng + COLUMN_DELIMITER
			+ sportFacility.address + "\n");
	    }
	    bufferedWriter.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}

	System.out.println("Génération terminée : " + sportFacilities.size()
		+ " installations sportives importées dans 'tlsp0rt.csv'");
    }

    private static double[] getLatLngFromAddress(String address)
	    throws IOException {
	long elapsedTimeSinceLastGoogleCall = new Date().getTime()
		- lastGoogleCall;
	if (elapsedTimeSinceLastGoogleCall < MIN_ELAPSED_MS_BETWEEN_GOOGLE_CALLS) {
	    try {
		Thread.sleep(MIN_ELAPSED_MS_BETWEEN_GOOGLE_CALLS
			- elapsedTimeSinceLastGoogleCall);
	    } catch (InterruptedException e) {
	    }
	}
	lastGoogleCall = new Date().getTime();
	URL url = new URL(
		"http://maps.googleapis.com/maps/api/geocode/json?address="
			+ URLEncoder.encode(address, "UTF8") + "&sensor=false");
	JSONObject json = (JSONObject) JSONValue.parse(new BufferedReader(
		new InputStreamReader(url.openStream())));
	String status = (String) json.get("status");
	if (status.equals("OK")) {
	    JSONArray results = (JSONArray) json.get("results");
	    JSONObject result = (JSONObject) results.get(0);
	    JSONObject geometry = (JSONObject) result.get("geometry");
	    String locationType = (String) geometry.get("location_type");
	    if (locationType.equals("APPROXIMATE")) {
		System.err.println("L'adresse " + address
			+ " ne peut pas être localisée avec précision");
		return null;
	    }
	    JSONObject location = (JSONObject) geometry.get("location");
	    Number lat = (Number) location.get("lat");
	    Number lng = (Number) location.get("lng");
	    return new double[] { lat.doubleValue(), lng.doubleValue() };
	}
	System.err.println("Erreur Google : status " + status);
	return null;
    }

    private static String[] getSportsFromString(String string) {
	ArrayList<String> sports = new ArrayList<String>();
	for (String[] sport : SPORTS) {
	    if (string.toLowerCase().contains(sport[0])) {
		sports.add(sport[1]);
	    }
	}
	String[] sportsArray = new String[sports.size()];
	return sports.toArray(sportsArray);
    }

    private static class SportFacility {

	private String sport;
	private String name;
	private double lat;
	private double lng;
	private String address;

	private SportFacility(String sport, String name, double lat,
		double lng, String address) {
	    this.sport = sport;
	    this.name = name;
	    this.lat = lat;
	    this.lng = lng;
	    this.address = address;
	}
    }
}
