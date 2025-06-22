package edu.northeastern.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class EbookService {

    public String readEbook(String filename) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            if (is == null) {
                throw new IOException("File not found: " + filename);
            }

            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}