package com.blueyleader.comicvine;

import java.io.Serializable;

public class Comic implements Serializable {
    public String name;
    public String id;
    public String date;
    public String url;
    public String issue;

    public Comic(String id, String name, String date, String url, String issue){
        this.id = id;
        this.name = name;
        this.date = date;
        this.url = url;
        this.issue = issue;
    }
}
