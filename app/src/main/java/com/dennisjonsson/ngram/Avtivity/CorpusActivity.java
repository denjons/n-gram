package com.dennisjonsson.ngram.Avtivity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dennisjonsson.ngram.Application.ContentService;
import com.dennisjonsson.ngram.Application.MWGApplication;
import com.dennisjonsson.ngram.Model.Source;
import com.dennisjonsson.ngram.R;

import java.util.ArrayList;
import java.util.UUID;

public class CorpusActivity extends ListActivity implements ContentService.Callback{

    private static final String LOG_TAG = "CorpusActivity";

    private SourceListAdapter adapter;
    ContentService service;
    ArrayList<Source> selectedSources;
    ArrayList<Source> sources;

    int [] list_item_colours;

    private LinearLayout progressBarContainer;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corpus);
        //setTitle(getResources().getString(R.string.main_title));
        setTitle("");
        setTitle(getResources().getString(R.string.title_activity_corpus));
        service = MWGApplication.getContentService();

        list_item_colours = getResources().getIntArray(R.array.list_item_colours);

        selectedSources = service.getSelectedSources();
        sources = new ArrayList<Source>();

        progressBarContainer = (LinearLayout)findViewById(R.id.corpus_progressbar_container);


        adapter = new SourceListAdapter(sources);
       // Log.d("GeneratorActivity", "size: " + sources.size());

        setListAdapter(adapter);
        listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                // Here you can do something when items are selected/de-selected,
                // such as update the title in the CAB
               // Log.d(LOG_TAG,"checked position: "+position);
                Source source = sources.get(position);
                if(!selectedSources.contains(source)){
                    selectedSources.add(source);
                }else{
                    selectedSources.remove(source);
                }
                //listView.setItemChecked(position,checked);

            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.menu_corpus_delete_source:
                        // copy selected sources
                        final ArrayList<Source> sourcesToBeDeleted =
                                new ArrayList<Source>(selectedSources);

                        new AlertDialog.Builder(CorpusActivity.this)
                                .setTitle(getResources().getString(R.string.delete))
                                .setMessage(
                                        getResources().getString(
                                                R.string.question_delete_selected_Sources))
                                .setIcon(android.R.drawable.ic_input_delete)
                                .setPositiveButton(android.R.string.yes,
                                        new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        HandleDeleteSelectedSources(sourcesToBeDeleted);
                                    }})
                                .setNegativeButton(android.R.string.no,null).show();

                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.corous_context, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
                selectedSources.clear();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }


        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.corpus, menu);
        return true;
    }

    class SourceListAdapter extends ArrayAdapter<Source> {
        public SourceListAdapter(ArrayList<Source> sources){
            super(getApplicationContext(),0,sources);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null){
                convertView = getLayoutInflater().
                        inflate(R.layout.list_item_source, null);
            }

            // BACKGROUND

            View background = (View)convertView.findViewById(R.id.list_item_background);

            background.setBackgroundColor(list_item_colours[position%list_item_colours.length]);

            // TITLE
            TextView titleEntryTextView =
                    (TextView)convertView.findViewById(R.id.list_item_source_title);
            String name = sources.get(position).getName();
            String content = sources.get(position).getContent();
            if(name != null && name.length() > 0){
                titleEntryTextView.setText(name);
            }else{
                titleEntryTextView.setText(getResources().
                        getString(R.string.empty_corpus)+" "+(position+1));
            }


            Source source = getItem(position);
            return convertView;

        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.menu_corpus_add_source) {
            service.createSource();
            progressBarContainer.setVisibility(View.VISIBLE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Source source = sources.get(position);

        handleSourceSelected(source);
    }

    @Override
    protected void onResume() {
        if(service == null){
            service = MWGApplication.getContentService();
        }
        service.addListener(this);
        service.getSourceNameList();
        super.onResume();
    }

    @Override
    protected void onPause() {
        service.removeListener(this);
        super.onPause();
    }

    @Override
    public void onResult(ContentService.CallbackMessage msg, Object o) {

        if(msg == ContentService.CallbackMessage.SUCCESS_CREATE_SOURCE){
            handleAddSource((Source) o);
        }
        else if(msg == ContentService.CallbackMessage.SUCCESS_NAME_LIST){
            initializeList((ArrayList<Source>)o);
        }else if(msg == ContentService.CallbackMessage.SUCCESS_UPDATE_SOURCE){
            //Log.d(LOG_TAG,"callback: sources updated");
        }
        else if(msg == ContentService.CallbackMessage.SUCCESS_GET_SOURCES){
            ArrayList<Source> sources = (ArrayList<Source>)o;
            if(sources.size() == 1){
                handleOpenSource(sources.get(0));
            }else{
                // merge all in one source: next version
            }
        }else if(msg == ContentService.CallbackMessage.FAILURE_GET_SOURCES){
           // Log.d(LOG_TAG, "failed to get sources");
        }else if(msg == ContentService.CallbackMessage.SUCCESS_DELETING_SOURCE){
            handleRemoveDeletedSourcesFromList((Source[])o);
        }

        if(msg == ContentService.CallbackMessage.FAILURE_GET_SOURCES ||
                msg == ContentService.CallbackMessage.FAILURE_GENERATOR ||
                msg == ContentService.CallbackMessage.FAILURE_CREATE_SOURCE ||
                msg == ContentService.CallbackMessage.FAILURE_DELETING_SOURCE ||
                msg == ContentService.CallbackMessage.FAILURE_NAME_LIST ||
                msg == ContentService.CallbackMessage.FAILURE_UPDATE_SOURCE){
            restartApp();
        }
        progressBarContainer.setVisibility(View.INVISIBLE);
    }

    private void restartApp(){

        AlertDialog.Builder builder = new AlertDialog.Builder(CorpusActivity.this);
        builder.setMessage(R.string.error_restart_message);
        builder.create();

        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void handleAddSource(Source source){
        sources.add(source);
        ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
        service.setSelectedSource(source);
        Intent intent = new Intent(this, GeneratorActivity.class);
        startActivity(intent);
    }

    private void handleSourceSelected(Source source){
        progressBarContainer.setVisibility(View.VISIBLE);
        service.getSources(new UUID[]{source.getId()});
    }

    private void handleOpenSource(Source source){
        service.setSelectedSource(source);
        progressBarContainer.setVisibility(View.INVISIBLE);
        Intent intent = new Intent(this, GeneratorActivity.class);
        startActivity(intent);
    }

    private void HandleDeleteSelectedSources(ArrayList<Source> sourcesToBeDeleted){

        progressBarContainer.setVisibility(View.VISIBLE);
        service.deleteSources(Source.ArrayListToArray(sourcesToBeDeleted));
    }

    private void handleRemoveDeletedSourcesFromList(Source[] sources){
        for(int i = 0; i < sources.length; i++){
            this.sources.remove(sources[i]);
        }
        ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
        Toast.makeText(CorpusActivity.this,
                getResources().getString(R.string.deleted_sources), Toast.LENGTH_SHORT).show();
    }


    private void initializeList(ArrayList<Source> newSources){
        sources.clear();
        for(Source source : newSources){
            this.sources.add(source);
        }
        ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
    }


}
