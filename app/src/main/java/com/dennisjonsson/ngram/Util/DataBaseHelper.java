package com.dennisjonsson.ngram.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dennisjonsson.ngram.R;

import java.util.UUID;

class DataBaseHelper extends SQLiteOpenHelper{
	
	public static final String DB_NAME = "markovWordGenerator.sqLite";
	private static final int VERSION = 1;

    // tables
    public static final String TABLE_SOURCES = "Sources";
    public static final String TABLE_URLS = "Urls";
    public static final String TABLE_GROUPS = "Groups";
    public static final String TABLE_INGROUPS = "InGroups";

    // SOURCES columns
	public static final String COLUMN_SOURCES_ID = "_id";
    public static final String COLUMN_SOURCES_NAME = "name";
    public static final String COLUMN_SOURCES_CONTENT = "content";

    // URL columns
	public static final String COLUMN_URL_URL = "url";
	public static final String COLUMN_URL_SOURCE = "source";


    // GROUPS columns
    public static final String COLUMN_GROUPS_ID = "id";
    public static final String COLUMN_GROUPS_NAME = "name";

    // GROUPS columns
    public static final String COLUMN_INGROUPS_GROUP = "id";
    public static final String COLUMN_INGROUPS_SOURCE = "name";

    private Context context;


	public DataBaseHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
        this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {


		db.execSQL("create table if not exists "+TABLE_SOURCES+"("+
                COLUMN_SOURCES_ID+" VARCHAR(40), "+
                COLUMN_SOURCES_NAME+" VARCHAR(40),"+
                COLUMN_SOURCES_CONTENT+" TEXT); ");

        db.execSQL("create table if not exists "+TABLE_URLS+"("+
                COLUMN_URL_URL+" VARCHAR(40), "+
                COLUMN_URL_SOURCE+" VARCHAR(40), "+
                "FOREIGN KEY ("+COLUMN_URL_SOURCE+") REFERENCES "+TABLE_SOURCES+"("+COLUMN_SOURCES_ID+")); ");

        db.execSQL("create table if not exists "+TABLE_GROUPS+"("+
                COLUMN_GROUPS_ID+" VARCHAR(40),"+
                COLUMN_GROUPS_NAME+" VARCHAR(40)); ");

        db.execSQL("create table if not exists "+TABLE_INGROUPS+"("+
                COLUMN_INGROUPS_GROUP+" VARCHAR(40), "+
                COLUMN_INGROUPS_SOURCE+" VARCHAR(40),"+
                "FOREIGN KEY ("+COLUMN_INGROUPS_GROUP+
                    ") REFERENCES "+TABLE_GROUPS+"("+COLUMN_GROUPS_ID+"),"+
                "FOREIGN KEY ("+COLUMN_INGROUPS_SOURCE+
                    ") REFERENCES "+TABLE_SOURCES+"("+COLUMN_SOURCES_ID+")); ");

        // insert example data

        String [] examples = context.getResources().getStringArray(R.array.example_corpus);
        String example = context.getResources().getString(R.string.example);
        for(int i = 0; i < examples.length; i++){

            ContentValues values = new ContentValues();
            values.put(DataBaseHelper.COLUMN_SOURCES_ID, UUID.randomUUID().toString());
            values.put(DataBaseHelper.COLUMN_SOURCES_NAME, example+" "+(i+1));
            values.put(DataBaseHelper.COLUMN_SOURCES_CONTENT, examples[i]);
            db.insert(DataBaseHelper.TABLE_SOURCES, null, values);
        }
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		
	}


}
