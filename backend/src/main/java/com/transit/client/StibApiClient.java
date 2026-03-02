package com.transit.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StibApiClient {

    private final String baseUrl;
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public StibApiClient(
            @Value("${stib.api.base-url}") String baseUrl,
            @Value("${stib.api.key}") String apiKey
    ) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        this.objectMapper.registerModule(javaTimeModule);
        this.objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public StibWaitingTimesResponse fetchWaitingTimes(List<String> pointIds) {
        String whereClause = pointIds.stream()
                .map(id -> "pointid=" + id)
                .collect(Collectors.joining(" or "));

        String url = baseUrl + "/catalog/datasets/waiting-time-rt-production/records"
                + "?where=" + whereClause.replace(" ", "%20")
                + "&apikey=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new StibApiException("STIB API returned HTTP " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), StibWaitingTimesResponse.class);
        } catch (StibApiException e) {
            throw e;
        } catch (Exception e) {
            throw new StibApiException("Failed to fetch waiting times from STIB API: " + e.getMessage(), e);
        }
    }
}
