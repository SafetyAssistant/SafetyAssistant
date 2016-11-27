package com.seneca.map524.safetyassistant;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
    private ClusterManager<MyItem> assaultClusterManager;
    private ClusterManager<MyItem> autoTheftClusterManager;
    private ClusterManager<MyItem> homicideClusterManager;
    private ClusterManager<MyItem> robberyClusterManager;
    private ClusterManager<MyItem> sexAssaultClusterManager;
    private ClusterManager<MyItem> shootingClusterManager;
    private ClusterManager<MyItem> theftOverClusterManager;

    private static boolean heatMap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
    public void onConnectionFailed(ConnectionResult connectionResult) {

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

        /*
        parsing all the csv files and getting the coordinates from each data set
         */
        List<String[]> assaults = getCsvData("assault.csv");
        List<String[]> autoThefts = getCsvData("auto-theft.csv");
        List<String[]> homicides = getCsvData("homicide.csv");
        List<String[]> robberies = getCsvData("robbery.csv");
        List<String[]> sexualAssaults = getCsvData("sexual-assault.csv");
        List<String[]> shootings = getCsvData("shooting.csv");
        List<String[]> theftOvers = getCsvData("theft-over.csv");
        List<Double[]> assaultCoordinates = getParsedCoordinates(assaults, 0);
        List<Double[]> autoTheftsCoordinates = getParsedCoordinates(autoThefts, 0);
        List<Double[]> homicidesCoordinates = getParsedCoordinates(homicides, 0);
        List<Double[]> robberiesCoordinates = getParsedCoordinates(robberies, 0);
        List<Double[]> sexualAssaultsCoordinates = getParsedCoordinates(sexualAssaults, 0);
        List<Double[]> shootingsCoordinates = getParsedCoordinates(shootings, 0);
        List<Double[]> theftOversCoordinates = getParsedCoordinates(theftOvers, 0);

        /* if heatMap is selected */
        if(heatMap) {
            ArrayList<WeightedLatLng> list = new ArrayList<WeightedLatLng>();
            for(int i = 0; i < assaultCoordinates.size(); i++) {
                list.add(new WeightedLatLng(new LatLng(assaultCoordinates.get(i)[1], assaultCoordinates.get(i)[0]), 10.0));
            }
            for(int i = 0; i < autoTheftsCoordinates.size(); i++) {
                list.add(new WeightedLatLng(new LatLng(autoTheftsCoordinates.get(i)[1], autoTheftsCoordinates.get(i)[0]), 10.0));
            }
            for(int i = 0; i < homicidesCoordinates.size(); i++) {
                list.add(new WeightedLatLng(new LatLng(homicidesCoordinates.get(i)[1], homicidesCoordinates.get(i)[0]), 10.0));
            }
            for(int i = 0; i < robberiesCoordinates.size(); i++) {
                list.add(new WeightedLatLng(new LatLng(robberiesCoordinates.get(i)[1], robberiesCoordinates.get(i)[0]), 10.0));
            }
            for(int i = 0; i < sexualAssaultsCoordinates.size(); i++) {
                list.add(new WeightedLatLng(new LatLng(sexualAssaultsCoordinates.get(i)[1], sexualAssaultsCoordinates.get(i)[0]), 10.0));
            }
            for(int i = 0; i < shootingsCoordinates.size(); i++) {
                list.add(new WeightedLatLng(new LatLng(shootingsCoordinates.get(i)[1], shootingsCoordinates.get(i)[0]), 10.0));
            }
            for(int i = 0; i < theftOversCoordinates.size(); i++) {
                list.add(new WeightedLatLng(new LatLng(theftOversCoordinates.get(i)[1], theftOversCoordinates.get(i)[0]), 10.0));
            }
            // Create a heat map tile provider, passing it the latlngs of the police stations.
            HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                    .radius(30)
                    .weightedData(list)
                    .build();
            // Add a tile overlay to the map, using the heat map tile provider.
            TileOverlay mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
          /* Heat Map is not selected, displaying regular icons and clusters */
        } else {
            // Initialize the cluster managers with the context and the map.
            assaultClusterManager = new ClusterManager<MyItem>(this, mMap);
            autoTheftClusterManager = new ClusterManager<MyItem>(this, mMap);
            homicideClusterManager = new ClusterManager<MyItem>(this, mMap);
            robberyClusterManager = new ClusterManager<MyItem>(this, mMap);
            sexAssaultClusterManager = new ClusterManager<MyItem>(this, mMap);
            shootingClusterManager = new ClusterManager<MyItem>(this, mMap);
            theftOverClusterManager = new ClusterManager<MyItem>(this, mMap);

            //Set custom renderers for each cluster manager in order to display custom icons
            assaultClusterManager.setRenderer(new AssaultIconRendered(this, mMap, assaultClusterManager));
            autoTheftClusterManager.setRenderer(new AutoTheftIconRendered(this, mMap, autoTheftClusterManager));
            homicideClusterManager.setRenderer(new HomicideIconRendered(this, mMap, homicideClusterManager));
            robberyClusterManager.setRenderer(new RobberyIconRendered(this, mMap, robberyClusterManager));
            sexAssaultClusterManager.setRenderer(new SexAssaultIconRendered(this, mMap, sexAssaultClusterManager));
            shootingClusterManager.setRenderer(new ShootingIconRendered(this, mMap, shootingClusterManager));
            theftOverClusterManager.setRenderer(new TheftOverIconRendered(this, mMap, theftOverClusterManager));


            // Point the map's listeners at the listeners implemented by the cluster managers
            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    assaultClusterManager.onCameraIdle();
                    autoTheftClusterManager.onCameraIdle();
                    homicideClusterManager.onCameraIdle();
                    robberyClusterManager.onCameraIdle();
                    sexAssaultClusterManager.onCameraIdle();
                    shootingClusterManager.onCameraIdle();
                    theftOverClusterManager.onCameraIdle();
                }
            });

            //feeding the assault cluster manager with the parsed data
            for(int i = 0; i < assaultCoordinates.size(); i++) {
                MyItem item = new MyItem(assaultCoordinates.get(i)[1], assaultCoordinates.get(i)[0]);
                assaultClusterManager.addItem(item);
            }

            //feeding the auto theft cluster manager with the parsed data
            for(int i = 0; i < autoTheftsCoordinates.size(); i++) {
                MyItem item = new MyItem(autoTheftsCoordinates.get(i)[1], autoTheftsCoordinates.get(i)[0]);
                autoTheftClusterManager.addItem(item);
            }

            //feeding the homicides cluster manager with the parsed data
            for(int i = 0; i < homicidesCoordinates.size(); i++) {
                MyItem item = new MyItem(homicidesCoordinates.get(i)[1], homicidesCoordinates.get(i)[0]);
                homicideClusterManager.addItem(item);
            }

            //feeding the robberies cluster manager with the parsed data
            for(int i = 0; i < robberiesCoordinates.size(); i++) {
                MyItem item = new MyItem(robberiesCoordinates.get(i)[1], robberiesCoordinates.get(i)[0]);
                robberyClusterManager.addItem(item);
            }

            //feeding the sexual assault cluster manager with the parsed data
            for(int i = 0; i < sexualAssaultsCoordinates.size(); i++) {
                MyItem item = new MyItem(sexualAssaultsCoordinates.get(i)[1], sexualAssaultsCoordinates.get(i)[0]);
                sexAssaultClusterManager.addItem(item);
            }

            //feeding the shootings cluster manager with the parsed data
            for(int i = 0; i < shootingsCoordinates.size(); i++) {
                MyItem item = new MyItem(shootingsCoordinates.get(i)[1], shootingsCoordinates.get(i)[0]);
                shootingClusterManager.addItem(item);
            }

            //feeding the theft over cluster manager with the parsed data
            for(int i = 0; i < theftOversCoordinates.size(); i++) {
                MyItem item = new MyItem(theftOversCoordinates.get(i)[1], theftOversCoordinates.get(i)[0]);
                theftOverClusterManager.addItem(item);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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

    //helper function to parse csv files
    //each line becomes a String which we keep in the List
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
    //but we need to get doubles from them
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


    //helper class needed to set clustering for the map markers
    public class MyItem implements ClusterItem {
        private final LatLng mPosition;

        public MyItem(double lat, double lng) {
            mPosition = new LatLng(lat, lng);
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }
    }

    /*
    This class implements custom icons for assault markers
    as well as custom icons for their Clusters
     */
    class AssaultIconRendered extends DefaultClusterRenderer<MyItem> {

        public AssaultIconRendered(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.assault));
            super.onBeforeClusterItemRendered(item, markerOptions);
        }
        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {

//            Context mContext = getApplicationContext();
//            final Drawable clusterIcon = ContextCompat.getDrawable(mContext, R.drawable.assault);
//            //final Drawable clusterIcon = getResources().getDrawable(R.drawable.assault);
//
//            IconGenerator mClusterIconGenerator = new IconGenerator(mContext);
//            clusterIcon.setColorFilter(getResources().getColor(android.R.color.holo_orange_light), PorterDuff.Mode.SRC_ATOP);
//            mClusterIconGenerator.setBackground(clusterIcon);
//
//            //modify padding for one or two digit numbers
//            if (cluster.getSize() < 10) {
//                mClusterIconGenerator.setContentPadding(40, 20, 0, 0);
//            }
//            else {
//                mClusterIconGenerator.setContentPadding(30, 20, 0, 0);
//            }

            Bitmap icon = drawTextToBitmap(R.drawable.assault,String.valueOf(cluster.getSize()) );
            //mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));

            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

        }

    }

    /*
    This class implements custom icons for auto theft markers
    as well as custom icons for their Clusters
     */
    class AutoTheftIconRendered extends DefaultClusterRenderer<MyItem> {

        public AutoTheftIconRendered(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }
        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_theft_1));
            super.onBeforeClusterItemRendered(item, markerOptions);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            Bitmap icon = drawTextToBitmap(R.drawable.car_theft_1, String.valueOf(cluster.getSize()) );
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }

    /*
    This class implements custom icons for homicide markers
    as well as custom icons for their Clusters
     */
    class HomicideIconRendered extends DefaultClusterRenderer<MyItem> {

        public HomicideIconRendered(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }
        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.homicide));
            super.onBeforeClusterItemRendered(item, markerOptions);
        }
        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            Bitmap icon = drawTextToBitmap(R.drawable.homicide, String.valueOf(cluster.getSize()) );
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }
    /*
    This class implements custom icons for robbery markers
    as well as custom icons for their Clusters
     */
    class RobberyIconRendered extends DefaultClusterRenderer<MyItem> {

        public RobberyIconRendered(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }
        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.robbery));
            super.onBeforeClusterItemRendered(item, markerOptions);
        }
        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            Bitmap icon = drawTextToBitmap(R.drawable.robbery, String.valueOf(cluster.getSize()) );
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }

    /*
    This class implements custom icons for sexual assault markers
    as well as custom icons for their Clusters
     */
    class SexAssaultIconRendered extends DefaultClusterRenderer<MyItem> {

        public SexAssaultIconRendered(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }
        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.sexual_assault));
            super.onBeforeClusterItemRendered(item, markerOptions);
        }
        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            Bitmap icon = drawTextToBitmap(R.drawable.sexual_assault, String.valueOf(cluster.getSize()) );
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }

    /*
    This class implements custom icons for shooting markers
    as well as custom icons for their Clusters
     */
    class ShootingIconRendered extends DefaultClusterRenderer<MyItem> {

        public ShootingIconRendered(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }
        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.gun));
            super.onBeforeClusterItemRendered(item, markerOptions);
        }
        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            Bitmap icon = drawTextToBitmap(R.drawable.gun, String.valueOf(cluster.getSize()) );
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }
    /*
    This class implements custom icons for theft over markers
    as well as custom icons for their Clusters
     */
    class TheftOverIconRendered extends DefaultClusterRenderer<MyItem> {

        public TheftOverIconRendered(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }
        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.theft_over));
            super.onBeforeClusterItemRendered(item, markerOptions);
        }
        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            Bitmap icon = drawTextToBitmap(R.drawable.theft_over, String.valueOf(cluster.getSize()) );
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
