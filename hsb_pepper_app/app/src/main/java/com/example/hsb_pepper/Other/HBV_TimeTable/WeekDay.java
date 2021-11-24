package com.example.hsb_pepper.Other.HBV_TimeTable;

import java.util.ArrayList;
import java.util.Arrays;

public class WeekDay {
    /* Class to store multiple lectures in one weekday
    */

    private String name;
    private ArrayList<Lectures> lectures = new ArrayList<Lectures>(Arrays.asList(new Lectures[]{}));

    public WeekDay(String name){
        this.name = name;
    }

    public WeekDay(String name, Lectures lectures){
        this.name = name;
        this.lectures.add(lectures);
    }

    public String getName(){
        return name;
    }
    public ArrayList<Lectures> getLectures(){
        return lectures;
    }
    public void addCourse(Lectures lectures){
        this.lectures.add(lectures);
    }
}

/* ----- ----- EOF ----- ----- ----- ----- ----- ----- ----- ----- */
