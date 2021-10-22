package com.example.hsb_pepper;

import android.nfc.Tag;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;
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
    private String chosenWeekday = "MO";

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

    private PhraseSet semester;
    private PhraseSet courses;
    private PhraseSet lectures;

    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */

    public TimeTableChatBot(QiContext qiContext){
        this.qiContext = qiContext;
        this.semester = PhraseSetBuilder.with(qiContext).withTexts(allSemester).build();
        this.courses = PhraseSetBuilder.with(qiContext).withTexts(allCourses).build();
        this.lectures = PhraseSetBuilder.with(qiContext).withTexts(allLectures).build();
    }

    public void start(){
        while(chosenSemester == null || chosenCourse == null || chosenLecture == null){
            Log.i(TAG, "--");
            checkInput();
        }
    }

    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */

    private void checkInput(){
        Listen listen = ListenBuilder.with(qiContext).withPhraseSets(semester,courses,lectures).build();
        ListenResult listenResult = listen.run();

        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        if (PhraseSetUtil.equals(matchedPhraseSet, semester)) {
            chosenSemester = listenResult.getHeardPhrase().getText();
            Log.i(TAG, "Heard Semester -> " + chosenSemester);
        }

        if (PhraseSetUtil.equals(matchedPhraseSet, courses)) {
            chosenCourse = listenResult.getHeardPhrase().getText();
            Log.i(TAG, "Heard Course -> " + chosenCourse);
        }

        if (PhraseSetUtil.equals(matchedPhraseSet, lectures)) {
            chosenLecture = listenResult.getHeardPhrase().getText();
            Log.i(TAG, "Heard lecture -> " + chosenLecture);
        }

        if (chosenSemester != null && chosenCourse != null && chosenLecture != null){
            try {
                ArrayList<WeekDay> week = this.getTimeTable(chosenCourse,chosenSemester,chosenKW);
                //WeekDay day = week.get(Arrays.asList(weekdays).indexOf(chosenWeekday));
                for(int i = 0; i < week.size(); i++){
                    WeekDay day = week.get(i);
                    for(int j = 0; j < day.getCourses().size(); j++){
                        Course course = day.getCourses().get(j);
                        if(course.getName().contains(chosenLecture)){
                            Log.i(TAG, day.getName() + " " + course.getName() + " " + course.getRoom());
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (chosenSemester == null){
            Say("Welches Semester?");
        } else if (chosenLecture == null){
            Say("Welcher Kurs?");
        } else if (chosenCourse == null){
            Say("Welcher Studiengang?");
        }
    }

    private void Say(String message){
        SayBuilder.with(qiContext).withPhrase(new Phrase(message)).build().run();
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
            Course course = new Course(
                    content[3],
                    content[1],
                    content[2],
                    content[4],
                    content[5]
            );

            if(!weekDays.get(weekDays.size() - 1).getName().equals(content[0])){ // neuer Tag
                weekDays.add(new WeekDay(content[0], course));
            } else { // selber Tag, anderer Eintrag
                weekDays.get(weekDays.size() - 1).addCourse(course);
            }
        }
        return weekDays;
    }
}
