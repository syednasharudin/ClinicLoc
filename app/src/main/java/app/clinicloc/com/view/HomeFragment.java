package app.clinicloc.com.view;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
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
    private XMLParser xmlParser;
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

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        listView = (ListView) rootView.findViewById(R.id.lvListOfTenant);
        pgLoadingTenant = (ProgressBar) rootView.findViewById(R.id.pgr_loading_bar);
        serverURL = getResources().getString(R.string.server_url)+"tenant";

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

            xmlParser = new XMLParser();
            String xml = xmlParser.getXmlFromUrl("http://api.androidhive.info/pizza/?format=xml");
            Document doc = xmlParser.getDomElement(xml); // getting DOM element

            NodeList nl = doc.getElementsByTagName("item");

            if(nl.getLength() > 0) {

                tenantList = new ArrayList<HashMap<String, String>>();

                for (int i = 0; i < nl.getLength(); i++) {
                    responseMessage = " ";
                    tenantMap = new HashMap<String, String>();

                    Element e = (Element) nl.item(i);
                    // adding each child node to HashMap key => value
                    tenantMap.put("id", xmlParser.getValue(e, "id"));
                    tenantMap.put("cost", xmlParser.getValue(e, "cost"));
                    tenantMap.put("name", xmlParser.getValue(e, "name"));

                    Log.d("Data: ", tenantMap.get("name"));
                    // adding HashList to ArrayList
                    tenantList.add(tenantMap);
                }

            }else{
                responseMessage = "No Data";
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
                        R.layout.list_item_tenant, new String[] { "id",
                        "name", "cost"},
                        new int[] { R.id.tv_tenant_id, R.id.tv_tenant_name, R.id.tv_company_name });
                // updating listview
                listView.setAdapter(adapter);
            }
        }
    }

}
