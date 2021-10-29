package com.example.hsb_pepper;

import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.util.PhraseSetUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

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

    ArrayList<WeekDay> week;
    private PhraseSet semesterPhrases;
    private PhraseSet coursesPhrases;
    private PhraseSet lecturesPhrases;

    // TODO: Arrays als json in /res hinzufügen
    private String[] allSemester = {
            "1", "2", "3", "4", "5", "6", "7"
    };
    private String[] allCourses = {
            "WI", "Wirtschaftsinformatik"
    };
    private String[] allLectures = { // das hier als json für jeden Kurs - / Semester Kombination
            "Programmierung", "BWL", "Graphen und Endliche Automaten", "Diskrete Mathematik", "SWE", // 1, Semester
            "Vernetze Systeme", "Datenbanken I", "Software Engineering III", "Theoretische Informatik", "Standardsoftware", "Controlling", // 3. Semester
            "ERP", "Business Intelligence", "IT-Recht", "Datenbanken II", "Big Data", "Compilerbau", "IT-Sicherheit", "ABAB", "Datenbanken II", "Marketing", "Parallellprogrammierung", "Grundlagen Systemintegration"// 5. Semester
    };

    /* ----- Exit handling ----- ----- ----- ----- */

    private boolean done;
    private boolean cancelExists;
    private PhraseSet cancelPhrases;

    private String[] cancelStrings = {
            "abbrechen", "abbruch", "hör auf", "zurück", "hä"
    };

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
                week = this.getTimeTable(chosenCourse, chosenSemester, chosenKW);
                for (int i = 0; i < week.size(); i++) {
                    WeekDay day = week.get(i);
                    for (int j = 0; j < day.getLectures().size(); j++) {
                        Lectures lectures = day.getLectures().get(j);
                        if (this.chosenLecture != null && lectures.getName().contains(chosenLecture)) {
                            String msg = this.chosenLecture + " findet am " + day.getName() + " im Zeitraum von "
                                    + lectures.getBegin() + " bis " + lectures.getEnd() + " Uhr im Raum "
                                    + lectures.getRoom() + " statt.";
                            HelperCollection.Say(this.qiContext, msg);
                            lectureFound = true;
                        }
                        // DISPLAY ALL COURSES OF THIS WEEK ON THE TABLET
                        Log.i(TAG, day.getName() + ", Kurs: " + lectures.getName() + " Raum: " + lectures.getRoom() + "Beginn: " + lectures.getBegin() + "Uhr Ende: " + lectures.getEnd() + "Uhr.");
                    }
                }
                if(chosenLecture != null && !lectureFound){
                    HelperCollection.Say(this.qiContext, this.chosenLecture + " habe ich für diese Studiengang / Semesterkombination nicht gefunden.");
                }
                this.done = true;
            } catch (IOException e) {
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

            if (this.chosenSemester == null) {
                HelperCollection.Say(this.qiContext, "Welches Semester?");
            } else if (this.chosenCourse == null) {
                HelperCollection.Say(this.qiContext, "Welcher Studiengang?");
            }
        }
    }
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */

    private ArrayList<WeekDay> getTimeTable(String _course, String _semester, String _kw) throws IOException {
        String course_str = _course + "_B" + _semester + "_" + _kw;
        String url_str = "https://informatik.hs-bremerhaven.de/docker-hbv-kms-web/timetablesfb2/" + course_str + ".csv";
        String response_str  = HelperCollection.getUrlContents(url_str);

        ArrayList<WeekDay> weekDays = new ArrayList<WeekDay>(Arrays.asList(new WeekDay[]{new WeekDay("Mo")}));
        BufferedReader reader = new BufferedReader(new StringReader(response_str));
        String line;

        int line_idx = 0;
        while ((line = reader.readLine()) != null) {
            line_idx++;
            if(line_idx == 1)
                continue;

            String[] content =  line.split(";");
            Lectures lectures = new Lectures(
                    content[3],
                    content[1],
                    content[2],
                    content[4],
                    content[5]
            );

            if(!weekDays.get(weekDays.size() - 1).getName().equals(content[0])){ // neuer Tag
                weekDays.add(new WeekDay(content[0], lectures));
            } else { // selber Tag, anderer Kurs / Eintrag
                weekDays.get(weekDays.size() - 1).addCourse(lectures);
            }
        }
        return weekDays;
    }

    private static int getWeek() {
        return Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
    }
}


/* ----- ----- EOF ----- ----- ----- ----- ----- ----- ----- ----- */
