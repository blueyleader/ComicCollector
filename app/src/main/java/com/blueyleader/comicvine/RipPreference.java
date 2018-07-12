package com.blueyleader.comicvine;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RipPreference extends Preference {

    public RipObject rp;

    public RipPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public RipPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RipPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RipPreference(Context context, RipObject rp) {
        super(context);
        this.rp = rp;
    }

    @Override
    protected View onCreateView(ViewGroup parent )
    {
        super.onCreateView(parent);
        LayoutInflater li = (LayoutInflater)getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = li.inflate( R.layout.rip_preference, parent, false);
        TextView text = view.findViewById(R.id.rip_name);
        text.setText(rp.name);
        return view;
    }
}
