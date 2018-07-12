package com.blueyleader.comicvine;

import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private static final String web_base = "https://comicvine.gamespot.com/api/";

    private static final String web_charater = "character/";
    private static final String web_charaters = "characters/";
    private static final String web_volume = "volume/";
    private static final String web_volumes = "volumes/";
    private static final String web_issues = "issues/";

    private static final String web_api_key = "?api_key=";
    private static final String web_format = "&format=json";
    private static final String web_filter_id = "&filter=id:";

    //json strings
    public final static String json_results = "results";
    public final static String json_issue_credits = "issue_credits";
    public final static String json_volume_credits = "volume_credits";
    public final static String json_id = "id";
    public final static String json_name = "name";
    public final static String json_volume = "volume";
    public final static String json_cover_date = "cover_date";
    public final static String json_issue_number = "issue_number";
    public final static String json_start_year = "start_year";
    public final static String json_count_of_issue_appearances = "count_of_issue_appearances";
    public final static String json_site_detail_url = "site_detail_url";

    public String key = "57e9f1dc4a6a9bdde575ca93d60621da18dcd080";
    public HashMap<Integer,Volume> set;

    public ArrayList<String> charactersToRip;
    public ArrayList<String> volumesToRip;
    public ArrayList<String> issuesToRip;

    private ListView listView;
    private VolumeAdapter adapter;

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //TODO
        //load array of collected ids


        //load map of comics
        File file = new File(getDir("data", MODE_PRIVATE), "map");
        if(file.exists()){
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                set = (HashMap<Integer,Volume>)ois.readObject();

            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        if(set == null){
            set = new HashMap<>();
            charactersToRip = new ArrayList<>();
            volumesToRip = new ArrayList<>();
            issuesToRip = new ArrayList<>();


            charactersToRip.add("4005-2349");

            new updateData().execute();
        }

        listView = (ListView) findViewById(R.id.list);

        adapter = new VolumeAdapter(set);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("ComicVine","onclick: "+ position);
                VolumeAdapter.ViewHolder holder = (VolumeAdapter.ViewHolder) view.getTag();
                adapter.extended[holder.ref]=!adapter.extended[holder.ref];
                if(adapter.extended[holder.ref]){
                    holder.issueText.setVisibility(View.GONE);
                    holder.comicList.setVisibility(View.VISIBLE);
                }
                else{
                    holder.issueText.setVisibility(View.VISIBLE);
                    holder.comicList.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.main, menu);
        //return super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu, menu);
        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        //SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.d("ComicVine","menu clicked");
        /*switch(id) {
            case R.id.refresh:
                //TODO add refresh state method to dd_service
                setState(controller.getState());
                break;
            case R.id.toggleChatHead:
                Intent toggleChatHead = new Intent();
                toggleChatHead.setAction("com.symbol.rxloggerutility.intent.action.CHATHEAD_TOGGLE");
                toggleChatHead.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                this.getApplicationContext().sendBroadcast(toggleChatHead);
                break;
            case R.id.backup:
                controller.backup();
                break;
        }*/

        return super.onOptionsItemSelected(item);
    }

    public String getJson(String web){

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            Thread.sleep(1000);
            URL url = new URL(web);
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
            //txtJson.setText(buffer.toString());
            return buffer.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(InterruptedException e) {
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

    private class updateData extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... strings) {
            //get charater issues
            characerProcessor();

            //get volume issues
            volumeProcessor();

            //get issue data
            issueProcessor();

            //get volume extra info
            volumeDataProcessor();

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //TODO save data
            try {
                File file = new File(getDir("data", MODE_PRIVATE), "map");
                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                outputStream.writeObject(set);
                outputStream.flush();
                outputStream.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }

            //TODO update Ui
            //mAdapter.updateData(set);
            //mAdapter.notifyDataSetChanged();

        }
    }

    public void characerProcessor(){
        for(int z =0;z<charactersToRip.size();z++){
            try {
                String url = web_base + web_charater + charactersToRip.get(z) + web_api_key + key + web_format;
                String results = getJson(url);

                JSONObject base = new JSONObject(results);
                JSONObject root = base.getJSONObject(json_results);
                JSONArray issues = root.getJSONArray(json_issue_credits);
                JSONArray volumes = root.getJSONArray(json_volume_credits);

                //build list of volumes that character is in
                for(int x = 0; x < volumes.length(); x++) {
                    JSONObject temp = volumes.getJSONObject(x);
                    int id = temp.getInt(json_id);
                    if(!set.containsKey(id)) {
                        set.put(id, new Volume(id + "", temp.getString(json_name),temp.getString(json_site_detail_url)));
                    }
                    //need to get year after
                }

                //build list of issues to rip data from
                for(int x=0;x<issues.length();x++){
                    issuesToRip.add(issues.getJSONObject(x).get(json_id)+"");
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        charactersToRip.clear();
    }

    public void volumeProcessor(){
        for(int z =0;z<volumesToRip.size();z++){
            try {
                String url = web_base + web_volume + volumesToRip.get(z) + web_api_key + key + web_format;
                String results = getJson(url);

                JSONObject base = new JSONObject(results);
                JSONObject root = base.getJSONObject(json_results);
                JSONArray issues = root.getJSONArray(json_issue_credits);
                JSONArray volumes = root.getJSONArray(json_volume_credits);

                //build list of volumes that character is in
                for(int x = 0; x < volumes.length(); x++) {
                    JSONObject temp = volumes.getJSONObject(x);
                    int id = temp.getInt(json_id);
                    if(!set.containsKey(id)) {
                        set.put(id, new Volume(id + "", temp.getString(json_name),temp.getString(json_site_detail_url)));
                    }
                    //need to get year after
                }

                //build list of issues to rip data from
                for(int x=0;x<issues.length();x++){
                    issuesToRip.add(issues.getJSONObject(x).get(json_id)+"");
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        volumesToRip.clear();
    }

    public void issueProcessor(){
        String[] urls =  new String[issuesToRip.size()/100+1];

        for(int x=0;x<issuesToRip.size();x=x+100) {
            StringBuilder sb = new StringBuilder(web_base + web_issues + web_api_key+key+web_format+web_filter_id);
            for(int y = x; y < x + 100 && y < issuesToRip.size(); y++) {
                sb.append(issuesToRip.get(y));
                sb.append("|");
            }
            sb.deleteCharAt(sb.length()-1);
            urls[x/100] = sb.toString();
        }

        for(int x=0;x<urls.length;x++) {
            try {
                Log.d("ComicVine", "url is: " + urls[x]);
                String results = getJson(urls[x]);
                JSONObject base = new JSONObject(results);
                JSONArray root = base.getJSONArray(json_results);
                for(int y=0;y<root.length();y++){
                    JSONObject issue = root.getJSONObject(y);
                    //get volume id
                    int volumeId = issue.getJSONObject(json_volume).getInt(json_id);
                    Volume vol;
                    if(set.containsKey(volumeId)){
                        vol = set.get(volumeId);
                    }
                    else{
                        JSONObject volume = issue.getJSONObject(json_volume);
                        vol = new Volume(volumeId + "", volume.getString(json_name), volume.getString(json_site_detail_url));
                        set.put(volumeId, vol);
                    }
                    int id = issue.getInt(json_id);
                    if(!vol.list.containsKey(id)) {
                        vol.list.put(id, new Comic(issue.getInt(json_id) + "", issue.getString(json_name), issue.getString(json_cover_date), issue.getString(json_site_detail_url), issue.getString(json_issue_number)));
                    }
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }

        issuesToRip.clear();
    }

    public void volumeDataProcessor(){
        String[] urls = new String[set.size()/100+1];
        int counter =0;
        int index=0;
        StringBuilder sb = new StringBuilder(web_base + web_volumes + web_api_key+key+web_format+web_filter_id);
        for(Iterator keys = set.keySet().iterator(); keys.hasNext();){
            sb.append(keys.next());
            sb.append("|");
            counter++;
            if(counter>=100){
                counter=0;
                sb.deleteCharAt(sb.length()-1);
                urls[index]=sb.toString();
                sb = new StringBuilder(web_base + web_volumes + web_api_key+key+web_format+web_filter_id);
                index++;
            }
        }
        sb.deleteCharAt(sb.length()-1);
        urls[index]=sb.toString();

        for(int x=0;x<urls.length;x++) {
            try {
                Log.d("ComicVine", "url is: " + urls[x]);
                String results = getJson(urls[x]);
                JSONObject base = new JSONObject(results);
                JSONArray root = base.getJSONArray(json_results);
                for(int y=0;y<root.length();y++){
                    JSONObject volume = root.getJSONObject(y);
                    //get volume id
                    int volumeId = volume.getInt(json_id);
                    Volume vol = set.get(volumeId);
                    vol.date = volume.getString(json_start_year);
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }
    }
}
