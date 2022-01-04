package com.project.hbv_pepper_app.Other.HBV_TimeTable;


public class Lectures extends TimeTable{
    /* --- Class to store information about lectures --- */

    private String begin;
    private String end;
    private String course;
    private String prof;

    public String toString() {
        return String.format("begin: %s, end: %s, course: %s, prof: %s", this.begin, this.end, this.course, this.prof);
    }

    public String getCourse() {
        return course;
    }

    public String getProf(){
        return prof;
    }

    public String getBegin() {
        return begin;
    }

    public String getEnd() {
        return end;
    }
}
