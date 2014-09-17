package app.clinicloc.com.view;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.clinicloc.com.bean.Tenant;
import app.clinicloc.com.json.JSONParser;
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

    private Tenant tenant;
    private ListView listView;
    private JSONParser jsonParser;
    private JSONObject jsonObject;
    private String serverURL;
    private List<NameValuePair> params;
    private String responseMessage;
    private ListAdapter adapter;
    private List<Tenant> tenants;
    private JSONArray jsonTenants;
    private JSONObject jsonTenant;
    private HashMap<String, String> tenantMap;
    private ArrayList<HashMap<String, String>> tenantList;
    private double latitude = 0;
    private double longitude = 0;

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

                if(latitude > 0 && longitude > 0){
                    params.add(new BasicNameValuePair("lat", ""+latitude));
                    params.add(new BasicNameValuePair("long", ""+longitude));
                    params.add(new BasicNameValuePair("no", "5"));

                    new LoadAllTenant().execute();
                }

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

    class LoadAllTenant extends AsyncTask<String, String, String> {

        public void onPreExecute(){
            // keluarkan loading bar & teks

//            pgLoadingTenant.setVisibility(View.VISIBLE);
//            tvLoadingCpny.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {

            try {

                responseMessage = " ";

                tenantList = new ArrayList<HashMap<String, String>>();

                jsonParser = new JSONParser();
                jsonObject = jsonParser.getJSONFromUrl(serverURL, params);

                jsonTenants = jsonObject.getJSONArray("string");

                for (int i = 0; i < jsonTenants.length(); i++) {

                    tenantMap = new HashMap<String, String>();

                    jsonTenant = jsonTenants.getJSONObject(i);

                    double jsonLatitude = Double.parseDouble(jsonTenant.getString("latitude"));
                    double jsonLongitude = Double.parseDouble(jsonTenant.getString("longitude"));

                    tenantMap.put("tenant_id", jsonTenant.getString("tenant_id"));
                    tenantMap.put("company_name", jsonTenant.getString("company_name"));
                    tenantMap.put("jarak", jsonTenant.getString("jarak")+"km");

                    drawMarker(new LatLng(jsonLatitude, jsonLongitude), "Klinik Damai", i);

                    tenantList.add(tenantMap);

                }

            }catch (JSONException jse){
                responseMessage = "Tidak dapat menghubungi pelayan.";
            }catch (Exception e){
                responseMessage = "Tidak dapat menghubungi pelayan.";
            }

            return null;
        }

        // lepas proses selesai
        public void onPostExecute(String args){

            // hide kan blik loading bar ngn teks
//            pgLoadingTenant.setVisibility(View.GONE);
//            tvLoadingCpny.setVisibility(View.GONE);

            // cek jika tiada nilai atau error, keluar alert dan kembali ke activity sblumnye
            if(responseMessage.trim().length() > 0) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
                alert.setMessage(responseMessage);
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
                    }
                });
                alert.show();
                // kalau x de error
            }else{
                // masukkan nilai list dlm listview xml

            }
        }
    }

}
