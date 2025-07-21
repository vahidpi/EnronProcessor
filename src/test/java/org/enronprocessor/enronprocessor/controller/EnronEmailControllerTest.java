package org.enronprocessor.enronprocessor.controller;

import org.enronprocessor.enronprocessor.service.EnronEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.AbstractMap;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EnronEmailControllerTest {
    private EnronEmailService emailService;
    private EnronEmailController controller;

    @BeforeEach
    void setUp() {
        emailService = mock(EnronEmailService.class);
        controller = new EnronEmailController(emailService);
    }

    @Test
    void testStart_shouldReturnOkWhenIngestionStarts() {
        ResponseEntity<String> response = controller.startIngestion();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Ingestion started.", response.getBody());
        verify(emailService, times(1)).startIngestion();
    }

    @Test
    void testStart_shouldReturnConflictWhenIngestionAlreadyInProgress() {
        doThrow(new IllegalStateException("Ingestion is already in progress."))
                .when(emailService).startIngestion();
        ResponseEntity<String> response = controller.startIngestion();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Ingestion is already in progress.", response.getBody());
    }

    @Test
    void testStatus_shouldReturnStatusOk() {
        when(emailService.isIngestionFinished()).thenReturn(true);
        when(emailService.getTotalMessages()).thenReturn(42);
        ResponseEntity<Map<String, Object>> response = controller.isIngestionFinished();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("finished"));
        assertEquals(42, response.getBody().get("messagesProcessed"));
    }

    @Test
    void testStatus_shouldReturnBadRequestOnIllegalStateException() {
        when(emailService.isIngestionFinished()).thenThrow(new IllegalStateException("Ingestion not started."));
        ResponseEntity<Map<String, Object>> response = controller.isIngestionFinished();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ingestion not started.", response.getBody().get("error"));
    }

    @Test
    void testTopSenders_shouldReturnTopSendersSuccessfully() {
        List<Map.Entry<String, Integer>> mockSenders = List.of(
                new AbstractMap.SimpleEntry<>("sender1@example.com", 10),
                new AbstractMap.SimpleEntry<>("sender2@example.com", 8)
        );
        when(emailService.getTopSenders()).thenReturn(mockSenders);
        ResponseEntity<?> response = controller.getTopSenders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(List.class, response.getBody());
        List<?> body = (List<?>) response.getBody();
        assertEquals(2, body.size());
    }

    @Test
    void testTopSenders_shouldReturnBadRequestOnIllegalStateException() {
        when(emailService.getTopSenders()).thenThrow(new IllegalStateException("Ingestion not finished yet."));
        ResponseEntity<?> response = controller.getTopSenders();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        assertEquals("Ingestion not finished yet.", ((Map<?, ?>) response.getBody()).get("error"));
    }
}
