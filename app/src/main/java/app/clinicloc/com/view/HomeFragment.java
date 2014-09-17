package app.clinicloc.com.view;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.clinicloc.com.bean.Tenant;
import app.clinicloc.com.json.JSONParser;
import app.clinicloc.com.json.XMLParser;

/**
 * Created by syednasharudin on 9/17/2014.
 */
public class HomeFragment extends Fragment implements LocationListener {

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
    private ProgressBar pgLoadingTenant;
    private Button btnOpenMap;
    private View rootView;
    private double latitude = 0;
    private double longitude = 0;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private int listItemPosition = 0;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        listView = (ListView) rootView.findViewById(R.id.lvListOfTenant);
        pgLoadingTenant = (ProgressBar) rootView.findViewById(R.id.pgr_loading_bar);
        //btnOpenMap = (Button) rootView.findViewById(R.id.btn_open_map);
        serverURL = getResources().getString(R.string.server_url)+"tenant.php";

        params = new ArrayList<NameValuePair>();

//        btnOpenMap.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(rootView.getContext(), MapsActivity.class);
//                startActivity(intent);
//            }
//        });

//        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                listItemPosition = position;
                PopupMenu popupMenu = new PopupMenu(getActivity(), view);
                popupMenu.getMenuInflater().inflate(R.menu.clinic_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        double navLat = Double.parseDouble(tenantList.get(position).get("latitude"));
                        double navLong = Double.parseDouble(tenantList.get(position).get("longitude"));

                        Intent navigation = new Intent(Intent.ACTION_VIEW, Uri
                                .parse("http://maps.google.com/maps?saddr="
                                        + latitude + ","
                                        + longitude + "&daddr="
                                        + navLat + "," + navLong));
                        startActivity(navigation);
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

        if(latitude > 0 && longitude > 0){
            params.add(new BasicNameValuePair("lat", ""+latitude));
            params.add(new BasicNameValuePair("long", ""+longitude));
            params.add(new BasicNameValuePair("no", "5"));

            new LoadAllTenant().execute();
        }

        return rootView;
    }

    @Override
    public void onLocationChanged(Location location) {

        if(latitude > 0 && longitude > 0){
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }else{
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            params.add(new BasicNameValuePair("lat", ""+latitude));
            params.add(new BasicNameValuePair("long", ""+longitude));
            params.add(new BasicNameValuePair("no", "5"));

            new LoadAllTenant().execute();
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

    // connect server
    class LoadAllTenant extends AsyncTask<String, Integer, String> {

        public void onPreExecute(){
            // keluarkan loading bar & teks

            pgLoadingTenant.setVisibility(View.VISIBLE);
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

                tenantList = new ArrayList<HashMap<String, String>>();

                jsonParser = new JSONParser();
                jsonObject = jsonParser.getJSONFromUrl(serverURL, params);

                jsonTenants = jsonObject.getJSONArray("string");

                for (int i = 0; i < jsonTenants.length(); i++) {

                    tenantMap = new HashMap<String, String>();

                    jsonTenant = jsonTenants.getJSONObject(i);

                    tenantMap.put("tenant_id", jsonTenant.getString("tenant_id"));
                    tenantMap.put("company_name", jsonTenant.getString("company_name"));
                    tenantMap.put("jarak", jsonTenant.getString("jarak")+"km");
                    tenantMap.put("latitude", jsonTenant.getString("latitude"));
                    tenantMap.put("longitude", jsonTenant.getString("longitude"));

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
            pgLoadingTenant.setVisibility(View.GONE);
//            tvLoadingCpny.setVisibility(View.GONE);

            // cek jika tiada nilai atau error, keluar alert dan kembali ke activity sblumnye
            if(responseMessage.trim().length() > 0) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
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
                adapter = new SimpleAdapter(
                        getActivity(), tenantList,
                        R.layout.list_item_tenant, new String[] { "tenant_id",
                        "company_name", "jarak"},
                        new int[] { R.id.tv_tenant_id, R.id.tv_tenant_name, R.id.tv_company_name });
                // updating listview
                listView.setAdapter(adapter);

            }
        }
    }

}