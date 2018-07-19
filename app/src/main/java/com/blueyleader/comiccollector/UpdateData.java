package com.blueyleader.comiccollector;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class UpdateData extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... strings) {
        MainActivity.self.set = new HashMap<>();

        HashMap<Integer,RipObject> rip = null;
        File file = new File(MainActivity.self.getDir("data", MODE_PRIVATE), "map_characters");
        if(file.exists()){
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                rip = (HashMap<Integer,RipObject>)ois.readObject();
                ois.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        if(rip != null){
            for(RipObject rp : rip.values()) {
                MainActivity.self.charactersToRip.add(rp.id);
            }
        }

        rip = null;
        file = new File(MainActivity.self.getDir("data", MODE_PRIVATE), "map_volumes");
        if(file.exists()){
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                rip = (HashMap<Integer,RipObject>)ois.readObject();
                ois.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        if(rip != null){
            for(RipObject rp : rip.values()) {
                MainActivity.self.volumesToRip.add(rp.id);
            }
        }

        rip = null;
        file = new File(MainActivity.self.getDir("data", MODE_PRIVATE), "map_issues");
        if(file.exists()){
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                rip = (HashMap<Integer,RipObject>)ois.readObject();
                ois.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        if(rip != null){
            for(RipObject rp : rip.values()) {
                MainActivity.self.issuesToRip.add(rp.id);
            }
        }

        //get character issues
        MainActivity.self.characterProcessor();

        //get volume issues
        MainActivity.self.volumeProcessor();

        //get issue data
        MainActivity.self.issueProcessor();

        //get volume extra info
        MainActivity.self.volumeDataProcessor();

        MainActivity.self.setCollected();

        MainActivity.self.loadingDialog.cancel();
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            File file = new File(MainActivity.self.getDir("data", MODE_PRIVATE), "map");
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(MainActivity.self.set);
            outputStream.flush();
            outputStream.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        MainActivity.self.adapter.updateData(MainActivity.self.set);
        MainActivity.self.adapter.notifyDataSetChanged();

    }
}