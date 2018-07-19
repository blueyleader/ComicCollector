package com.blueyleader.comiccollector;

import java.io.Serializable;
import java.util.HashMap;

public class Volume implements Serializable {
    public HashMap<Integer,Comic> list;
    public String name;
    public String id;
    public String date="";
    public String url;

    public Volume(String id, String name, String url){
        list = new HashMap<>();
        this.id = id;
        this.name = name;
        this.url = url;

    }
}
