package edu.northeastern.model;

public class TextChunk {
    private final long id;
    private final String text;
    private final int position;

    public TextChunk(long id, String text, int position) {
        this.id = id;
        this.text = text;
        this.position = position;
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public int getPosition() {
        return position;
    }
}