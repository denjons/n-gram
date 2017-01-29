package com.dennisjonsson.ngram.Util;

import android.content.Context;

import com.dennisjonsson.ngram.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by dennisj on 6/24/15.
 */
public class TextManager{

    private static final String LOG_TAG = "TextManager";

    private int minWords;
    private float ratio;
    public int [] badChars;

    public TextManager(Context context) {

        this.minWords = context.getResources().
                getInteger(R.integer.text_manager_min_words);
        this.ratio = context.getResources().
                getFraction(R.fraction.text_manager_max_blank_characters_in_result_fraction,1,1);
        badChars = context.getResources().getIntArray(R.array.bad_chars);
    }

    public boolean isValidChar(char c){
        int type = Character.getType(c);
        return ((type == 1 || type == 2) || type == 5);
    }

    public void cleanUpText(ArrayList<String> list, int textLength){

        for(int i = 0; i < list.size(); i ++){
            list.set(i,
                    cleanUpText(list.get(i), textLength));
        }

    }


    public String cleanUpText(String text, int textLength){

        if((text == null || text.isEmpty())){
            return "";
        }

        if(text.length() < textLength){
            return text;
        }

        String [] strings = text.split(" ");

        // remove 2 outer words at each end in case they are cut off
        if(strings.length > minWords){
            String [] temp = new String[strings.length-2];
            System.arraycopy(strings,1,temp,0,temp.length);
        }

        // sort after largest words

        Arrays.sort(strings, new Comparator<String>() {
            @Override
            public int compare(String s, String s2) {
                if(s.length()>s2.length()){
                    return -1;
                }else if(s.length()<s2.length()){
                    return  1;
                }
                return 0;
            }
        });

        // fill stringBuilder with largest words
        StringBuilder stringBuilder = new StringBuilder(textLength);

        double length = textLength;
        double stringBuilderLength = 0;
        for (int i = 0; i <  strings.length; i++){
            if(strings[i].length() + stringBuilder.length() <= textLength){
                stringBuilder.append(strings[i]);
                stringBuilderLength = stringBuilder.length();
                if(stringBuilderLength/length >= (1.0-ratio)){
                    setAllToFirstsToUpperCase(stringBuilder);
                    return stringBuilder.toString();
                }
                stringBuilder.append(" ");
                stringBuilderLength ++;
            }
        }

        // if string builder is empty after loop
        stringBuilder.append(strings[0].substring(0,textLength-stringBuilder.length()));
        setAllToFirstsToUpperCase(stringBuilder);


        return stringBuilder.toString();
    }

    private void setAllToFirstsToUpperCase(StringBuilder stringBuilder){
        stringBuilder.replace(0,1,Character.toUpperCase(stringBuilder.charAt(0))+"");
        for(int i = 1; i < stringBuilder.length(); i++){
            if(stringBuilder.charAt(i-1) ==  ' '){
                stringBuilder.replace(i,i+1,Character.toUpperCase(stringBuilder.charAt(i))+"");
            }

        }
    }

}
