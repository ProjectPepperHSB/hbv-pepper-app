package com.example.hsb_pepper;

import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.DiscussBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Discuss;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.conversation.Say;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {


    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implements VARIABLES
    private static final String TAG = "MainActivity";
    private static final boolean DEBUG_MODE = true;

    //qiChat
    // Store the Chat action.
    private Chat chat;

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
    public void onRobotFocusGained(QiContext qiContext) {
        createChatBot(qiContext);
    }

    @Override
    public void onRobotFocusLost() {
        // The robot focus is lost.

        // Remove on started listeners from the Chat action.
        if (chat != null) {
            chat.removeAllOnStartedListeners();
        }
    }
    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }

    // endregion implements EVENTS
    /* ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- */
    // region implements FUNCTIONS

    // region implements CHATBOT
    public void createChatBot(QiContext qiContext){
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

    }
    // endregion implements CHATBOT
    // endregion implements FUNCTIONS
}