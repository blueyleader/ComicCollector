package com.blueyleader.comicvine;

import java.util.HashMap;

public class Volume {
    public HashMap<Integer,Comic> list;
    public String name;
    public String id;
    public String date;

    public Volume(String id, String name){
        list = new HashMap<>();
        this.id = id;
        this.name = name;
    }
}
