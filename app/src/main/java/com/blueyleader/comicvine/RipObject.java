package com.blueyleader.comicvine;

import java.io.Serializable;

public class RipObject implements Serializable {
    public String name;
    public String date;
    public String id;
    int type;

    public RipObject(String name, String id, String date, int type){
        this.name = name;
        this.id = id;
        this.date = date;
        this.type = type;
    }
}
