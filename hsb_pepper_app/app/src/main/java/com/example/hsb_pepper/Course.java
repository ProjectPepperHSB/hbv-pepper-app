package com.example.hsb_pepper;

import java.util.ArrayList;

public class Course {
    private String name;
    private ArrayList<String> begins = new ArrayList<String>();
    private ArrayList<String> ends = new ArrayList<String>();
    private String prof;
    private String room;

    public Course(String name, String begins, String ends, String prof, String room) {
        this.name = name;
        this.begins.add(begins);
        this.ends.add(ends);
        this.prof = prof;
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public String getProf(){
        return prof;
    }

    public String getRoom(){
        return room;
    }

    public ArrayList<String> getBegins() {
        return begins;
    }

    public ArrayList<String> getEnds() {
        return ends;
    }

    public void setBegins(ArrayList<String> begins) {
        this.begins = begins;
    }

    public void setEnds(ArrayList<String> ends) {
        this.ends = ends;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProf(String prof) {
        this.prof = prof;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}
