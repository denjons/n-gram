package com.dennisjonsson.ngram.Model;

/**
 * Created by dennisj on 6/26/15.
 */
public class State {

    public int length;
    public int samples;
    public int extra_text_length;
    public int markov_order;

    public State(int length, int samples, int extra_text_length, int markov_order) {
        this.length = length;
        this.samples = samples;
        this.extra_text_length = extra_text_length;
        this.markov_order = markov_order;
    }
}
