package com.blueyleader.comiccollector;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

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
import java.util.HashSet;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    public static MainActivity self;

    public static final String web_base = "https://comicvine.gamespot.com/api/";

    public static final String web_character = "character/";
    public static final String web_volume = "volume/";
    public static final String web_volumes = "volumes/";
    public static final String web_issues = "issues/";
    public static final String web_issue = "issue/";

    public static final String web_api_key = "?api_key=";
    public static final String web_format = "&format=json";
    public static final String web_filter_id = "&filter=id:";

    public static final String web_character_ref= "4005-";
    public static final String web_volume_ref= "4050-";
    public static final String web_issue_ref= "4000-";

    //json strings
    public final static String json_results = "results";
    public final static String json_issue_credits = "issue_credits";
    public final static String json_volume_credits = "volume_credits";
    public final static String json_issues = "issues";
    public final static String json_id = "id";
    public final static String json_name = "name";
    public final static String json_volume = "volume";
    public final static String json_cover_date = "cover_date";
    public final static String json_issue_number = "issue_number";
    public final static String json_start_year = "start_year";
    public final static String json_site_detail_url = "site_detail_url";

    public HashMap<Integer,Volume> set;
    public HashSet<Integer> collected;

    public ArrayList<String> charactersToRip;
    public ArrayList<String> volumesToRip;
    public ArrayList<String> issuesToRip;

    public ListView listView;
    public VolumeAdapter adapter;

    public ProgressDialog loadingDialog;

    FloatingActionButton fabCol;
    FloatingActionButton fabNotCol;
    Button butCol;
    Button butNotCol;

    SearchView searchView;

    ImageView imageView;

    boolean isFABOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        self=this;

        //TODO remove and make safe
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        //TODO
        //load array of collected ids
        File file = new File(getDir("data", MODE_PRIVATE), "collected");
        if (file.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                collected = (HashSet<Integer>) ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (collected == null) {
            collected = new HashSet<>();
        }

        charactersToRip = new ArrayList<>();
        volumesToRip = new ArrayList<>();
        issuesToRip = new ArrayList<>();

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences (this);
        Boolean pull = sh.getBoolean("auto_pull_start",false);
        if(!pull) {
            //load map of comics
            file = new File(getDir("data", MODE_PRIVATE), "map");
            if (file.exists()) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                    set = (HashMap<Integer, Volume>) ois.readObject();
                    setCollected();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (set == null) {
                set = new HashMap<>();
            }
        }
        else {
            loading();
            set = new HashMap<>();
            new UpdateData().execute();
        }

        imageView = findViewById(R.id.imageView);

        listView = findViewById(R.id.list);

        adapter = new VolumeAdapter(set);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("ComicVine","onclick: "+ position);
                VolumeAdapter.ViewHolder holder = (VolumeAdapter.ViewHolder) view.getTag();
                adapter.set.get(holder.ref).extended=!adapter.set.get(holder.ref).extended;
                if(adapter.set.get(holder.ref).extended){
                    holder.issueText.setVisibility(View.GONE);
                    holder.comicList.setVisibility(View.VISIBLE);
                }
                else{
                    holder.issueText.setVisibility(View.VISIBLE);
                    holder.comicList.setVisibility(View.GONE);
                }
            }
        });
        adapter.getFilter().filter("");

        FloatingActionButton fab = findViewById(R.id.fab);
        fabCol = findViewById(R.id.collected);
        fabNotCol = findViewById(R.id.not_collected);
        butCol = findViewById(R.id.text_collected);
        butNotCol = findViewById(R.id.text_not_collected);
        //fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });

        boolean cur = sh.getBoolean("show_collected",false);
        if(cur){
            fabCol.setImageResource(R.drawable.ic_visibility_24px);
        }
        else{
            fabCol.setImageResource(R.drawable.ic_visibility_off_24px);
        }

        cur = sh.getBoolean("show_not_collected",true);
        if(cur){
            fabNotCol.setImageResource(R.drawable.ic_visibility_24px);
        }
        else{
            fabNotCol.setImageResource(R.drawable.ic_visibility_off_24px);
        }

        fabCol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences (MainActivity.self);
                boolean cur = !sh.getBoolean("show_collected",false);
                if(cur){
                    fabCol.setImageResource(R.drawable.ic_visibility_24px);
                }
                else{
                    fabCol.setImageResource(R.drawable.ic_visibility_off_24px);
                }
                sh.edit().putBoolean("show_collected",cur).apply();
                adapter.getFilter().filter(searchView.getQuery());
            }
        });

        fabNotCol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences (MainActivity.self);
                boolean cur = !sh.getBoolean("show_not_collected",true);
                if(cur){
                    fabNotCol.setImageResource(R.drawable.ic_visibility_24px);
                }
                else{
                    fabNotCol.setImageResource(R.drawable.ic_visibility_off_24px);
                }
                sh.edit().putBoolean("show_not_collected",cur).apply();
                adapter.getFilter().filter(searchView.getQuery());
                Log.d("ComicCollector","query is: " + searchView.getQuery());
            }
        });
    }

    @Override
    public void onBackPressed() {
        //TODO not sure if needed after alert dialog
        if(imageView.getVisibility()==View.VISIBLE){
            imageView.setVisibility(View.GONE);
        }
        else if(isFABOpen){
            closeFABMenu();
        }else{
            super.onBackPressed();
        }
    }

    private void showFABMenu(){
        isFABOpen=true;
        butCol.setVisibility(View.VISIBLE);
        butNotCol.setVisibility(View.VISIBLE);

        fabCol.animate().translationY(-getResources().getDimension(R.dimen.standard_75));
        butCol.animate().translationY(-getResources().getDimension(R.dimen.standard_75));
        butCol.animate().translationX(-getResources().getDimension(R.dimen.standard_55));

        fabNotCol.animate().translationY(-getResources().getDimension(R.dimen.standard_145));
        butNotCol.animate().translationY(-getResources().getDimension(R.dimen.standard_145));
        butNotCol.animate().translationX(-getResources().getDimension(R.dimen.standard_55));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        fabCol.animate().translationY(0);
        fabNotCol.animate().translationY(0);
        butCol.animate().translationY(0);
        butCol.animate().translationX(0);
        butNotCol.animate().translationY(0);
        butNotCol.animate().translationX(0);

        butCol.setVisibility(View.GONE);
        butNotCol.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        // Retrieve the SearchView and plug it into SearchManager
        MenuItem search = menu.findItem(R.id.action_search);
        searchView = (SearchView) search.getActionView();
        //searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.d("ComicVine","menu clicked");
        switch(id){
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.refresh:
                loading();
                new UpdateData().execute();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public static String getJson(String web, boolean continuous){

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            if(continuous) {
                Thread.sleep(1000);
            }
            URL url = new URL(web);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }
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


    public void characterProcessor()
    {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences (this);
        String key = sh.getString("API_KEY","");
        if(key.equals("")){
            Log.d("ComicVine","no Key");
            Toast.makeText(this,"Please add a ComicVine API key before adding objects",Toast.LENGTH_LONG).show();
            return;
        }
        for(int z=0;z<charactersToRip.size();z++){
            try {
                String url = web_base + web_character + web_character_ref + charactersToRip.get(z) + web_api_key + key + web_format;
                String results = getJson(url,true);

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
    }

    public void volumeProcessor(){
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences (this);
        String key = sh.getString("API_KEY","");
        if(key.equals("")){
            Log.d("ComicVine","no Key");
            Toast.makeText(this,"Please add a ComicVine API key before adding objects",Toast.LENGTH_LONG).show();
            return;
        }
        for(int z =0;z<volumesToRip.size();z++){
            try {
                String url = web_base + web_volume + web_volume_ref + volumesToRip.get(z) + web_api_key + key + web_format;
                String results = getJson(url,true);

                JSONObject base = new JSONObject(results);
                JSONObject root = base.getJSONObject(json_results);
                JSONArray issues = root.getJSONArray(json_issues);

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
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences (this);
        String key = sh.getString("API_KEY","");
        if(key.equals("")){
            Log.d("ComicVine","no Key");
            Toast.makeText(this,"Please add a ComicVine API key before adding objects",Toast.LENGTH_LONG).show();
            return;
        }
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
                String results = getJson(urls[x],true);
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
                        vol.list.put(id, new Comic(issue.getInt(json_id), issue.getString(json_name), issue.getString(json_cover_date), issue.getString(json_site_detail_url), issue.getString(json_issue_number)));
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
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences (this);
        String key = sh.getString("API_KEY","");
        if(key.equals("")){
            Log.d("ComicVine","no Key");
            Toast.makeText(this,"Please add a ComicVine API key before adding objects",Toast.LENGTH_LONG).show();
            return;
        }

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
                String results = getJson(urls[x],true);
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

    public void loading(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(loadingDialog!=null){
                    loadingDialog.cancel();
                }
                loadingDialog =  ProgressDialog.show(MainActivity.self, "",
                        "Loading. Please wait...", true);
            }
        });
    }

    public void setCollected(){
        for(Volume vol : set.values() ){
            for(Comic comic : vol.list.values()){
                if(collected.contains(comic.id)){
                    comic.collected=true;
                }
            }
        }
    }

    public void saveCollected() {
        try {
            File file = new File(MainActivity.self.getDir("data", MODE_PRIVATE), "collected");
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(collected);
            outputStream.flush();
            outputStream.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
