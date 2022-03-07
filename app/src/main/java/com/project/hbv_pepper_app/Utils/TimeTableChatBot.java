package com.project.hbv_pepper_app.Utils;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.util.PhraseSetUtil;

import java.util.Calendar;

//import com.project.hbv_pepper_app.Other.HBV_TimeTable.Lectures;

public class TimeTableChatBot {
    /* Class to interact with the focused User
       ├ Asks for required values
       └ Returns (specific) timetables
     */

    private final static String TAG = "TimeTableChatBotClass";

    private QiContext qiContext;
    private String chosenSemester;
    private String chosenCourse;
    private String chosenLecture;
    private String chosenKW = String.valueOf(getWeek());

    //ArrayList<WeekDay> week;
    private PhraseSet semesterPhrases;
    private PhraseSet coursesPhrases;
    private PhraseSet lecturesPhrases;

    // TODO: Arrays als json in /res hinzufügen
    private String[] allSemester = { "1", "2", "3", "4", "5", "6", "7" };
    private String[] allCourses = { "WI", "Wirtschaftsinformatik" };
    private String[] allLectures = { // das hier als json für jeden Kurs - / Semester Kombination
            "Programmierung", "BWL", "Graphen und Endliche Automaten", "Diskrete Mathematik", "SWE", // 1, Semester
            "Vernetze Systeme", "Datenbanken I", "Software Engineering III", "Theoretische Informatik", "Standardsoftware", "Controlling", // 3. Semester
            "ERP", "Business Intelligence", "IT-Recht", "Datenbanken II", "Big Data", "Compilerbau", "IT-Sicherheit", "ABAB", "Datenbanken II", "Marketing", "Parallellprogrammierung", "Grundlagen Systemintegration"// 5. Semester
    };

    /* ----- Exit handling ----- ----- ----- ----- */

    private boolean done;
    private boolean cancelExists;
    private PhraseSet cancelPhrases;

    private String[] cancelStrings = { "abbrechen", "abbruch", "hör auf", "zurück", "hä" };

    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */

    public TimeTableChatBot(QiContext qiContext){
        this.qiContext = qiContext;
        this.semesterPhrases = PhraseSetBuilder.with(qiContext).withTexts(allSemester).build();
        this.coursesPhrases = PhraseSetBuilder.with(qiContext).withTexts(allCourses).build();
        this.lecturesPhrases = PhraseSetBuilder.with(qiContext).withTexts(allLectures).build();
        this.cancelPhrases = PhraseSetBuilder.with(qiContext).withTexts(cancelStrings).build();
    }

    public void start(){
        while(this.chosenSemester == null || this.chosenCourse == null || !done){
            Log.i("!","_");
            checkInput();
            if(this.cancelExists) {
                HelperCollection.Say(this.qiContext, "Okay, ich breche den Vorgang ab.");
                return;
            }
        }
    }

    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */

    private void checkInput(){
        if (this.chosenCourse != null && this.chosenSemester != null) {
            try {
                boolean lectureFound = false;
                Log.i(TAG, "---> get Timetables");
                if(chosenLecture != null && !lectureFound) HelperCollection.Say(this.qiContext, this.chosenLecture + " habe ich für diese Studiengang / Semesterkombination nicht gefunden.");
                this.done = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Listen listen = ListenBuilder.with(qiContext)
                    .withPhraseSets(this.semesterPhrases, this.coursesPhrases, this.lecturesPhrases, this.cancelPhrases)
                    .build();
            ListenResult listenResult = listen.run();
            PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

            if (PhraseSetUtil.equals(matchedPhraseSet, this.cancelPhrases)) {
                this.cancelExists = true;
                return;
            }
            if (PhraseSetUtil.equals(matchedPhraseSet, this.lecturesPhrases)) {
                this.chosenLecture = listenResult.getHeardPhrase().getText();
                Log.i(TAG, "Heard Lecture -> " + this.chosenLecture);
            }

            if (PhraseSetUtil.equals(matchedPhraseSet, this.semesterPhrases)) {
                this.chosenSemester = listenResult.getHeardPhrase().getText();
                Log.i(TAG, "Heard Semester -> " + this.chosenSemester);
            }

            if (PhraseSetUtil.equals(matchedPhraseSet, this.coursesPhrases)) {
                this.chosenCourse = listenResult.getHeardPhrase().getText();
                Log.i(TAG, "Heard Course -> " + this.chosenCourse);
            }

            if (this.chosenSemester == null) HelperCollection.Say(this.qiContext, "Welches Semester?");
            else if (this.chosenCourse == null) HelperCollection.Say(this.qiContext, "Welcher Studiengang?");
        }
    }

    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */

    private static int getWeek() {
        return Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
    }
}


/* ----- ----- EOF ----- ----- ----- ----- ----- ----- ----- ----- */
