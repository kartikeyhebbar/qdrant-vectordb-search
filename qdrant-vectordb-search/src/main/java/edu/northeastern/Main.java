package edu.northeastern;

import edu.northeastern.model.SearchResult;
import edu.northeastern.model.TextChunk;
import edu.northeastern.service.EbookService;
import edu.northeastern.service.EmbeddingService;
import edu.northeastern.service.QdrantService;
import edu.northeastern.util.TextProcessor;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (EmbeddingService embeddingService = new EmbeddingService()) {
            // Initialize services
            QdrantService qdrantService = new QdrantService();
            EbookService ebookService = new EbookService();
            TextProcessor textProcessor = new TextProcessor();

            System.out.println("Starting eBook Vector Search Application...");

            // Read and process the eBook
            String ebookContent = ebookService.readEbook("ebook.txt");
            List<TextChunk> chunks = textProcessor.createChunks(ebookContent);

            System.out.println("Created " + chunks.size() + " chunks from the eBook.");

            // Create collection and store vectors
            qdrantService.createCollection();
            qdrantService.storeChunks(chunks, embeddingService);

            System.out.println("Successfully stored all chunks in Qdrant.");

            // Interactive search
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\nEnter search query (or 'quit' to exit): ");
                String query = scanner.nextLine();

                if (query.equalsIgnoreCase("quit")) {
                    break;
                }

                List<SearchResult> results = qdrantService.search(query, 5, embeddingService);
                textProcessor.displayResults(results);
            }

            scanner.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}