package edu.northeastern.service;

import edu.northeastern.config.QdrantConfig;
import edu.northeastern.model.SearchResult;
import edu.northeastern.model.TextChunk;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.PointIdFactory;
import io.qdrant.client.ValueFactory;
import io.qdrant.client.VectorsFactory;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class QdrantService {
    private final QdrantClient qdrantClient;

    public QdrantService() {
        this.qdrantClient = new QdrantClient(
                QdrantGrpcClient.newBuilder(QdrantConfig.QDRANT_HOST, QdrantConfig.QDRANT_PORT, true)
                        .withApiKey(QdrantConfig.QDRANT_API_KEY)
                        .withTimeout(Duration.ofSeconds(30))
                        .build()
        );
    }

    public void createCollection() throws ExecutionException, InterruptedException {
        // Check if collection exists
        List<String> collections = qdrantClient.listCollectionsAsync().get();
        boolean exists = collections.contains(QdrantConfig.COLLECTION_NAME);

        if (exists) {
            // Delete existing collection
            qdrantClient.deleteCollectionAsync(QdrantConfig.COLLECTION_NAME).get();
            System.out.println("Deleted existing collection: " + QdrantConfig.COLLECTION_NAME);
        }

        // Create new collection
        var vectorParams = Collections.VectorParams.newBuilder()
                .setSize(QdrantConfig.VECTOR_DIMENSION)
                .setDistance(Collections.Distance.Cosine)
                .build();

        qdrantClient.createCollectionAsync(
                QdrantConfig.COLLECTION_NAME,
                vectorParams
        ).get();

        System.out.println("Created collection: " + QdrantConfig.COLLECTION_NAME);
    }

    public void storeChunks(List<TextChunk> chunks, EmbeddingService embeddingService)
            throws ExecutionException, InterruptedException, Exception {
        int batchSize = 10;

        for (int i = 0; i < chunks.size(); i += batchSize) {
            int end = Math.min(i + batchSize, chunks.size());
            List<TextChunk> batch = chunks.subList(i, end);

            // Create points
            List<Points.PointStruct> points = new ArrayList<>();
            for (TextChunk chunk : batch) {
                // Get embedding for chunk
                float[] embedding = embeddingService.getEmbedding(chunk.getText());

                var point = Points.PointStruct.newBuilder()
                        .setId(PointIdFactory.id(chunk.getId()))
                        .setVectors(VectorsFactory.vectors(embedding))
                        .putPayload("text", ValueFactory.value(chunk.getText()))
                        .putPayload("position", ValueFactory.value(chunk.getPosition()))
                        .build();

                points.add(point);
            }

            // Upsert points
            qdrantClient.upsertAsync(QdrantConfig.COLLECTION_NAME, points).get();

            System.out.println("Stored batch " + (i/batchSize + 1) + " of " +
                    ((chunks.size() + batchSize - 1) / batchSize));
        }
    }

    public List<SearchResult> search(String query, int limit, EmbeddingService embeddingService)
            throws ExecutionException, InterruptedException, Exception {
        // Get embedding for query
        float[] queryEmbedding = embeddingService.getEmbedding(query);

        // Convert float[] to List<Float> for Qdrant
        List<Float> queryVector = new ArrayList<>();
        for (float f : queryEmbedding) {
            queryVector.add(f);
        }

        // Search in Qdrant
        var searchPoints = Points.SearchPoints.newBuilder()
                .setCollectionName(QdrantConfig.COLLECTION_NAME)
                .addAllVector(queryVector)
                .setLimit(limit)
                .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                .build();

        List<Points.ScoredPoint> searchResults = qdrantClient.searchAsync(searchPoints).get();

        // Convert to SearchResult objects
        return searchResults.stream()
                .map(point -> {
                    String text = point.getPayloadMap().get("text").getStringValue();
                    long position = point.getPayloadMap().get("position").getIntegerValue();
                    float score = point.getScore();
                    return new SearchResult(text, position, score);
                })
                .collect(Collectors.toList());
    }
}