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

// TODO:
// ├ Set variable chat to null if focus lost for x seconds
// └ outsource classes and not-main functions

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {
     /* Main Function of this Applications
        ├ Creates chatBots, handles interactions
        └ Some more magic with "AI"
     */

    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implements VARIABLES

    private static final String TAG = "MainActivity";
    private static final boolean DEBUG_MODE = true;

    private QiChatbot qiChatbot;
    private Chat chat;
    private Bookmark proposalBookmark;
    private Map<String, Bookmark> bookmarks;

    private TimeTableChatBot ttchatBot = null;

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
        ttchatBot = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Remove on started listeners from the Chat action.
        if (chat != null) {
            chat.removeAllOnStartedListeners();
        }
        ttchatBot = null;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.i(TAG,"### Focus gained ###");
        HelperCollection.Say(qiContext, "Hallo ich bin Robotn");

        if(true){
            ttchatBot = new TimeTableChatBot(qiContext);
            ttchatBot.start();
        }

        //Hear for keyword
        //TestMensa(qiContext);
    }

    // endregion implements EVENTS
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implement TESTING

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

    // endregion TESTING


}


/* ----- ----- EOF ----- ----- ----- ----- ----- ----- ----- ----- */
