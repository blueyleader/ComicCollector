package com.blueyleader.comicvine;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setTitle("please add").setMessage("Enter id of object to collect");

                builder.setView(R.layout.add_rip_dialog);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button

                        int text = Integer.parseInt(((EditText)((AlertDialog)dialog).getCurrentFocus().findViewById(R.id.id_edit)).getText().toString());

                        Log.d("ComicVine","Text was " + text);

                        TypeHolder type = (TypeHolder) ((AlertDialog)dialog).getCurrentFocus().findViewById(R.id.id_edit).getTag();
                        RipObject rp = new RipObject(text+"",text+"",text+"",text);
                        type.map.put(text,rp);
                        type.view.addPreference(new RipPreference(type.view.getContext(), rp));
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();

                dialog.show();
                dialog.getCurrentFocus().findViewById(R.id.id_edit).setTag(type);
            }
        });

        return view;
    }
}
