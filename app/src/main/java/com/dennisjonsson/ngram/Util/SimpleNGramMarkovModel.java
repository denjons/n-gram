package com.dennisjonsson.ngram.Util;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by dennisj on 6/17/15.
 */
public class SimpleNGramMarkovModel {

    private static final String LOG_TAG = "SimpleNGramMArkovModel";

    private HashMap<String, ArrayList<Integer>> ngrams;
    private int n = 0;

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    private String [] keys;

    private TextManager textManager;

    public SimpleNGramMarkovModel(int n, TextManager textManager) {
        this.n = n;
        ngrams = new HashMap<String, ArrayList<Integer>>();
        this.textManager = textManager;
    }

    public void setOrder(int n){
        this.n = n;
    }


    public void readString(char[] array){

        char next;
        StringBuilder stringBuilder = new StringBuilder(n);

        for(int i = 0; i < array.length; i++){

            next = array[i];

            /*!textManager.isBadChar(next)*/
            if(!textManager.isValidChar(next) ){
                next = ' ';
            }


            if( /*next == ' '  &&*/
                    (stringBuilder.length() <= 0 || !(next == ' '
                    &&  stringBuilder.charAt(stringBuilder.length()-1) == ' '))){

                if(stringBuilder.length() >= n) {

                    String str = stringBuilder.toString();
                    ArrayList<Integer> strings = ngrams.get(str);

                    if (strings != null) {
                        strings.add((int) next);
                    } else {
                        strings = new ArrayList<Integer>();
                        strings.add((int) next);
                        ngrams.put(str, strings);
                    }
                    stringBuilder.deleteCharAt(0);
                }

                stringBuilder.append(next);
            }

        }
    }

    public void clearModel(){
        ngrams.clear();
        keys = null;
    }

    public String generateStart(){
        if(keys == null){
            keys =  ngrams.keySet().toArray(new String [0]);
        }
        if(keys != null && keys.length > 0) {
            return keys[(int) (Math.random() * (keys.length - 1))];
        }
        return null;
    }


    public void generationText(ArrayList<String> resultSet, int length, int samples){

        StringBuilder strb = new StringBuilder(length);
        boolean continueLoop = true;
        for(int i = 0; i < samples; i++){

            strb.append(generateStart());
            continueLoop = true;
            while(strb.length() < length && continueLoop){
                ArrayList<Integer> nextChars =
                        ngrams.get(strb.substring(strb.length()-n,strb.length()));

                if(nextChars!=null){
                    strb.append((char)nextChars.get((int)((nextChars.size())*Math.random())).intValue());
                }else{
                    continueLoop = false;
                }
            }
            resultSet.add(strb.toString());
            strb.delete(0,strb.length());
        }

        //System.out.println(strb.toString());
    }


}
