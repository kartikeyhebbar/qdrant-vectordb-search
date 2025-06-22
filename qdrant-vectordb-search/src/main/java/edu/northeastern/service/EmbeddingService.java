package edu.northeastern.service;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

import java.io.IOException;

public class EmbeddingService implements AutoCloseable {
    private final ZooModel<String, float[]> model;
    private final Predictor<String, float[]> predictor;

    public EmbeddingService() throws ModelNotFoundException, MalformedModelException, IOException {
        System.out.println("Loading embedding model... This may take a few moments on first run.");

        Criteria<String, float[]> criteria = Criteria.builder()
                .setTypes(String.class, float[].class)
                .optApplication(Application.NLP.TEXT_EMBEDDING)
                .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/all-MiniLM-L6-v2")
                .optEngine("PyTorch")
                .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                .build();

        this.model = criteria.loadModel();
        this.predictor = model.newPredictor();

        System.out.println("Model loaded successfully!");
    }

    public float[] getEmbedding(String text) throws TranslateException {
        return predictor.predict(text);
    }

    @Override
    public void close() throws Exception {
        predictor.close();
        model.close();
    }
}