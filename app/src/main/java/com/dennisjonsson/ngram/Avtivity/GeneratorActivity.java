package com.dennisjonsson.ngram.Avtivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.NavUtils;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dennisjonsson.ngram.Application.ContentService;
import com.dennisjonsson.ngram.Application.MWGApplication;
import com.dennisjonsson.ngram.Model.Source;
import com.dennisjonsson.ngram.Model.State;
import com.dennisjonsson.ngram.R;
import com.dennisjonsson.ngram.Util.SimpleNGramMarkovModel;
import com.dennisjonsson.ngram.Util.TextManager;

public class GeneratorActivity extends Activity implements ContentService.Callback  {

    private static final String LOG_TAG = "GeneratorActivity";

    private ContentService service;
    private InputMethodManager inputManger;

    private Source source;

    private int max_order, samples, wordLength, markovOrder, source_name_length;

    SimpleNGramMarkovModel model;

    private Button generatorButton;
    private SeekBar orderPicker;
    private TextView orderLabel;
    private EditText corpus;

    private ScrollView scrollView;

    private LinearLayout spinnerContainer;

    private String markovOrderLabelText;

    private State state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generator);

       // setTitle(getResources().getString(R.string.main_title));
       // setTitle("");
       setTitle(getResources().getString(R.string.title_activity_generator));

        source_name_length = getResources().getInteger(R.integer.source_name_length);

        state = MWGApplication.getApplicationState();

        if(state == null){
            MWGApplication.NewApplicationState();
            state = MWGApplication.getApplicationState();
        }
        max_order = getResources().getInteger(R.integer.max_markov_order);

        samples = state.samples;
        wordLength = state.length;
        markovOrder = state.markov_order;


        scrollView = (ScrollView)findViewById(R.id.generator_scroll_view);
        markovOrderLabelText = getString(R.string.markov_order);
        orderLabel = (TextView)findViewById(R.id.generator_order_label);

        spinnerContainer = (LinearLayout)findViewById(R.id.generator_progressbar_container);

        // widgets
        generatorButton = (Button)findViewById(R.id.generator_button);
        orderPicker = (SeekBar)findViewById(R.id.generator_order_seekbar);

        String txt = markovOrderLabelText+": "+markovOrder;
        orderLabel.setText(txt);

        orderPicker.setMax(max_order-1);
        orderPicker.setProgress(markovOrder-1);

        corpus = (EditText)findViewById(R.id.generator_corpus_textView);

        inputManger = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        // services
        model = new SimpleNGramMarkovModel(markovOrder, MWGApplication.getTextManager());
        service = MWGApplication.getContentService();
        source = service.getSelectedSource();


        orderPicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                markovOrder = i+1;
                state.markov_order = markovOrder;
               // Log.d(LOG_TAG,"progress: "+i+", markovOrder: "+markovOrder);
                model.setOrder(markovOrder);
                String str = markovOrderLabelText+": "+markovOrder;
                orderLabel.setText(str);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleTextProcessing();
            }
        };

        generatorButton.setOnClickListener(onClickListener);

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void handleTextProcessing(){

        InputMethodSubtype inputMethodSubtype = inputManger.getCurrentInputMethodSubtype();
       // Log.d(LOG_TAG, inputMethodSubtype.getLocale());

        String text = corpus.getText().toString();
        if(text != null && validString(text)){
            spinnerContainer.setVisibility(View.VISIBLE);
            enableGUI(false);
            service.handleTextGeneration(model, text, state);
        }else{
            int duration = Toast.LENGTH_SHORT;
            String message = getResources().getString(R.string.corpus_too_short);
            Toast toast = Toast.makeText(getApplicationContext(),
                    message, duration);
            toast.show();
        }

    }

    private boolean validString(String string){
        int validChars = 0;
        TextManager textManager = MWGApplication.getTextManager();
        for(int i = 0; i < string.length(); i++){
            if(textManager.isValidChar(string.charAt(i)) ){
                validChars ++;
            }
            if(validChars > markovOrder){
                return true;
            }
        }
        return false;
    }

    private void enableGUI(boolean lock){
        generatorButton.setEnabled(lock);
        orderPicker.setEnabled(lock);
    }

    private String [] getValuesAsStrings(int low, int high){
        String [] values = new String [high-low];
        for(int i = 0; i<high-low; i++) {
            values[i] = new Integer(i+low).toString();
        }
        return values;
    }


    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        if(service == null){
            NavUtils.navigateUpFromSameTask(this);
            super.onResume();
        }
        source = service.getSelectedSource();
        if(source == null){
            NavUtils.navigateUpFromSameTask(this);
            super.onResume();
        }
        if(corpus == null){
            restartApp();
        }
        service.addListener(this);
        corpus.setText(source.getContent());
        scrollToBottom();
        super.onResume();
    }

    @Override
    protected void onPause() {
        service.removeListener(this);
        handleUpdateSource();
        super.onPause();
    }

    private void scrollToBottom(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        };
        scrollView.post(runnable);
    }

    private void handleUpdateSource(){
        String content = corpus.getText().toString();
        source.setContent(content);
        if(content != null && content.length() > 0){
            if(content.length() > source_name_length){
                source.setName(content.substring(0,source_name_length)+"...");
            }else{
                source.setName(content.substring(0,content.length())+"...");
            }
        }else{
            source.setName("");
        }
        service.updateSource(source);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.generator, menu);
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
            Intent intent = new Intent(this, ResultActivity.class);
            startActivity(intent);
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
        //AlertDialog.Builder builder = new AlertDialog.Builder(GeneratorActivity.this);
        //builder.setMessage(R.string.error_restart_message);
        //builder.create();

        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

}
