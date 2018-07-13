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
    TypeHolder type;

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

    public PreferenceCategoryAdd(Context context, String name, TypeHolder type) {
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
                TypeHolder type = (TypeHolder) v.getTag();
                int val = (int)(Math.random()*100);
                RipObject rp = new RipObject(val+"",val+"",val);
                type.map.put(val,rp);
                type.view.addPreference(new RipPreference(type.view.getContext(), rp));
                Log.d("ComicVine","tag was " + rp.name);
            }
        });
        return view;
    }
}
