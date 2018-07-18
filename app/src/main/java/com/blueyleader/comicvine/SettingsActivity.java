package com.blueyleader.comicvine;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {
    public static SettingsActivity self;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        setupActionBar();
    }

    @Override
    public boolean onIsMultiPane() {
        return (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralSettings.class.getName().equals(fragmentName)
                || CollectionFragment.class.getName().equals(fragmentName);
    }

    public void loading(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(MainActivity.self.loadingDialog!=null){
                    MainActivity.self.loadingDialog.cancel();
                }
                MainActivity.self.loadingDialog =  ProgressDialog.show(SettingsActivity.self, "",
                        "Loading. Please wait...", true);
            }
        });
    }

}
