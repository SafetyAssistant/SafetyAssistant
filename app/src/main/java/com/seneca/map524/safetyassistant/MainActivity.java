package com.seneca.map524.safetyassistant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import static java.lang.Double.parseDouble;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    LatLng startLocation;

    private ClusterManager<MyItem> clusterManager;
    TileOverlay mOverlay;

    List<String[]> assaults;
    List<String[]> autoThefts;
    List<String[]> homicides;
    List<String[]> robberies;
    List<String[]> sexualAssaults;
    List<String[]> shootings;
    List<String[]> theftOvers;
    List<Double[]> assaultCoordinates;
    List<Double[]> autoTheftsCoordinates;
    List<Double[]> homicidesCoordinates;
    List<Double[]> robberiesCoordinates;
    List<Double[]> sexualAssaultsCoordinates;
    List<Double[]> shootingsCoordinates;
    List<Double[]> theftOversCoordinates;

    private SharedPreferences preferences;
    private static final String PREF_FILE_NAME = "LegendCheckBoxPref";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = this.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        boolean assault_check_state = preferences.getBoolean("assault", false);
        boolean auto_theft_check_state = preferences.getBoolean("autoTheft", false);
        boolean homicide_check_state = preferences.getBoolean("homicide", false);
        boolean robbery_check_state = preferences.getBoolean("robbery", false);
        boolean sex_assault_check_state = preferences.getBoolean("sexAssault", false);
        boolean shooting_check_state = preferences.getBoolean("shooting", false);
        boolean theft_over_check_state = preferences.getBoolean("theftOver", false);
        boolean heat_map_check_state = preferences.getBoolean("heatMap", false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        Menu menu = navigationView.getMenu();
        //creating checkBoxes for the left drawer
        CheckBox cb_assault =(CheckBox) MenuItemCompat.getActionView(menu.findItem(R.id.cb_assault)).findViewById(R.id.action_view_cb);
        CheckBox cb_autoTheft = (CheckBox) MenuItemCompat.getActionView(menu.findItem(R.id.cb_autoTheft)).findViewById(R.id.action_view_cb);
        CheckBox cb_homocide = (CheckBox) MenuItemCompat.getActionView(menu.findItem(R.id.cb_homicide)).findViewById(R.id.action_view_cb);
        CheckBox cb_robbery = (CheckBox) MenuItemCompat.getActionView(menu.findItem(R.id.cb_robbery)).findViewById(R.id.action_view_cb);
        CheckBox cb_sexAssault = (CheckBox) MenuItemCompat.getActionView(menu.findItem(R.id.cb_sexAssault)).findViewById(R.id.action_view_cb);
        CheckBox cb_shooting = (CheckBox) MenuItemCompat.getActionView(menu.findItem(R.id.cb_shooting)).findViewById(R.id.action_view_cb);
        CheckBox cb_theftOver = (CheckBox) MenuItemCompat.getActionView(menu.findItem(R.id.cb_theftOver)).findViewById(R.id.action_view_cb);
        CheckBox cb_heatMap =(CheckBox) MenuItemCompat.getActionView(menu.findItem(R.id.cb_heatMap)).findViewById(R.id.action_view_cb);

        //initializing default checkbox values based on the saved preferences
        cb_autoTheft.setChecked(auto_theft_check_state);
        cb_assault.setChecked(assault_check_state);
        cb_homocide.setChecked(homicide_check_state);
        cb_robbery.setChecked(robbery_check_state);
        cb_sexAssault.setChecked(sex_assault_check_state);
        cb_shooting.setChecked(shooting_check_state);
        cb_theftOver.setChecked(theft_over_check_state);
        cb_heatMap.setChecked(heat_map_check_state);

        //Listener for assaults checkbox
        cb_assault.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean("assault", isChecked).apply();
                updateMapWithData();
            }
        });

        //Listener for auto theft checkbox
        cb_autoTheft.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean("autoTheft", isChecked).apply();
                updateMapWithData();
            }
        });

        //Listener for homocide checkbox
        cb_homocide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean("homicide", isChecked).apply();
                updateMapWithData();
            }
        });

        //Listener for robbery checkbox
        cb_robbery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean("robbery", isChecked).apply();
                updateMapWithData();
            }
        });

        //Listener for sexual assault checkbox
        cb_sexAssault.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean("sexAssault", isChecked).apply();
                updateMapWithData();
            }
        });

        //Listener for shootings checkbox
        cb_shooting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean("shooting", isChecked).apply();
                updateMapWithData();
            }
        });

        //Listener for theft over checkbox
        cb_theftOver.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean("theftOver", isChecked).apply();
                updateMapWithData();
            }
        });

        //Listener for the Heat Map checkbox
        cb_heatMap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean("heatMap", isChecked).apply();
                updateMapWithData();
            }
        });

        /*
        parsing all the csv files and getting the coordinates from each data set
         */
        assaults = getCsvData("assault.csv");
        autoThefts = getCsvData("auto-theft.csv");
        homicides = getCsvData("homicide.csv");
        robberies = getCsvData("robbery.csv");
        sexualAssaults = getCsvData("sexual-assault.csv");
        shootings = getCsvData("shooting.csv");
        theftOvers = getCsvData("theft-over.csv");
        assaultCoordinates = getParsedCoordinates(assaults, 0);
        autoTheftsCoordinates = getParsedCoordinates(autoThefts, 0);
        homicidesCoordinates = getParsedCoordinates(homicides, 0);
        robberiesCoordinates = getParsedCoordinates(robberies, 0);
        sexualAssaultsCoordinates = getParsedCoordinates(sexualAssaults, 0);
        shootingsCoordinates = getParsedCoordinates(shootings, 0);
        theftOversCoordinates = getParsedCoordinates(theftOvers, 0);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED    ) {
            final Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                LatLng toronto = new LatLng(43.6532, -79.3832);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 1));
                new android.os.Handler().postDelayed(
                     new Runnable() {
                          public void run() {
                              startLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                              CameraUpdate location = CameraUpdateFactory.newLatLngZoom(startLocation, 16);
                              mMap.animateCamera(location);
                          }
                     },
                     1000);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED    ) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        //Listener for the 'Jump to My Location' button
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
            @Override
            public boolean onMyLocationButtonClick() {
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    Location loc = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    if (loc != null) {
                        LatLng coordinate = new LatLng(loc.getLatitude(), loc.getLongitude());
                        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                                coordinate, 16);
                        mMap.animateCamera(location);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        });

        LatLng toronto = new LatLng(43.6532, -79.3832);

        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(toronto, 12);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(toronto));
        mMap.animateCamera(location);

        // Initializing the cluster manager with the context and the map.
        clusterManager = new ClusterManager<>(this, mMap);
        clusterManager.setRenderer(new CrimeIconRendered(this, mMap, clusterManager));
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        //Adding markers to the map
        updateMapWithData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 2) {
            if (permissions.length == 1 &&
                    permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                // Permission was denied. Display an error message.
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

    //helper function to parse csv files
    //each line becomes a String which we keep in the List
    List<String[]> getCsvData(String filename) {
        String next[];
        List<String[]> list = new ArrayList<>();
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
    //but we need to get doubles from them
    List<Double[]> getParsedCoordinates(List<String[]> data, int column){
        List<Double[]> list = new ArrayList<>();
        String[] tokens;
        for (int i = 0; i < data.size(); i++) {
            tokens = data.get(i)[column].split(" ");
            Double[] coordinates = {parseDouble(tokens[1].replace("(", "")), parseDouble(tokens[2].replace(")", ""))};
            list.add(coordinates);
        }
        return list;
    }


    //helper class needed to set clustering for the map markers
    public class MyItem implements ClusterItem {
        private final LatLng mPosition;
        private int picture;
        private String title;

        MyItem(double lat, double lng, int pictureResource, String text) {
            mPosition = new LatLng(lat, lng);
            picture = pictureResource;
            title = text;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }
        int getPicture() {
            return picture;
        }
        public String getTitle() {
            return title;
        }
    }

    public void updateMapWithData() {
        //clearing the current clusterManager
        clusterManager.clearItems();
        //retrieving current preferences
        preferences = this.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        boolean assault_check_state = preferences.getBoolean("assault", false);
        boolean auto_theft_check_state = preferences.getBoolean("autoTheft", false);
        boolean homicide_check_state = preferences.getBoolean("homicide", false);
        boolean robbery_check_state = preferences.getBoolean("robbery", false);
        boolean sex_assault_check_state = preferences.getBoolean("sexAssault", false);
        boolean shooting_check_state = preferences.getBoolean("shooting", false);
        boolean theft_over_check_state = preferences.getBoolean("theftOver", false);
        boolean heat_map_check_state = preferences.getBoolean("heatMap", false);

        /* if heatMap is selected */
        if(heat_map_check_state) {
            mMap.clear();
            if(mOverlay != null) {
                mOverlay.clearTileCache();
            }
            ArrayList<WeightedLatLng> list = new ArrayList<>();
            if(assault_check_state) {
                for (int i = 0; i < assaultCoordinates.size(); i++) {
                    list.add(new WeightedLatLng(new LatLng(assaultCoordinates.get(i)[1], assaultCoordinates.get(i)[0]), 10.0));
                }
            }
            if(auto_theft_check_state) {
                for (int i = 0; i < autoTheftsCoordinates.size(); i++) {
                    list.add(new WeightedLatLng(new LatLng(autoTheftsCoordinates.get(i)[1], autoTheftsCoordinates.get(i)[0]), 10.0));
                }
            }
            if(homicide_check_state) {
                for (int i = 0; i < homicidesCoordinates.size(); i++) {
                    list.add(new WeightedLatLng(new LatLng(homicidesCoordinates.get(i)[1], homicidesCoordinates.get(i)[0]), 10.0));
                }
            }
            if(robbery_check_state) {
                for (int i = 0; i < robberiesCoordinates.size(); i++) {
                    list.add(new WeightedLatLng(new LatLng(robberiesCoordinates.get(i)[1], robberiesCoordinates.get(i)[0]), 10.0));
                }
            }
            if(sex_assault_check_state) {
                for (int i = 0; i < sexualAssaultsCoordinates.size(); i++) {
                    list.add(new WeightedLatLng(new LatLng(sexualAssaultsCoordinates.get(i)[1], sexualAssaultsCoordinates.get(i)[0]), 10.0));
                }
            }
            if(shooting_check_state) {
                for (int i = 0; i < shootingsCoordinates.size(); i++) {
                    list.add(new WeightedLatLng(new LatLng(shootingsCoordinates.get(i)[1], shootingsCoordinates.get(i)[0]), 10.0));
                }
            }
            if(theft_over_check_state) {
                for (int i = 0; i < theftOversCoordinates.size(); i++) {
                    list.add(new WeightedLatLng(new LatLng(theftOversCoordinates.get(i)[1], theftOversCoordinates.get(i)[0]), 10.0));
                }
            }
            // Create a heat map tile provider, passing it the latlngs of the police stations.
            HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                    .radius(30)
                    .weightedData(list)
                    .build();
            // Add a tile overlay to the map, using the heat map tile provider.
            mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

          /* Heat Map is not selected, displaying regular icons and clusters */
        } else {
            mMap.clear();
            if(assault_check_state) {
                //feeding the assault cluster manager with the parsed data
                for (int i = 0; i < assaultCoordinates.size(); i++) {
                    MyItem item = new MyItem(assaultCoordinates.get(i)[1],
                            assaultCoordinates.get(i)[0],
                            R.drawable.assault,
                            "Assault, occurred at " + assaults.get(i)[5] + ":00 O'clock"
                    );
                    clusterManager.addItem(item);
                }
            }
            if(auto_theft_check_state) {
                //feeding the auto theft cluster manager with the parsed data
                for (int i = 0; i < autoTheftsCoordinates.size(); i++) {
                    MyItem item = new MyItem(autoTheftsCoordinates.get(i)[1],
                            autoTheftsCoordinates.get(i)[0],
                            R.drawable.car_theft_1,
                            "Car Theft, occurred at " + autoThefts.get(i)[5] + ":00 O'clock"
                    );
                    clusterManager.addItem(item);
                }
            }
            if(homicide_check_state) {
                //feeding the homicides cluster manager with the parsed data
                for (int i = 0; i < homicidesCoordinates.size(); i++) {
                    String[] date = homicides.get(i)[2].split("T");
                    MyItem item = new MyItem(homicidesCoordinates.get(i)[1],
                            homicidesCoordinates.get(i)[0],
                            R.drawable.homicide,
                            "Homicide, type: " + homicides.get(i)[16] + ", occurred on " + date[0]
                    );
                    clusterManager.addItem(item);
                }
            }
            if(robbery_check_state) {
                //feeding the robberies cluster manager with the parsed data
                for (int i = 0; i < robberiesCoordinates.size(); i++) {
                    MyItem item = new MyItem(robberiesCoordinates.get(i)[1],
                            robberiesCoordinates.get(i)[0],
                            R.drawable.robbery,
                            "Robbery, occurred at " + robberies.get(i)[5] + ":00 O'clock"
                    );
                    clusterManager.addItem(item);
                }
            }
            if(sex_assault_check_state) {
                //feeding the sexual assault cluster manager with the parsed data
                for (int i = 0; i < sexualAssaultsCoordinates.size(); i++) {
                    MyItem item = new MyItem(sexualAssaultsCoordinates.get(i)[1],
                            sexualAssaultsCoordinates.get(i)[0],
                            R.drawable.sexual_assault,
                            "Sexual Assault, occurred at " + sexualAssaults.get(i)[5] + ":00 O'clock"
                    );
                    clusterManager.addItem(item);
                }
            }
            if(shooting_check_state) {
                //feeding the shootings cluster manager with the parsed data
                for (int i = 0; i < shootingsCoordinates.size(); i++) {
                    String[] date = shootings.get(i)[2].split("T");
                    MyItem item = new MyItem(
                            shootingsCoordinates.get(i)[1],
                            shootingsCoordinates.get(i)[0],
                            R.drawable.gun,
                            "Shooting, occurred on " + date[0]
                    );
                    clusterManager.addItem(item);
                }
            }
            if(theft_over_check_state) {
                //feeding the theft over cluster manager with the parsed data
                for (int i = 0; i < theftOversCoordinates.size(); i++) {
                    MyItem item = new MyItem(theftOversCoordinates.get(i)[1],
                            theftOversCoordinates.get(i)[0],
                            R.drawable.theft_over,
                            "Theft Over, occurred at " + theftOvers.get(i)[5] + ":00 O'clock"
                    );
                    clusterManager.addItem(item);
                }
            }
            //This call needed to reactively update the markers
            clusterManager.cluster();
        }
    }

    /*
    This class implements custom icons for the crimes
    as well as custom icons for the Clusters
     */
    class CrimeIconRendered extends DefaultClusterRenderer<MyItem> {

        CrimeIconRendered(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }
        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(item.getPicture())).title(item.getTitle());
            super.onBeforeClusterItemRendered(item, markerOptions);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            Bitmap icon = drawTextToBitmap(R.drawable.crime_cluster_2, String.valueOf(cluster.getSize()) );
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }
    /*
    helper function which combines icon and a counter and creates a custom icon for the cluster
     */
    public Bitmap drawTextToBitmap(int gResId, String gText) {
        Resources resources = getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, gResId);
        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();

        if ( bitmapConfig == null ) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        /* SET FONT COLOR (e.g. WHITE -> rgb(255,255,255)) */
        paint.setColor(Color.rgb(198, 19, 28));
        /* SET FONT SIZE (e.g. 15) */
        paint.setTextSize((int) (15 * scale));
        /* SET TEXT BOLD */
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        /* SET SHADOW WIDTH, POSITION AND COLOR (e.g. BLACK) */
        paint.setShadowLayer(1f, 0f, 1f, Color.BLACK);

        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width())/2;
        int y = (bitmap.getHeight() + bounds.height())/2;
        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }
}
