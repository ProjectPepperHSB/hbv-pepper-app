package com.project.hbv_pepper_app.Utils;

// Unused class
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

    public void Person(){}

    // endregion implements INIT
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implements GET_SET

    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    public boolean getGender() {
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