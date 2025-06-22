package edu.northeastern.util;

import edu.northeastern.config.QdrantConfig;
import edu.northeastern.model.SearchResult;
import edu.northeastern.model.TextChunk;

import java.util.ArrayList;
import java.util.List;

public class TextProcessor {

    public List<TextChunk> createChunks(String text) {
        List<TextChunk> chunks = new ArrayList<>();
        long id = 0;

        for (int i = 0; i < text.length(); i += QdrantConfig.CHUNK_SIZE - QdrantConfig.CHUNK_OVERLAP) {
            int end = Math.min(i + QdrantConfig.CHUNK_SIZE, text.length());
            String chunkText = text.substring(i, end);

            // Try to break at sentence boundary
            if (end < text.length()) {
                int lastPeriod = chunkText.lastIndexOf('.');
                if (lastPeriod > QdrantConfig.CHUNK_SIZE / 2) {
                    end = i + lastPeriod + 1;
                    chunkText = text.substring(i, end);
                }
            }

            chunks.add(new TextChunk(id++, chunkText.trim(), i));

            if (end >= text.length()) {
                break;
            }
        }

        return chunks;
    }

    public void displayResults(List<SearchResult> results) {
        if (results.isEmpty()) {
            System.out.println("No results found.");
            return;
        }

        System.out.println("\nSearch Results:");
        System.out.println("================");

        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            System.out.println("\nResult " + (i + 1) + " (Score: " +
                    String.format("%.4f", result.getScore()) + "):");
            System.out.println("Position in book: character " + result.getPosition());
            System.out.println("Text excerpt:");
            System.out.println("\"" + truncateText(result.getText(), 200) + "...\"");
            System.out.println("-".repeat(80));
        }
    }

    private String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength).trim();
    }
}