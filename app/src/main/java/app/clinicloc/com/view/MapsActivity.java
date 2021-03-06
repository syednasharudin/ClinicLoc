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
import android.util.Log;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
    private TextView mapClinicAddress;
    private TextView mapClinicContacNo;
    private TextView mapCountPatient;
    private FrameLayout mapClinicInfoLayout;
    private FrameLayout mapClinicLayout;
    private boolean firstTime;
    private boolean alreadyDraw;

    private JSONParser jsonParser;
    private JSONObject jsonObject;
    private String serverURL;
    private List<NameValuePair> params;
    private String responseMessage;
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
        mapClinicAddress = (TextView) findViewById(R.id.map_clinicAddress);
        mapClinicContacNo = (TextView) findViewById(R.id.map_clinicContactNo);
        mapCountPatient  = (TextView) findViewById(R.id.map_count_patient);
        mapClinicInfoLayout = (FrameLayout) findViewById(R.id.map_data);
        mapClinicLayout = (FrameLayout) findViewById(R.id.map_layout);
        serverURL = getResources().getString(R.string.server_url)+"tenant.php";
        tenantList = new ArrayList<HashMap<String, String>>();
        params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("lat", ""+latitude));
        params.add(new BasicNameValuePair("long", ""+longitude));

        if(!alreadyDraw)
            new loadMapTenants().execute();

        try {
            // Loading map
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
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

        if(latitude > 0 && longitude > 0){
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }else{
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            params.add(new BasicNameValuePair("lat", ""+latitude));
            params.add(new BasicNameValuePair("long", ""+longitude));

            if(!alreadyDraw)
                new loadMapTenants().execute();
        }
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

    public void drawMultipleMarker(){

        googleMap.clear();

        for(int i=0; i<tenantList.size(); i++){

            double drawLat = Double.parseDouble(tenantList.get(i).get("latitude"));
            double drawLong = Double.parseDouble(tenantList.get(i).get("longitude"));

//            Log.w("Index2: "+i, tenantList.get(i).get("company_name"));

            drawMarker(new LatLng(drawLat, drawLong), tenantList.get(i).get("company_name"), i);
        }

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

                int index = Integer.parseInt(marker.getTitle());

                mapClinicName.setText(tenantList.get(index).get("company_name"));
                //
                mapClinicAddress.setText(tenantList.get(index).get("address"));
                mapClinicContacNo.setText(tenantList.get(index).get("no_tel"));
                mapCountPatient.setText(tenantList.get(index).get("total"));
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                marker.hideInfoWindow();
                return true;
            }
        });

    }

    private void drawMarker(LatLng point, String title, int index){
        // Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting latitude and longitude for the marker
        markerOptions.position(point);
        markerOptions.title(""+index);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.cl_logo_official));

        // Adding marker on the Google Map
        googleMap.addMarker(markerOptions).hideInfoWindow();
    }

    class loadMapTenants extends AsyncTask<String, Integer, String> {

        public void onPreExecute(){
            // keluarkan loading bar & teks

//            pgLoadingTenant.setVisibility(View.VISIBLE);
//            tvLoadingCpny.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected String doInBackground(String... args) {

            try {

                responseMessage = " ";

                jsonParser = new JSONParser();
                jsonObject = jsonParser.getJSONFromUrl(serverURL, params);

                jsonTenants = jsonObject.getJSONArray("string");

                for (int j = 0; j < jsonTenants.length(); j++) {

                    tenantMap = new HashMap<String, String>();

                    jsonTenant = jsonTenants.getJSONObject(j);

                    tenantMap.put("tenant_id", jsonTenant.getString("tenant_id"));
                    tenantMap.put("company_name", jsonTenant.getString("company_name"));
                    tenantMap.put("latitude", jsonTenant.getString("latitude"));
                    tenantMap.put("longitude", jsonTenant.getString("longitude"));
                    tenantMap.put("jarak", jsonTenant.getString("jarak")+"km");
                    tenantMap.put("address", jsonTenant.getString("address")+", "
                            +jsonTenant.getString("postcode")+", "+jsonTenant.getString("city")+
                            ", "+jsonTenant.getString("state"));
                    tenantMap.put("total", jsonTenant.getString("total"));
                    tenantMap.put("no_tel", jsonTenant.getString("no_tel"));

                    tenantList.add(j, tenantMap);

//                    Log.w("Index: "+j, tenantList.get(j).get("company_name"));
//                    tenantList.add(tenantMap);

                }

            }catch (JSONException jse){
                responseMessage = "Could not fetch data from server.";
            }catch (Exception e){
                responseMessage = "Could not fetch data from server.";
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

                if(!alreadyDraw) {
                    drawMultipleMarker();
                    alreadyDraw = true;
                }
            }
        }
    }

}