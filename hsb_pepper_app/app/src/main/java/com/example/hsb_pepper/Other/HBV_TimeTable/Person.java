package com.example.hsb_pepper.Other.HBV_TimeTable;

public class Person {
    /* Class to store data about a person
        ├ setting
        └ getting
     */

    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implements VARIABLES

    private String emotion;
    private int age;
    private boolean gender; // 0 = w && 1 = m

    //endregion implements VARIABLES
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implements INIT

    public void Person(){

    }

    // endregion implements INIT
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implements GET_SET

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    // endregion implements GET_SET
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
}

/* ----- ----- EOF ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */