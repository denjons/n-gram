package com.dennisjonsson.ngram.Application;

import android.os.AsyncTask;

import com.dennisjonsson.ngram.Model.Source;
import com.dennisjonsson.ngram.Model.State;
import com.dennisjonsson.ngram.Util.DataSource;
import com.dennisjonsson.ngram.Util.SimpleNGramMarkovModel;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by dennisj on 6/17/15.
 */
public class ContentService{

    private static final String LOG_TAG = "ContentService";

    private ArrayList<Source> selectedSources;
    private Source selectedSource;

    // can be removed
    private ArrayList<String> lastResults;

    private SimpleNGramMarkovModel model;

    private DataSource dataSource;

    private CopyOnWriteArrayList<Callback> listeners;


    public enum CallbackMessage{
        CONNECTION_FAILURE,
        FAILURE_GENERATOR,
        SUCCESS_GENERATOR,
        SUCCESS_NAME_LIST,
        SUCCESS_GET_SOURCES,
        SUCCESS_CREATE_SOURCE,
        SUCCESS_UPDATE_SOURCE,
        SUCCESS_DELETING_SOURCE,
        FAILURE_NAME_LIST,
        FAILURE_GET_SOURCES,
        FAILURE_CREATE_SOURCE,
        FAILURE_UPDATE_SOURCE,
        FAILURE_DELETING_SOURCE,
        INVALID_STATE
    }

    public ContentService(DataSource dataSource){
        this.dataSource = dataSource;
        listeners = new CopyOnWriteArrayList<Callback>();
        selectedSources = new ArrayList<Source>();
    }

    public SimpleNGramMarkovModel getModel() {
        return model;
    }

    public void addListener(Callback listener){
        if(!listeners.contains(listener)){
            listeners.add(listener);
        }
    }

    public void removeListener(Callback listener){
        if(listeners.contains(listener)){
            listeners.remove(listener);
        }
    }

    private synchronized void notifyListeners(CallbackMessage msg, Object o){
        Iterator<Callback> listenersIterator = listeners.iterator();
        while(listenersIterator.hasNext()){
            listenersIterator.next().onResult(msg, o);
        }
    }

    public interface Callback {
        void onResult(CallbackMessage msg, Object o);
    }

    public synchronized void handleTextGeneration(SimpleNGramMarkovModel markovModel,
                                                  final String text, final State state){
        this.model = markovModel;
        new AsyncTask<String, Integer, ArrayList<String>>() {
            @Override
            protected ArrayList<String> doInBackground(String... strings) {

                model.clearModel(); // removes previous content

                char [] charArray = strings[0].toCharArray();
                model.readString(charArray);

                ArrayList<String> results = new ArrayList<String>();

                model.generationText(results,state.length +state.extra_text_length,
                        state.samples);
               // Log.d(LOG_TAG,"length: "+state.length+ ", samples: "+state.samples);

                MWGApplication.getTextManager().cleanUpText(results, state.length);


                return results;
            }

            @Override
            protected void onPostExecute(ArrayList<String> strings) {
                super.onPostExecute(strings);
                if(strings != null){
                    lastResults = strings;
                    notifyListeners(CallbackMessage.SUCCESS_GENERATOR, strings);
                }else{
                    notifyListeners(CallbackMessage.FAILURE_GENERATOR, strings);
                }
            }
        }.execute(text);
    }

    public synchronized void HandleTextRegeneration(final State state){
        if(this.model != null){
            new AsyncTask<Void, Integer, ArrayList<String>>() {

                @Override
                protected ArrayList<String> doInBackground(Void... voids) {

                    ArrayList<String> result = new ArrayList<String>();
                    model.generationText(result, state.length + state.extra_text_length,
                            state.samples);
                  //  Log.d(LOG_TAG,"length: "+state.length+ ", samples: "+state.samples);
                    MWGApplication.getTextManager().cleanUpText(result, state.length);
                    return result;
                }

                @Override
                protected void onPostExecute(ArrayList<String> strings) {
                    super.onPostExecute(strings);
                    if(strings.size() > 0){
                        lastResults = strings;
                        notifyListeners(CallbackMessage.SUCCESS_GENERATOR,strings);
                    }else{
                        notifyListeners(CallbackMessage.FAILURE_GENERATOR,strings);
                    }
                }

            }.execute();
        }else{
            throw new IllegalStateException("model cannot be null");
        }
    }

