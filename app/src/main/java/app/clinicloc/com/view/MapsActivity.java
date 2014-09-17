package app.clinicloc.com.view;

import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import app.clinicloc.com.view.anim.WeightAnimation;

public class MapsActivity extends FragmentActivity implements LocationListener {

    // Google Map
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private TextView mapClinicName;
    private FrameLayout mapClinicInfoLayout;
    private FrameLayout mapClinicLayout;
    private boolean firstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mapClinicName = (TextView) findViewById(R.id.map_clinicName);
        mapClinicInfoLayout = (FrameLayout) findViewById(R.id.map_data);
        mapClinicLayout = (FrameLayout) findViewById(R.id.map_layout);

        try {
            // Loading map
            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * function to load map. If map is not created it will create it for you
     * */
    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }else{

                drawMarker(new LatLng(3.160862, 101.726322), "Klinik Damai", 0);
                drawMarker(new LatLng(3.163090, 101.696796), "Klinik Raja", 1);
                drawMarker(new LatLng(3.143894, 101.697998), "Klinik Al-Qassam", 2);

                googleMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(4.210484000000000000,101.975766000000020000) , 8) );
                googleMap.setMyLocationEnabled(true);
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
//                        Toast.makeText(getApplicationContext(), "Index: "+marker.getTitle(), Toast.LENGTH_LONG);


                        if(!firstTime) {
                            firstTime = true;
                            WeightAnimation weightAnimation = new WeightAnimation(mapClinicInfoLayout, 2);
                            weightAnimation.setDuration(1000);
                            WeightAnimation weightAnimation2 = new WeightAnimation(mapClinicLayout, 7);
                            weightAnimation2.setDuration(1000);
                            mapClinicInfoLayout.startAnimation(weightAnimation);
                            mapClinicLayout.startAnimation(weightAnimation2);
                        }

                        mapClinicName.setText(marker.getTitle());
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                        marker.hideInfoWindow();
                        return true;
                    }
                });

                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap();
    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        googleMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void drawMarker(LatLng point, String title, int index){
        // Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting latitude and longitude for the marker
        markerOptions.position(point);
        markerOptions.title(title);
//        markerOptions.describeContents();

        // Adding marker on the Google Map
        googleMap.addMarker(markerOptions).hideInfoWindow();
    }

}
