package com.blueyleader.comicvine;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class PreferenceCategoryAdd extends PreferenceCategory {
    String name;
    int type;

    public PreferenceCategoryAdd(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PreferenceCategoryAdd(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PreferenceCategoryAdd(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreferenceCategoryAdd(Context context) {
        super(context);
    }

    public PreferenceCategoryAdd(Context context, String name,int type) {
        super(context);
        this.name = name;
        this.type = type;
    }

    protected View onCreateView(ViewGroup parent )
    {
        super.onCreateView(parent);
        Log.d("ComicVine","RipPreference onCreateView");
        LayoutInflater li = (LayoutInflater)getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = li.inflate( R.layout.preference_category_button, parent, false);
        TextView text = view.findViewById(R.id.catagory_title);
        text.setText(name);
        ImageButton ib =  view.findViewById(R.id.add_button);
        view.setTag(type);
        ib.setTag(type);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = (int) v.getTag();
                Log.d("ComicVine","tag was " + val);
            }
        });
        return view;
    }
}
