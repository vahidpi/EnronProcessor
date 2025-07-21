package org.enronprocessor.enronprocessor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Service
public class EnronEmailServiceImpl implements EnronEmailService {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Logger logger = LoggerFactory.getLogger(EnronEmailServiceImpl.class);


    boolean ingestionFinished = true;
    boolean ingestionStarted = false;


    final Map<String, Integer> senderCounts = new ConcurrentHashMap<>();
    private final Path maildirPath;

    public EnronEmailServiceImpl(@Value("${enron.maildir.path}") String path) {
        this.maildirPath = Paths.get(path);
    }

    @Override
    public void startIngestion() {
        if (ingestionStarted && !ingestionFinished) {
            throw new IllegalStateException("Ingestion is already in progress.");
        }

        ingestionStarted = true;
        ingestionFinished = false;
        senderCounts.clear();

        executor.submit(() -> {
            try (Stream<Path> paths = Files.walk(maildirPath)) {
                paths.filter(Files::isRegularFile).forEach(this::processFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to process emails", e);
            } finally {
                ingestionFinished = true;
            }
        });
    }

    @Override
    public boolean isIngestionFinished() {
        if (!ingestionStarted) {
            throw new IllegalStateException("Ingestion has not been started yet.");
        }
        return ingestionFinished;
    }

    @Override
    public List<Map.Entry<String, Integer>> getTopSenders() {
        if (!ingestionStarted) {
            throw new IllegalStateException("Ingestion has not been started yet.");
        }

        return senderCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .toList();
    }

    @Override
    public int getTotalMessages() {
        if (!ingestionStarted) {
            throw new IllegalStateException("Ingestion has not been started yet.");
        }
        return senderCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    private void processFile(Path file) {
        try {
            String sender = extractSender(file);
            if (sender != null && !sender.isEmpty()) {
                senderCounts.merge(sender.toLowerCase(), 1, Integer::sum);
            }
        } catch (IOException e) {
            logger.error("Failed to process file: {}", file.toAbsolutePath(), e);
        }
    }

    private String extractSender(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().startsWith("from:")) {
                    return line.substring(5).trim();
                }
                if (line.isEmpty()) break;
            }
        }
        return null;
    }
}