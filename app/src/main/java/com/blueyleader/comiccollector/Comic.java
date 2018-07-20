package com.blueyleader.comiccollector;

import java.io.Serializable;

public class Comic implements Serializable {
    public String name;
    public int id;
    public String date;
    public String url;
    public String issue;
    public String image;

    public Boolean collected = false;

    public Comic(int id, String name, String date, String url, String issue, String image){
        this.id = id;
        this.name = name;
        this.date = date;
        this.url = url;
        this.issue = issue;
        this.image = image;
    }
}
