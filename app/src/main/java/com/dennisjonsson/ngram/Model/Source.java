package com.dennisjonsson.ngram.Model;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by dennisj on 6/18/15.
 */
public class Source {

    private UUID id;
    private String name;
    private String Content;

    public Source() {
    }

    public Source(UUID id, String name, String content) {
        this.id = id;
        this.name = name;
        Content = content;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public static Source[] ArrayListToArray(ArrayList<Source> sources){
        Source[] sourcesArray = new Source[sources.size()];
        for(int i = 0; i < sourcesArray.length; i++){
            sourcesArray[i] = sources.get(i);
        }
        return sourcesArray;
    }
}
