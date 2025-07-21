package org.enronprocessor.enronprocessor.service;

import java.util.List;
import java.util.Map;

public interface EnronEmailService {
    void startIngestion();
    boolean isIngestionFinished();
    int getTotalMessages();
    List<Map.Entry<String, Integer>> getTopSenders();
}
