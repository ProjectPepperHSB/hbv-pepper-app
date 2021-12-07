package com.project.hbv_pepper_app.Other.HBV_TimeTable;
import com.project.hbv_pepper_app.Utils.HelperCollection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.google.gson.Gson;
import android.util.Log;

public class TimeTableHandler {
    private String course, semester, kw;
    final private String URL = "https://informatik.hs-bremerhaven.de/docker-hbv-kms-http/timetable";
    private String timetable_html;

    public TimeTableHandler(String course, String semester){
        this.course = course;
        this.semester = semester;
        this.kw = String.valueOf(this.getWeek());
        this.checkSemester();
    }

    public TimeTableHandler(String course, String semester, String kw){
        this.course = course;
        this.semester = semester;
        this.kw = kw;
        this.checkSemester();
    }

    private void checkSemester(){
        // sets the value of semester to a number as string
        final List<String> semester_list = Arrays.asList("eins", "zwei", "drei", "vier", "fünf", "sechs", "sieben", "acht");
        if (semester_list.contains(this.semester)) {
            for(int i = 1; i < semester_list.size() + 1; i++) {
                if (semester_list.get(i-1).equals(this.semester)) {
                    this.semester = String.valueOf(i);
                }
            }
        }
    }

    public TimeTable getTimeTable() {
        String url = String.format("%s?course=%s&semester=%s&kw=%s", this.URL, this.course, this.semester, this.kw);
        this.timetable_html = HelperCollection.getUrlContents(String.format(url+"&htmlOnly=true"));
        String response_json_str  = HelperCollection.getUrlContents(url);
        return new Gson().fromJson(response_json_str, TimeTable.class);
    }

    private static int getWeek() {
        return Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
    }
    public String getHtmlPage() { return this.timetable_html; }
}

/* ----- ----- EOF ----- ----- ----- ----- ----- ----- ----- ----- */
