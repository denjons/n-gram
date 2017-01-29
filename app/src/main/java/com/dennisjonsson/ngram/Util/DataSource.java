package com.dennisjonsson.ngram.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.dennisjonsson.ngram.Model.Source;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by dennisj on 6/17/15.
 */
public class DataSource {
    private static final String LOG_TAG = "markovwordgenerator.Util.DataSource";
    private DataBaseHelper dbHelper;

    public DataSource(Context context) {
        this.dbHelper = new DataBaseHelper(context);

    }

    /*
    *   Get content values from source
    * */
    public ContentValues getValuesForSource(Source source) {

        ContentValues values = new ContentValues();
        String name = source.getName();
        String content = source.getContent();
        values.put(DataBaseHelper.COLUMN_SOURCES_ID, source.getId().toString());
        values.put(DataBaseHelper.COLUMN_SOURCES_NAME, (name!=null) ? name : "" );
        values.put(DataBaseHelper.COLUMN_SOURCES_CONTENT, (content!=null) ? content : "" );

        return values;
    }

    /*
    *   Get source from cursor
    * */
    private Source getSourceFromCursor(Cursor cursor) {
        Source source = new Source(
                UUID.fromString(
                cursor.getString(cursor.getColumnIndex(dbHelper.COLUMN_SOURCES_ID))),
                cursor.getString(cursor.getColumnIndex(dbHelper.COLUMN_SOURCES_NAME)),
                cursor.getString(cursor.getColumnIndex(dbHelper.COLUMN_SOURCES_CONTENT))
        );

        return source;
    }

    /*
    *   Create new Source and store in DB
    * */

    public Source createSource(){

        Source source = new Source(
                UUID.randomUUID(),
                "",
                ""
        );

        ContentValues values = getValuesForSource(source);

        long insertId = dbHelper.getWritableDatabase().insert(DataBaseHelper.TABLE_SOURCES, null, values);

       // Log.d(LOG_TAG, "insert id: " + insertId);

        return source;
    }

    /*
    *   Update source
    * */

    public void updateSource(Source source){
        ContentValues values = getValuesForSource(source);
        dbHelper.getWritableDatabase().update(DataBaseHelper.TABLE_SOURCES, values,
                DataBaseHelper.COLUMN_SOURCES_ID+" = '"+source.getId().toString()+"'", null);
    }

    /*
    *   Get list of Sources
    * */

    public ArrayList<Source> getSources(UUID[] uuids){

        StringBuilder strBuilder = new StringBuilder(uuids.length*43+2);
        strBuilder.append("(");
        for(int i = 0; i < uuids.length; i++){
            strBuilder.append("'"+uuids[i]+"'");
            if(i < uuids.length -1){
                strBuilder.append(",");
            }
        }
        strBuilder.append(")");

        String selectQuery = "SELECT  * FROM " + DataBaseHelper.TABLE_SOURCES+" WHERE "+
                DataBaseHelper.COLUMN_SOURCES_ID+" in "+strBuilder.toString();
        Cursor cursor = dbHelper.getWritableDatabase().rawQuery(selectQuery, null);

        ArrayList<Source> list = new ArrayList<Source>();

        // loop through cursor and add all rows to list
        if (cursor.moveToFirst()) {
            do {
                Source source = getSourceFromCursor(cursor);
                list.add(source);
            } while (cursor.moveToNext());
        }

        return list;
    }

    public ArrayList<Source> getSourceNames(){

        String selectQuery = "SELECT  "+DataBaseHelper.COLUMN_SOURCES_ID+", "+
                DataBaseHelper.COLUMN_SOURCES_NAME+" FROM " + DataBaseHelper.TABLE_SOURCES;

        Cursor cursor = dbHelper.getWritableDatabase().rawQuery(selectQuery, null);

        ArrayList<Source> list = new ArrayList<Source>();

        // loop through cursor and add all rows to list
        if (cursor.moveToFirst()) {
            do {

                Source source = new Source(
                        UUID.fromString(
                                cursor.getString(cursor.getColumnIndex(dbHelper.COLUMN_SOURCES_ID))),
                        cursor.getString(cursor.getColumnIndex(dbHelper.COLUMN_SOURCES_NAME)),
                        null
                );
                list.add(source);
            } while (cursor.moveToNext());
        }

        return list;
    }

    /*
    *   Delete Source
    * */
    public void deleteSource(Source[] source){
        StringBuilder strBuilder = new StringBuilder(source.length*43+2);
        strBuilder.append("(");
        for(int i = 0; i < source.length; i++){
            strBuilder.append("'"+source[i].getId().toString()+"'");
            if(i < source.length -1){
                strBuilder.append(",");
            }
        }
        strBuilder.append(")");
        dbHelper.getWritableDatabase().delete(DataBaseHelper.TABLE_SOURCES,
                DataBaseHelper.COLUMN_SOURCES_ID+" in "+strBuilder.toString() , null);

    }

}
