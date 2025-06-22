package edu.northeastern.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class QdrantConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = QdrantConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find application.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration properties", e);
        }
    }

    // Qdrant settings
    public static final String QDRANT_HOST = properties.getProperty("qdrant.host");
    public static final int QDRANT_PORT = Integer.parseInt(properties.getProperty("qdrant.port", "6334"));
    public static final String QDRANT_API_KEY = properties.getProperty("qdrant.api.key");
    public static final String COLLECTION_NAME = properties.getProperty("qdrant.collection.name", "ebook_collection");

    // Embedding settings
    public static final int VECTOR_DIMENSION = Integer.parseInt(properties.getProperty("embedding.vector.dimension", "384"));

    // Text processing settings
    public static final int CHUNK_SIZE = Integer.parseInt(properties.getProperty("text.chunk.size", "500"));
    public static final int CHUNK_OVERLAP = Integer.parseInt(properties.getProperty("text.chunk.overlap", "100"));

    // Validation
    static {
        if (QDRANT_HOST == null || QDRANT_HOST.isEmpty()) {
            throw new RuntimeException("qdrant.host is not configured in application.properties");
        }
        if (QDRANT_API_KEY == null || QDRANT_API_KEY.isEmpty()) {
            throw new RuntimeException("qdrant.api.key is not configured in application.properties");
        }
    }
}