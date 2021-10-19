package com.example.hsb_pepper;

import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.util.PhraseSetUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {


    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implements VARIABLES
    private static final String TAG = "MainActivity";
    private static final boolean DEBUG_MODE = true;

    private QiChatbot qiChatbot;
    private Chat chat;
    private Bookmark proposalBookmark;
    private Map<String, Bookmark> bookmarks;

    private Animate animate;

    // endregion implements VARIABLES
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implements EVENTS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
        Log.i(TAG,"test");
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusLost() {
        Log.i(TAG,"Focus lost");
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Remove on started listeners from the Chat action.
        if (chat != null) {
            chat.removeAllOnStartedListeners();
        }
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.i(TAG,"### Focus gained ###");
        SayBuilder.with(qiContext).withText("Hallo ich bin Robotn").build().run();
/*
        try {
            createChatBot(qiContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        //Hear for keyword
        TestMensa(qiContext);
    }

    public void TestMensa(QiContext qiContext){

        PhraseSet phraseSetYes = PhraseSetBuilder.with(qiContext).withTexts("Mensa", "Mensaplan", "Essen", "Cafetaria").build();

        Listen listen = ListenBuilder.with(qiContext).withPhraseSets(phraseSetYes).build();

        ListenResult listenResult = listen.run();

        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();
        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetYes)) {
            Log.i(TAG, "Heard phrase set: Mensa");
            runOnUiThread(() -> setContentView(R.layout.mensa));
            TestMensa(qiContext);
        }
    }



    // endregion implements EVENTS
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implements FUNCTIONS

    // region implements CHATBOT
    public void createChatBot(QiContext qiContext) throws IOException {

        // Create a topic.
        Topic greetingsTopic = TopicBuilder.with(qiContext) // Create the builder using the QiContext.
                .withResource(R.raw.greetings) // Set the topic resource.
                .build(); // Build the topic.

        // Create a new QiChatbot.
        QiChatbot qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(greetingsTopic)
                .build();

        // Create a new Chat action.
        chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

        // Add an on started listener to the Chat action.
        chat.addOnStartedListener(() -> Log.i(TAG, "Discussion started."));

        // Run the Chat action asynchronously.
        Future<Void> chatFuture = chat.async().run();

        // Add a lambda to the action execution.
        chatFuture.thenConsume(future -> {
            if (future.hasError()) {
                Log.e(TAG, "Discussion finished with error.", future.getError());
            }
        });


        ArrayList<WeekDay> week = getTimeTable("WI","1","42");



    }
    // endregion implements CHATBOT

    // region implements TIMETABLE-STUFF
    public ArrayList<WeekDay> getTimeTable(String _course, String _semester, String _kw) throws IOException {
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
        /*
        for(int i = 0; i < weekDays.size(); i++){
            WeekDay day = weekDays.get(i);
            Log.i(TAG, day.getName() + " " + day.getCourses().get(0).getName());

            //... do something like return some info or crash

        }
        */
        return weekDays;
    }


    // endregion implements TIMETABLE-STUFF
// endregion implements FUNCTIONS
}
