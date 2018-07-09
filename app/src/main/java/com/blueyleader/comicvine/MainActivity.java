package com.blueyleader.comicvine;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String web_base = "https://comicvine.gamespot.com/api/";

    private static final String web_charater = "character/";
    private static final String web_volumes = "volumes/";
    private static final String web_issues = "issues/";

    private static final String web_api_key = "?api_key=";
    private static final String web_format = "&format=json";
    private static final String web_amp = "&";
    private static final String web_filter_id = "&filter=id:";

    //json strings
    public final static String json_results = "results";
    public final static String json_issue_credits = "issue_credits";
    public final static String json_volume_credits = "volume_credits";
    public final static String json_id = "id";
    public final static String json_name = "id";
    public final static String json_count_of_issue_appearances = "count_of_issue_appearances";

    public HashMap<Integer,Volume> set;
    public String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtJson = findViewById(R.id.text);
        set = new HashMap<>();
        new JsonTask().execute("https://comicvine.gamespot.com/api/character/4005-2349/?api_key=57e9f1dc4a6a9bdde575ca93d60621da18dcd080&format=json");
    }

    public void parseCharater(String in){
        try {
            JSONObject base = new JSONObject(in);
            JSONObject root  = base.getJSONObject(json_results);
            JSONArray issues = root.getJSONArray(json_issue_credits);
            JSONArray volumes = root.getJSONArray(json_volume_credits);

            //build list of volumes that character is in
            for(int x=0;x<volumes.length();x++){
                JSONObject temp = volumes.getJSONObject(x);
                int id = temp.getInt(json_id);
                if(set.containsKey(id)){
                    //TODO key is already there
                }
                else{
                    set.put(id,new Volume(id+"",temp.getString(json_name)));
                }
                //need to get year after
            }

            //go through each issue to find which volume it is in
            ArrayList<String> rip = new ArrayList<>();
            for(int x=0;x<issues.length();x=x+100){
                //https://comicvine.gamespot.com/api/volumes/?api_key=57e9f1dc4a6a9bdde575ca93d60621da18dcd080&format=json&filter=id:2501|2505
                StringBuilder sb = new StringBuilder(web_base + web_issues + web_api_key+key+web_format+web_filter_id);
                for(int y=x;y<x+100;y++){
                    sb.append(issues.getJSONObject(x).getInt(json_id));
                    sb.append("|");
                }
                rip.add(sb.toString());
            }

            for(int x=0;x<rip.size();x++) {
                try {
                    HttpURLConnection connection = null;
                    BufferedReader reader = null;

                    try {
                        URL url = new URL(rip.get(x));
                        connection = (HttpURLConnection) url.openConnection();
                        connection.connect();


                        InputStream stream = connection.getInputStream();

                        reader = new BufferedReader(new InputStreamReader(stream));

                        StringBuffer buffer = new StringBuffer();
                        String line = "";

                        while ((line = reader.readLine()) != null) {
                            buffer.append(line+"\n");
                            Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                        }

                        JSONObject issue_base = new JSONObject(buffer.toString());


                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                        try {
                            if (reader != null) {
                                reader.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    buffer.toString();

                    Thread.sleep(1000);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }




        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    Button btnHit;
    TextView txtJson;
    ProgressDialog pd;

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            txtJson.setText(result);
        }
    }
}
