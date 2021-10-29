package com.example.hsb_pepper;


import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.util.PhraseSetUtil;

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

    QiContext qiContext = null;
    ImageView imageView;

    private QiChatbot qiChatbot;
    private Chat chat;
    private Bookmark proposalBookmark;
    private Map<String, Bookmark> bookmarks;

    private TimeTableChatBot TTChatBot = null;

    //Mensa Stuff
    private String mensaURL = "https://informatik.hs-bremerhaven.de/docker-hbv-kms-web/mensa";
    public Mensa mensa;

    // endregion implements VARIABLES
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implements EVENTS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Select img id
        imageView = (ImageView)findViewById(R.id.iMensa);
        //Get Mensa img and csv
        GetMensaData getMensaData = new GetMensaData(imageView, new AsyncResponse() {
            @Override
            public void processFinish(Mensa result) {
                mensa = result;
                System.out.println("got Mensa Info");
            }
        });
        getMensaData.execute(mensaURL);

        QiSDK.register(this, this);
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
        TTChatBot = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Remove on started listeners from the Chat action.
        if (chat != null) {
            chat.removeAllOnStartedListeners();
        }
        TTChatBot = null;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.i(TAG,"### Focus gained ###");
        this.qiContext = qiContext;
        HelperCollection.Say(qiContext, "Hallo");


        if(true){
            TTChatBot = new TimeTableChatBot(qiContext);
            TTChatBot.start();
        }



        TestMensa(qiContext);
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

            runOnUiThread(() -> setContentView(R.layout.mensa_layout));
            System.out.println(mensa.getDay()[2]);
            System.out.println(mensa.getOffer1()[0]);
            System.out.println(mensa.getOffer2()[4]);

            TestMensa(qiContext);
        }
    }

    // endregion TESTING
}


/* ----- ----- EOF ----- ----- ----- ----- ----- ----- ----- ----- */
