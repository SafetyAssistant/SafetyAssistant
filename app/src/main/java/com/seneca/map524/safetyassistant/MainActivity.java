package com.seneca.map524.safetyassistant;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import static java.lang.Double.parseDouble;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(43.6532, -79.3832);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.zoomTo( 12.0f ));

        List<String[]> assaults = getCsvData("assault.csv");
        List<Double[]> assaultCoordinates = getParsedCoordinates(assaults, 0);
        for(int i = 0; i < assaultCoordinates.size(); i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(assaultCoordinates.get(i)[1], assaultCoordinates.get(i)[0]))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.assault))
                    );
        }

        List<String[]> autoThefts = getCsvData("auto-theft.csv");
        List<Double[]> autoTheftsCoordinates = getParsedCoordinates(autoThefts, 0);
        for(int i = 0; i < autoTheftsCoordinates.size(); i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(autoTheftsCoordinates.get(i)[1], autoTheftsCoordinates.get(i)[0]))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_theft_1))
            );
        }

        List<String[]> homicides = getCsvData("homicide.csv");
        List<Double[]> homicidesCoordinates = getParsedCoordinates(homicides, 0);
        for(int i = 0; i < homicidesCoordinates.size(); i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(homicidesCoordinates.get(i)[1], homicidesCoordinates.get(i)[0]))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.homicide))
            );
        }

        List<String[]> robberies = getCsvData("robbery.csv");
        List<Double[]> robberiesCoordinates = getParsedCoordinates(robberies, 0);
        for(int i = 0; i < robberiesCoordinates.size(); i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(robberiesCoordinates.get(i)[1], robberiesCoordinates.get(i)[0]))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.robbery))
            );
        }

        List<String[]> sexualAssaults = getCsvData("sexual-assault.csv");
        List<Double[]> sexualAssaultsCoordinates = getParsedCoordinates(sexualAssaults, 0);
        for(int i = 0; i < sexualAssaultsCoordinates.size(); i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(sexualAssaultsCoordinates.get(i)[1], sexualAssaultsCoordinates.get(i)[0]))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.sexual_assault))
            );
        }

        List<String[]> shootings = getCsvData("shooting.csv");
        List<Double[]> shootingsCoordinates = getParsedCoordinates(shootings, 0);
        for(int i = 0; i < shootingsCoordinates.size(); i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(shootingsCoordinates.get(i)[1], shootingsCoordinates.get(i)[0]))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.gun))
            );
        }

        List<String[]> theftOvers = getCsvData("theft-over.csv");
        List<Double[]> theftOversCoordinates = getParsedCoordinates(theftOvers, 0);
        for(int i = 0; i < theftOversCoordinates.size(); i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(theftOversCoordinates.get(i)[1], theftOversCoordinates.get(i)[0]))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.theft_over))
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);

        inflater.inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                return true;
            case R.id.options:
                Intent options = new Intent(this, Options.class);
                startActivity(options);
                return true;
            case R.id.about:
                Intent about = new Intent(this, About.class);
                startActivity(about);
                return true;
            case R.id.help:
                Intent help = new Intent(this, Help.class);
                startActivity(help);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Parsing csv files, each line becomes a String which we keep in the List
    List<String[]> getCsvData(String filename) {
        String next[] = {};
        List<String[]> list = new ArrayList<String[]>();
        try {
            //Specify asset file name in open();
            CSVReader reader = new CSVReader(new InputStreamReader(getAssets().open(filename)));
            reader.readNext();
            for(;;) {
                next = reader.readNext();
                if(next != null) {
                    list.add(next);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    //all coordinates in all datasets come in format "POINT (-79.40189534 43.641915264)"
    //but we need to get just doubles from them
    List<Double[]> getParsedCoordinates(List<String[]> data, int column){
        List<Double[]> list = new ArrayList<Double[]>();
        String[] tokens;
        for (int i = 0; i < data.size(); i++) {
            tokens = data.get(i)[column].split(" ");
            Double[] coordinates = {parseDouble(tokens[1].replace("(", "")), parseDouble(tokens[2].replace(")", ""))};
            list.add(coordinates);
        }
        return list;
    }

}
