package edu.northeastern.model;

public class SearchResult {
    private final String text;
    private final long position;
    private final float score;

    public SearchResult(String text, long position, float score) {
        this.text = text;
        this.position = position;
        this.score = score;
    }

    public String getText() {
        return text;
    }

    public long getPosition() {
        return position;
    }

    public float getScore() {
        return score;
    }
}