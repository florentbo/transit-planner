package com.transit.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

public record StibWaitingTimesResponse(
        @JsonProperty("total_count") int totalCount,
        @JsonProperty("results") List<Result> results
) {

    public record Result(
            @JsonProperty("pointid") String pointId,
            @JsonProperty("lineid") String lineId,
            @JsonProperty("passingtimes") @JsonDeserialize(using = PassingTimesDeserializer.class) List<PassingTime> passingTimes
    ) {}

    public record PassingTime(
            @JsonProperty("destination") Destination destination,
            @JsonProperty("expectedArrivalTime") OffsetDateTime expectedArrivalTime,
            @JsonProperty("lineId") String lineId
    ) {}

    public record Destination(
            @JsonProperty("fr") String fr,
            @JsonProperty("nl") String nl
    ) {}

    static class PassingTimesDeserializer extends StdDeserializer<List<PassingTime>> {

        private static final ObjectMapper INNER_MAPPER;

        static {
            INNER_MAPPER = new ObjectMapper();
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            INNER_MAPPER.registerModule(javaTimeModule);
            INNER_MAPPER.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
            INNER_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

        public PassingTimesDeserializer() {
            super(List.class);
        }

        @Override
        public List<PassingTime> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String jsonString = p.getText();
            return INNER_MAPPER.readValue(jsonString, new TypeReference<List<PassingTime>>() {});
        }
    }
}
