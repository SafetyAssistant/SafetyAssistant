package com.seneca.map524.safetyassistant;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
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
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import static java.lang.Double.parseDouble;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ClusterManager<MyItem> assaultClusterManager;
    private ClusterManager<MyItem> autoTheftClusterManager;
    private ClusterManager<MyItem> homicideClusterManager;
    private ClusterManager<MyItem> robberyClusterManager;
    private ClusterManager<MyItem> sexAssaultClusterManager;
    private ClusterManager<MyItem> shootingClusterManager;
    private ClusterManager<MyItem> theftOverClusterManager;

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

        // Move the camera to the center of Toronto
        LatLng toronto = new LatLng(43.6532, -79.3832);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(toronto));
        mMap.animateCamera(CameraUpdateFactory.zoomTo( 12.0f ));

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

        //reading data from csv and feeding it to the assault cluster manager
        List<String[]> assaults = getCsvData("assault.csv");
        List<Double[]> assaultCoordinates = getParsedCoordinates(assaults, 0);
        for(int i = 0; i < assaultCoordinates.size(); i++) {
            MyItem item = new MyItem(assaultCoordinates.get(i)[1], assaultCoordinates.get(i)[0]);
            assaultClusterManager.addItem(item);
        }

        //reading data from csv and feeding it to the auto theft cluster manager
        List<String[]> autoThefts = getCsvData("auto-theft.csv");
        List<Double[]> autoTheftsCoordinates = getParsedCoordinates(autoThefts, 0);
        for(int i = 0; i < autoTheftsCoordinates.size(); i++) {
            MyItem item = new MyItem(autoTheftsCoordinates.get(i)[1], autoTheftsCoordinates.get(i)[0]);
            autoTheftClusterManager.addItem(item);
        }

        //reading data from csv and feeding it to the homicides cluster manager
        List<String[]> homicides = getCsvData("homicide.csv");
        List<Double[]> homicidesCoordinates = getParsedCoordinates(homicides, 0);
        for(int i = 0; i < homicidesCoordinates.size(); i++) {
            MyItem item = new MyItem(homicidesCoordinates.get(i)[1], homicidesCoordinates.get(i)[0]);
            homicideClusterManager.addItem(item);
        }

        //reading data from csv and feeding it to the robbery cluster manager
        List<String[]> robberies = getCsvData("robbery.csv");
        List<Double[]> robberiesCoordinates = getParsedCoordinates(robberies, 0);
        for(int i = 0; i < robberiesCoordinates.size(); i++) {
            MyItem item = new MyItem(robberiesCoordinates.get(i)[1], robberiesCoordinates.get(i)[0]);
            robberyClusterManager.addItem(item);
        }

        //reading data from csv and feeding it to the sexual assault cluster manager
        List<String[]> sexualAssaults = getCsvData("sexual-assault.csv");
        List<Double[]> sexualAssaultsCoordinates = getParsedCoordinates(sexualAssaults, 0);
        for(int i = 0; i < sexualAssaultsCoordinates.size(); i++) {
            MyItem item = new MyItem(sexualAssaultsCoordinates.get(i)[1], sexualAssaultsCoordinates.get(i)[0]);
            sexAssaultClusterManager.addItem(item);
        }

        //reading data from csv and feeding it to the shooting cluster manager
        List<String[]> shootings = getCsvData("shooting.csv");
        List<Double[]> shootingsCoordinates = getParsedCoordinates(shootings, 0);
        for(int i = 0; i < shootingsCoordinates.size(); i++) {
            MyItem item = new MyItem(shootingsCoordinates.get(i)[1], shootingsCoordinates.get(i)[0]);
            shootingClusterManager.addItem(item);
        }

        //reading data from csv and feeding it to the theft over cluster manager
        List<String[]> theftOvers = getCsvData("theft-over.csv");
        List<Double[]> theftOversCoordinates = getParsedCoordinates(theftOvers, 0);
        for(int i = 0; i < theftOversCoordinates.size(); i++) {
            MyItem item = new MyItem(theftOversCoordinates.get(i)[1], theftOversCoordinates.get(i)[0]);
            theftOverClusterManager.addItem(item);
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
