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

public class TimeTableChatBot {
    /* Class to interact with the focused User
       ├ Asks for required values
       └ Returns (specific) timetables
     */

    private String TAG = "TimeTableChatBotClass";



    private QiContext qiContext;
    private String chosenSemester;
    private String chosenCourse;
    private String chosenLecture;
    private String chosenKW = "42";
    //private String chosenWeekday = "MO";

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
    private String[] allLectures = { // das hier als json für jede Kurs - / Semester Kombination
            "Programmierung", "BWL"
    };
    private String[] weekdays = {
            "MO", "DI", "MI", "DO", "FR" // , "SA"
    };

    /* ----- Exit handling ----- ----- ----- ----- */

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
        while(chosenSemester == null || chosenCourse == null || chosenLecture == null){
            checkInput();
            if(cancelExists)
                HelperCollection.Say(qiContext, "Okay, ich breche den Vorgang ab.");
                return;
        }
    }

    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */

    private void checkInput(){
        Listen listen = ListenBuilder.with(qiContext).withPhraseSets(semesterPhrases, coursesPhrases, lecturesPhrases,cancelPhrases).build();
        ListenResult listenResult = listen.run();

        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        if (PhraseSetUtil.equals(matchedPhraseSet, cancelPhrases)) {
            this.cancelExists = true;
            return;
        }

        if (PhraseSetUtil.equals(matchedPhraseSet, semesterPhrases)) {
            chosenSemester = listenResult.getHeardPhrase().getText();
            Log.i(TAG, "Heard Semester -> " + chosenSemester);
        }

        if (PhraseSetUtil.equals(matchedPhraseSet, coursesPhrases)) {
            chosenCourse = listenResult.getHeardPhrase().getText();
            Log.i(TAG, "Heard Course -> " + chosenCourse);
        }

        if (PhraseSetUtil.equals(matchedPhraseSet, lecturesPhrases)) {
            chosenLecture = listenResult.getHeardPhrase().getText();
            Log.i(TAG, "Heard Lecture -> " + chosenLecture);
        }

        if (chosenSemester != null && chosenCourse != null && chosenLecture != null){
            try {
                ArrayList<WeekDay> week = this.getTimeTable(chosenCourse,chosenSemester,chosenKW);
                //WeekDay day = week.get(Arrays.asList(weekdays).indexOf(chosenWeekday));
                for(int i = 0; i < week.size(); i++){
                    WeekDay day = week.get(i);
                    for(int j = 0; j < day.getLectures().size(); j++){
                        Lectures lectures = day.getLectures().get(j);
                        if(lectures.getName().contains(chosenLecture)){
                            Log.i(TAG, day.getName() + " " + lectures.getName() + " " + lectures.getRoom());
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (chosenSemester == null){
            HelperCollection.Say(qiContext, "Welches Semester?");
        } else if (chosenLecture == null){
            HelperCollection.Say(qiContext, "Welcher Kurs?");
        } else if (chosenCourse == null){
            HelperCollection.Say(qiContext, "Welcher Studiengang?");
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
            } else { // selber Tag, anderer Eintrag
                weekDays.get(weekDays.size() - 1).addCourse(lectures);
            }
        }
        return weekDays;
    }
}


/* ----- ----- EOF ----- ----- ----- ----- ----- ----- ----- ----- */
