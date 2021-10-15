package com.example.hsb_pepper;

import java.util.ArrayList;
import java.util.Arrays;

public class WeekDay {
    private String name;
    private ArrayList<Course> courses = new ArrayList<Course>(Arrays.asList(new Course[]{}));

    public WeekDay(String name){
        this.name = name;
    }

    public WeekDay(String name, Course course){
        this.name = name;
        this.courses.add(course);
    }
    public String getName(){
        return name;
    }
    public ArrayList<Course> getCourses(){
        return courses;
    }
    public void addCourse(Course course){
        courses.add(course);
    }
}
