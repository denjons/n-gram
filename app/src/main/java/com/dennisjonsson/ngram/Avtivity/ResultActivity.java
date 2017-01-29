package com.dennisjonsson.ngram.Avtivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dennisjonsson.ngram.Application.ContentService;
import com.dennisjonsson.ngram.Application.MWGApplication;
import com.dennisjonsson.ngram.Model.State;
import com.dennisjonsson.ngram.R;
import com.dennisjonsson.ngram.Util.SimpleNGramMarkovModel;

import java.util.ArrayList;

public class ResultActivity extends Activity implements ContentService.Callback{

    private static final String LOG_TAG = "ResultActivity";

    private LinearLayout spinnerContainer;

    //private TextView topResultTextView, middleResultTextView, lowResultTextView;

    private Button regenerateButton;// floatingRegenerateButton;

    private TextView wordLengthLabel, samplesLabel;
    private SeekBar wordLengthSeekBar, samplesSeekBar;

    private ScrollView scrollView;

    private String wordLengthLabelText, samplesLabelText;

    private ContentService service;
    private SimpleNGramMarkovModel model;

    private LinearLayout textContainer;// floatingContainer, settingsContainer;

    private int wordLength, samples, markovOrder, max_samples, max_length;

    private int result_text_size, result_padding_horizontal, result_padding_vertical, screenHeight;

    private State state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //setTitle(getResources().getString(R.string.main_title));
        //setTitle("");
        setTitle(getResources().getString(R.string.title_activity_result));

        getActionBar().setDisplayHomeAsUpEnabled(true);

        service = MWGApplication.getContentService();
        model = service.getModel();

        if( model == null ){
            NavUtils.navigateUpFromSameTask(this);
            return;

        }

        result_text_size = getResources().getInteger(R.integer.result_text_size);
        result_padding_horizontal = getResources().getInteger(R.integer.result_padding_horizontal);
        result_padding_vertical = getResources().getInteger(R.integer.result_padding_vertical);

        max_samples = getResources().getInteger(R.integer.max_samples);
        max_length = getResources().getInteger(R.integer.max_length);

     //   floatingContainer = (LinearLayout)findViewById(R.id.result_floating_container);

        scrollView = (ScrollView)findViewById(R.id.result_scroll_view);

        spinnerContainer = (LinearLayout)findViewById(R.id.result_progressBar_container);

     //   settingsContainer = (LinearLayout)findViewById(R.id.result_settings_container);

        textContainer = (LinearLayout)findViewById(R.id.result_text_container);

        ArrayList<String> results = service.getLastResults();

        if(results != null){
            setResultText(results);
        }
        else{
           // Log.d(LOG_TAG, "result list is null!");
        }

        regenerateButton = (Button)findViewById(R.id.result_regenerate_button);
       // floatingRegenerateButton = (Button)findViewById(R.id.result_floating_regenerate_button);

        View.OnClickListener onClickListener =new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinnerContainer.setVisibility(View.VISIBLE);
                enableGUI(false);
                service.HandleTextRegeneration(state);
            }
        };

        regenerateButton.setOnClickListener(onClickListener);
   //     floatingRegenerateButton.setOnClickListener(onClickListener);

        wordLengthLabel = (TextView)findViewById(R.id.result_length_label);
        samplesLabel = (TextView)findViewById(R.id.result_samples_label);

        wordLengthLabelText = getString(R.string.word_length);
        samplesLabelText = getString(R.string.samples);

        state = MWGApplication.getApplicationState();

        if(state == null){
            MWGApplication.NewApplicationState();
            state = MWGApplication.getApplicationState();
        }

        samples = state.samples;
        wordLength = state.length;

        markovOrder = model.getN();

        wordLengthLabel.setText(wordLengthLabelText+": "+wordLength);
        samplesLabel.setText(samplesLabelText+": "+samples);

        wordLengthSeekBar = (SeekBar)findViewById(R.id.result_length_seekbar);
        samplesSeekBar = (SeekBar)findViewById(R.id.result_samples_seekbar);

        wordLengthSeekBar.setMax(max_length -1-markovOrder);
        wordLengthSeekBar.setProgress(wordLength-1);

        samplesSeekBar.setMax(max_samples-1);
        samplesSeekBar.setProgress(samples-1);

        samplesSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
               // Log.d(LOG_TAG, "progress: " + i + ", samples: " + samples);
                samples = i+1;
                state.samples = samples;
                String str = samplesLabelText+": "+samples;
                samplesLabel.setText(str);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        wordLengthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
               // Log.d(LOG_TAG, "progress: " + i + ", wordLength: " + wordLength);
                wordLength = i+1+markovOrder;
                state.length = wordLength;
                String str = wordLengthLabelText+": "+wordLength;
                wordLengthLabel.setText(str);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void enableGUI(boolean lock){
        wordLengthSeekBar.setEnabled(lock);
        samplesSeekBar.setEnabled(lock);
        regenerateButton.setEnabled(lock);
    }

    @Override
    protected void onResume() {
        if(model == null){
            NavUtils.navigateUpFromSameTask(this);
            super.onResume();
        }
        service.addListener(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        service.removeListener(this);
        super.onPause();
    }

    private void setResultText(ArrayList<String> results){
       // topResultTextView.setText("");
       // middleResultTextView.setText("");
       // lowResultTextView.setText("");
        textContainer.removeAllViews();
        for(int i = 0; i < results.size(); i ++){
            TextView textView = new TextView(getApplicationContext());
            textView.setTextSize(result_text_size);
            textView.setTextColor(getResources().getColor(R.color.result_text_color));
            textView.setGravity(Gravity.CENTER);
            textView.setText(results.get(i));
            textView.setPadding(result_padding_horizontal,result_padding_vertical,
                    result_padding_horizontal,result_padding_vertical);
            textView.setTextIsSelectable(true);
            if(i%2>0){
                textView.setBackgroundColor(getResources().getColor(R.color.result_marked_background));
            }
            textContainer.addView(textView);

        }
    }

    private void scrollToTop(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_UP);
            }
        };
        scrollView.post(runnable);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResult(ContentService.CallbackMessage msg, Object o) {
        if(msg == ContentService.CallbackMessage.SUCCESS_GENERATOR){
            ArrayList<String> results = (ArrayList<String>)o;
            setResultText(results);
            scrollToTop();
        }
        if(msg == ContentService.CallbackMessage.SUCCESS_UPDATE_SOURCE){
           // Log.d(LOG_TAG,"callback: sources updated");
        }

        if(msg == ContentService.CallbackMessage.FAILURE_GET_SOURCES ||
                msg == ContentService.CallbackMessage.FAILURE_GENERATOR ||
                msg == ContentService.CallbackMessage.FAILURE_CREATE_SOURCE ||
                msg == ContentService.CallbackMessage.FAILURE_DELETING_SOURCE ||
                msg == ContentService.CallbackMessage.FAILURE_NAME_LIST ||
                msg == ContentService.CallbackMessage.FAILURE_UPDATE_SOURCE){
            restartApp();
        }

        spinnerContainer.setVisibility(View.INVISIBLE);
        enableGUI(true);
    }

    private void restartApp(){

       // AlertDialog.Builder builder = new AlertDialog.Builder(ResultActivity.this);
       // builder.setMessage(R.string.error_restart_message);
       // builder.create();

        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}
