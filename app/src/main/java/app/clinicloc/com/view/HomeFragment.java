package app.clinicloc.com.view;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.NameValuePair;
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
public class HomeFragment extends Fragment {

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
    //private Button btnOpenMap;
    Spinner spinnerPanelFilter;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        listView = (ListView) rootView.findViewById(R.id.lvListOfTenant);
        pgLoadingTenant = (ProgressBar) rootView.findViewById(R.id.pgr_loading_bar);
        //btnOpenMap = (Button) rootView.findViewById(R.id.btn_open_map);
        serverURL = getResources().getString(R.string.server_url)+"clinicloc2.php";
        spinnerPanelFilter = (Spinner) rootView.findViewById(R.id.spinnerPanel);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount()-1; // you dont display last item. It is used as hint.
            }

        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add("Red Alert");
        adapter.add("Green Alert");
        adapter.add("Yellow Alert");
        adapter.add("Hint to be displayed");

        spinnerPanelFilter.setSelection(adapter.getCount()); //display hint
        spinnerPanelFilter.setAdapter(adapter);
        //spinnerPanelFilter.setOnItemSelectedListener(this);

//        btnOpenMap.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(rootView.getContext(), MapsActivity.class);
//                startActivity(intent);
//            }
//        });

        new LoadAllTenant().execute();

        return rootView;
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
                params = new ArrayList<NameValuePair>();

                tenantList = new ArrayList<HashMap<String, String>>();

                jsonParser = new JSONParser();
                jsonObject = jsonParser.getJSONFromUrl(serverURL, params);

                jsonTenants = jsonObject.getJSONArray("string");

                for (int i = 0; i < jsonTenants.length(); i++) {

                    tenantMap = new HashMap<String, String>();

                    jsonTenant = jsonTenants.getJSONObject(i);

                    tenantMap.put("tenant_id", jsonTenant.getString("tenant_id"));
                    tenantMap.put("name", jsonTenant.getString("name"));
                    tenantMap.put("company_name", jsonTenant.getString("company_name"));


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
                        "name", "company_name"},
                        new int[] { R.id.tv_tenant_id, R.id.tv_tenant_name, R.id.tv_company_name });
                // updating listview
                listView.setAdapter(adapter);
            }
        }
    }

}
