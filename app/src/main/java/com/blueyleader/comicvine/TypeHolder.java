package com.blueyleader.comicvine;

import android.content.Context;

import java.util.HashMap;

public class TypeHolder {
    HashMap<Integer,RipObject> map;
    PreferenceCategoryAdd view;
    Context context;

    public TypeHolder( HashMap<Integer,RipObject> map,Context context){
        this.map = map;
        this.view = view;
        this.context = context;
    }
}
