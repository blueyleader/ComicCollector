package com.blueyleader.comicvine;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SearchSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_search);
        setHasOptionsMenu(true);
    }
}
