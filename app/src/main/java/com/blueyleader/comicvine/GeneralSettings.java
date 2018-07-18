package com.blueyleader.comicvine;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class GeneralSettings extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);
    }
}
