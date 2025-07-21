package org.enronprocessor.enronprocessor.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

class EnronEmailServiceImplTest {

    private Path tempDir;
    private EnronEmailServiceImpl emailService;

    @BeforeEach
    void setup() throws IOException {
        tempDir = Files.createTempDirectory("enronTestMaildir");
        emailService = new EnronEmailServiceImpl(tempDir.toString());
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.walk(tempDir)
                .map(Path::toFile)
                .forEach(java.io.File::delete);
    }

    @Test
    void testStartIngestion_shouldStartIngestionSuccessfully() throws Exception {
        Path file = Files.createFile(tempDir.resolve("email1.txt"));
        Files.writeString(file, "From: sender@example.com\nSubject: Test\n\nBody");
        emailService.startIngestion();
        waitUntil(() -> emailService.isIngestionFinished());

        assertTrue(emailService.isIngestionFinished());
        assertEquals(1, emailService.getTotalMessages());
    }

    @Test
    void testStartIngestion_shouldThrowExceptionIfAlreadyIngesting() {
        emailService.startIngestion();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            emailService.startIngestion();
        });

        assertEquals("Ingestion is already in progress.", exception.getMessage());
    }

    @Test
    void testStartIngestion_shouldThrowExceptionIfIngestionNotStarted() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            emailService.isIngestionFinished();
        });

        assertEquals("Ingestion has not been started yet.", exception.getMessage());
    }

    @Test
    void testIsIngestionFinished_shouldReturnFalseIfIngestionIsOngoing() {
        Path file = tempDir.resolve("email.txt");
        try {
            Files.writeString(file, "From: test@example.com\n\nHello");
        } catch (IOException e) {
            fail("Failed to write test email file", e);
        }
        emailService.startIngestion();
        boolean finished = emailService.isIngestionFinished();

        assertFalse(finished);
    }

    @Test
    void testIsIngestionFinished_ShouldReturnTrueWhenIngestionIsFinished() throws Exception {
        Path file = tempDir.resolve("email.txt");
        Files.writeString(file, "From: someone@enron.com\n\nBody");

        emailService.startIngestion();
        waitUntil(() -> emailService.isIngestionFinished());

        assertTrue(emailService.isIngestionFinished());
    }

    @Test
    void testGetTopSenders_shouldThrowExceptionIfIngestionNotStarted() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            emailService.getTopSenders();
        });

        assertEquals("Ingestion has not been started yet.", exception.getMessage());
    }

    @Test
    void testGetTopSenders_shouldReturnTopSendersOrdered() {
        // Simulate ingestion has started and finished
        emailService.ingestionStarted = true;
        emailService.ingestionFinished = true;
        emailService.senderCounts.put("alice@example.com", 5);
        emailService.senderCounts.put("bob@example.com", 10);
        emailService.senderCounts.put("carol@example.com", 7);

        List<Map.Entry<String, Integer>> topSenders = emailService.getTopSenders();

        assertEquals(3, topSenders.size());
        assertEquals("bob@example.com", topSenders.get(0).getKey());
        assertEquals(10, topSenders.get(0).getValue());
        assertEquals("carol@example.com", topSenders.get(1).getKey());
        assertEquals("alice@example.com", topSenders.get(2).getKey());
    }

    @Test
    void testGetTotalMessages_shouldThrowExceptionIfIngestionNotStarted() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            emailService.getTotalMessages();
        });

        assertEquals("Ingestion has not been started yet.", exception.getMessage());
    }

    @Test
    void testGetTotalMessages_shouldReturnCorrectTotal() {
        // Simulate ingestion has started and finished
        emailService.ingestionStarted = true;
        emailService.ingestionFinished = true;
        emailService.senderCounts.put("alice@example.com", 3);
        emailService.senderCounts.put("bob@example.com", 5);
        emailService.senderCounts.put("carol@example.com", 2);

        int total = emailService.getTotalMessages();

        assertEquals(10, total);
    }

    private void waitUntil(BooleanSupplier condition) throws InterruptedException, TimeoutException {
        long deadline = System.currentTimeMillis() + (long) 2000;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() > deadline) {
                throw new TimeoutException("Condition was not met in time.");
            }
            Thread.sleep(100);
        }
    }


}