    public synchronized void createSource(){
        new AsyncTask<Void,Void,Source>(){

            @Override
            protected Source doInBackground(Void... voids) {
                Source source = null;
                try {
                    source = dataSource.createSource();
                }catch (Exception e){
                    return null;
                }
                return source;
            }

            @Override
            protected void onPostExecute(Source source) {
                if(source == null){
                    notifyListeners(CallbackMessage.FAILURE_CREATE_SOURCE, source);
                }
                notifyListeners(CallbackMessage.SUCCESS_CREATE_SOURCE,source);
                super.onPostExecute(source);
            }
        }.execute();

    }

    public synchronized void deleteSources(Source ... sources){

        new AsyncTask<Source,Void,Source[]>(){
            @Override
            protected Source[] doInBackground(Source... sources) {

                try {
                    dataSource.deleteSource(sources);
                }catch (Exception e){
                    return null;
                }
                return sources;
            }

            @Override
            protected void onPostExecute(Source[] sources) {
                if(sources == null){
                    notifyListeners(CallbackMessage.FAILURE_DELETING_SOURCE,sources);
                }
                notifyListeners(CallbackMessage.SUCCESS_DELETING_SOURCE,sources);
                super.onPostExecute(sources);
            }
        }.execute(sources);
    }

    public synchronized void updateSource(Source ... sources){

        new AsyncTask<Source,Void,Source[]>(){
            @Override
            protected Source[] doInBackground(Source... sources) {

                try {
                    for (Source source : sources) {
                        dataSource.updateSource(source);
                    }
                }catch(Exception e){
                    return null;
                }

                return sources;
            }

            @Override
            protected void onPostExecute(Source[] sources) {
                if(sources == null){
                    notifyListeners(CallbackMessage.FAILURE_UPDATE_SOURCE, sources);
                }
                notifyListeners(CallbackMessage.SUCCESS_UPDATE_SOURCE,sources);
                super.onPostExecute(sources);
            }
        }.execute(sources);
    }

    public synchronized void getSources(UUID[] ids){
        new AsyncTask<UUID,Void,ArrayList<Source>>(){

            @Override
            protected ArrayList<Source> doInBackground(UUID... uuids) {
                ArrayList<Source> sources = null;
                try {
                    sources = dataSource.getSources(uuids);
                }catch (Exception e){
                    return null;
                }
                if(sources != null && sources.size() <= 0){
                    return null;
                }
                return sources;
            }

            @Override
            protected void onPostExecute(ArrayList<Source> sources) {
                if(sources != null){
                    notifyListeners(CallbackMessage.SUCCESS_GET_SOURCES,sources);
                }else{
                    notifyListeners(CallbackMessage.FAILURE_GET_SOURCES,sources);
                }

                super.onPostExecute(sources);
            }
        }.execute(ids);
    }

    public synchronized void getSourceNameList() {

        new AsyncTask<Void, Void, ArrayList<Source>>() {

            @Override
            protected ArrayList<Source> doInBackground(Void... voids) {
                ArrayList<Source> sources = null;
                try {
                    sources = dataSource.getSourceNames();
                }
                catch (Exception e){
                    return null;
                }
                return sources;

            }

            @Override
            protected void onPostExecute(ArrayList<Source> sources) {
                if(sources == null){
                    notifyListeners(CallbackMessage.FAILURE_NAME_LIST, sources);
                }
                notifyListeners(CallbackMessage.SUCCESS_NAME_LIST, sources);
                super.onPostExecute(sources);
            }
        }.execute();
    }

    public ArrayList<String> getLastResults() {
        return lastResults;
    }

    public ArrayList<Source> getSelectedSources() {
        return selectedSources;
    }

    public Source getSelectedSource() {
        return selectedSource;
    }

    public void setSelectedSource(Source selectedSource) {
        this.selectedSource = selectedSource;
    }
}
