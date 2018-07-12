package com.blueyleader.comicvine;

import java.io.Serializable;

public class RipObject implements Serializable {
    public String name;
    public String id;
    int type;

    public RipObject(String name, String id, int type){
        this.name = name;
        this.id = id;
        this.type = type;
    }
}
