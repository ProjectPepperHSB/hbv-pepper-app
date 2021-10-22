package com.example.hsb_pepper;

import java.util.ArrayList;

public class Course {
    private String name;
    private String begin;
    private String end;
    private String prof;
    private String room;

    public Course(String name, String begin, String end, String prof, String room) {
        this.name = name;
        this.begin = begin;
        this.end = end;
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

    public String getBegin() {
        return begin;
    }

    public String getEnd() {
        return end;
    }

    public void setBegin(String begins) {
        this.begin = begin;
    }

    public void setEnd(String ends) {
        this.end = end;
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
