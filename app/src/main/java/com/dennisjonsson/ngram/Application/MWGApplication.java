package com.dennisjonsson.ngram.Application;

import android.app.Application;


import com.dennisjonsson.ngram.Model.State;
import com.dennisjonsson.ngram.R;
import com.dennisjonsson.ngram.Util.DataSource;
import com.dennisjonsson.ngram.Util.TextManager;

/**
 * Created by dennisj on 6/14/15.
 */
public class MWGApplication extends Application {

    private static ContentService contentService;
    private static DataSource dataSource;
    private static TextManager textManager;
    private static State applicationState;
    private static int wordLength, samples, generator_extra_text_length, markovOrder;

    @Override
    public void onCreate() {
        dataSource = new DataSource(getApplicationContext());
        contentService = new ContentService(dataSource);
        wordLength = getResources().getInteger(R.integer.initial_length);
        samples = getResources().getInteger(R.integer.initial_samples);
        generator_extra_text_length = getResources().getInteger(R.integer.generator_extra_text_length);
        markovOrder = getResources().getInteger(R.integer.initial_markov_order);
        NewApplicationState();
        textManager = new TextManager(getApplicationContext());
        super.onCreate();
    }

    public static void NewApplicationState(){
        applicationState = new State(wordLength, samples,
                generator_extra_text_length, markovOrder);
    }

    public static ContentService getContentService() {
        if(contentService == null){
            contentService = new ContentService(dataSource);
        }
        return contentService;
    }

    public static TextManager getTextManager() {
        return textManager;
    }

    public static State getApplicationState(){
        return applicationState;
    }
}
