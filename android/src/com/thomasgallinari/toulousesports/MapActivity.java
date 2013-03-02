package com.thomasgallinari.toulousesports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends SherlockFragmentActivity implements
	ActionBar.OnNavigationListener, GoogleMap.OnInfoWindowClickListener {

    private static final String COLUMN_DELIMITER = ";";
    private static final int COLUMN_SPORT = 0;
    private static final int COLUMN_NAME = 1;
    private static final int COLUMN_LAT = 2;
    private static final int COLUMN_LNG = 3;
    private static final int COLUMN_ADDRESS = 4;

    private HashMap<String, ArrayList<SportFacility>> sportFacilities;
    private ArrayAdapter<String> sportAdapter;
    private GoogleMap map;

    @Override
    public void onInfoWindowClick(Marker marker) {
	double dLat = marker.getPosition().latitude;
	double dLng = marker.getPosition().longitude;
	Location currentLocation = map.getMyLocation();
	if (currentLocation != null) {
	    double sLat = currentLocation.getLatitude();
	    double sLng = currentLocation.getLongitude();
	    startActivity(new Intent(android.content.Intent.ACTION_VIEW,
		    Uri.parse("http://maps.google.com/maps?saddr=" + sLat + ","
			    + sLng + "&daddr=" + dLat + "," + dLng)));
	} else {
	    startActivity(new Intent(android.content.Intent.ACTION_VIEW,
		    Uri.parse("http://maps.google.com/maps?q=loc:" + dLat + "+"
			    + dLng)));
	}
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
	map.clear();
	if (itemPosition > 0) {
	    String selectedSport = sportAdapter.getItem(itemPosition);
	    ArrayList<SportFacility> selectedSportFacilities = sportFacilities
		    .get(selectedSport);
	    if (selectedSportFacilities != null) {
		for (SportFacility sportFacility : selectedSportFacilities) {
		    map.addMarker(new MarkerOptions()
			    .position(
				    new LatLng(sportFacility.lat,
					    sportFacility.lng))
			    .title(sportFacility.name)
			    .snippet(sportFacility.address)
			    .icon(BitmapDescriptorFactory
				    .defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
		}
	    }
	}
	return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	super.onCreate(savedInstanceState);
	setContentView(R.layout.map);

	sportFacilities = new HashMap<String, ArrayList<SportFacility>>();

	map = ((SupportMapFragment) getSupportFragmentManager()
		.findFragmentById(R.id.map)).getMap();
	map.setMyLocationEnabled(true);
	map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
		43.604482, 1.443962), 12), 2000, null);
	map.setOnInfoWindowClickListener(this);

	ActionBar actionBar = getSupportActionBar();
	actionBar.setDisplayShowTitleEnabled(false);
	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

	setSupportProgressBarIndeterminateVisibility(true);
	try {
	    initData();
	    ArrayList<String> sportList = new ArrayList<String>(
		    sportFacilities.keySet());
	    Collections.sort(sportList);
	    sportList.add(0, getString(R.string.select_sport));
	    sportAdapter = new ActionBarSpinnerAdapter(getSupportActionBar()
		    .getThemedContext(),
		    R.layout.sherlock_spinner_dropdown_item, sportList);
	    actionBar.setListNavigationCallbacks(sportAdapter, this);
	} catch (IOException e) {
	    Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
	} finally {
	    setSupportProgressBarIndeterminateVisibility(false);
	}
    }

    private void initData() throws IOException {
	BufferedReader bufferedReader = new BufferedReader(
		new InputStreamReader(getResources().openRawResource(
			R.raw.tlsp0rt)));
	String line;
	while ((line = bufferedReader.readLine()) != null) {
	    String[] columns = line.split(COLUMN_DELIMITER, -1);
	    String sport = columns[COLUMN_SPORT];
	    String name = columns[COLUMN_NAME];
	    double lat = Double.parseDouble(columns[COLUMN_LAT]);
	    double lng = Double.parseDouble(columns[COLUMN_LNG]);
	    String address = columns[COLUMN_ADDRESS];
	    ArrayList<SportFacility> list = sportFacilities.get(sport);
	    if (list == null) {
		list = new ArrayList<SportFacility>();
		sportFacilities.put(sport, list);
	    }
	    list.add(new SportFacility(sport, name, lat, lng, address));
	}
	bufferedReader.close();
    }
}
