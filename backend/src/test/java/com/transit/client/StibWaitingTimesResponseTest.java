package com.transit.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class StibWaitingTimesResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
    }

    @Test
    void shouldDeserializeStibApiResponse() throws Exception {
        String json = """
                {
                  "total_count": 1,
                  "results": [
                    {
                      "pointid": "8784",
                      "lineid": "6",
                      "passingtimes": "[{\\"destination\\": {\\"fr\\": \\"ELISABETH\\", \\"nl\\": \\"ELISABETH\\"}, \\"expectedArrivalTime\\": \\"2024-12-27T18:51:00+01:00\\", \\"lineId\\": \\"6\\"}]"
                    }
                  ]
                }
                """;

        StibWaitingTimesResponse response = objectMapper.readValue(json, StibWaitingTimesResponse.class);

        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.results()).hasSize(1);

        StibWaitingTimesResponse.Result result = response.results().get(0);
        assertThat(result.pointId()).isEqualTo("8784");
        assertThat(result.lineId()).isEqualTo("6");
        assertThat(result.passingTimes()).hasSize(1);

        StibWaitingTimesResponse.PassingTime passingTime = result.passingTimes().get(0);
        assertThat(passingTime.destination().fr()).isEqualTo("ELISABETH");
        assertThat(passingTime.destination().nl()).isEqualTo("ELISABETH");
        assertThat(passingTime.lineId()).isEqualTo("6");

        OffsetDateTime expectedTime = OffsetDateTime.of(2024, 12, 27, 18, 51, 0, 0, ZoneOffset.ofHours(1));
        assertThat(passingTime.expectedArrivalTime()).isEqualTo(expectedTime);
        assertThat(passingTime.expectedArrivalTime().getOffset()).isEqualTo(ZoneOffset.ofHours(1));
    }

    @Test
    void shouldDeserializeMultipleResults() throws Exception {
        String json = """
                {
                  "total_count": 2,
                  "results": [
                    {
                      "pointid": "8784",
                      "lineid": "51",
                      "passingtimes": "[{\\"destination\\": {\\"fr\\": \\"GARE DU MIDI\\", \\"nl\\": \\"ZUIDSTATION\\"}, \\"expectedArrivalTime\\": \\"2024-12-27T18:51:00+01:00\\", \\"lineId\\": \\"51\\"}, {\\"destination\\": {\\"fr\\": \\"GARE DU MIDI\\", \\"nl\\": \\"ZUIDSTATION\\"}, \\"expectedArrivalTime\\": \\"2024-12-27T18:57:00+01:00\\", \\"lineId\\": \\"51\\"}]"
                    },
                    {
                      "pointid": "5008",
                      "lineid": "6",
                      "passingtimes": "[{\\"destination\\": {\\"fr\\": \\"ELISABETH\\", \\"nl\\": \\"ELISABETH\\"}, \\"expectedArrivalTime\\": \\"2024-12-27T19:02:00+01:00\\", \\"lineId\\": \\"6\\"}]"
                    }
                  ]
                }
                """;

        StibWaitingTimesResponse response = objectMapper.readValue(json, StibWaitingTimesResponse.class);

        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.results()).hasSize(2);

        StibWaitingTimesResponse.Result firstResult = response.results().get(0);
        assertThat(firstResult.pointId()).isEqualTo("8784");
        assertThat(firstResult.lineId()).isEqualTo("51");
        assertThat(firstResult.passingTimes()).hasSize(2);
        assertThat(firstResult.passingTimes().get(0).destination().fr()).isEqualTo("GARE DU MIDI");
        assertThat(firstResult.passingTimes().get(1).destination().nl()).isEqualTo("ZUIDSTATION");

        StibWaitingTimesResponse.Result secondResult = response.results().get(1);
        assertThat(secondResult.pointId()).isEqualTo("5008");
        assertThat(secondResult.passingTimes()).hasSize(1);
    }

    @Test
    void shouldPreserveTimezoneOffset() throws Exception {
        String json = """
                {
                  "total_count": 1,
                  "results": [
                    {
                      "pointid": "8784",
                      "lineid": "6",
                      "passingtimes": "[{\\"destination\\": {\\"fr\\": \\"ELISABETH\\", \\"nl\\": \\"ELISABETH\\"}, \\"expectedArrivalTime\\": \\"2024-12-27T18:51:00+01:00\\", \\"lineId\\": \\"6\\"}]"
                    }
                  ]
                }
                """;

        StibWaitingTimesResponse response = objectMapper.readValue(json, StibWaitingTimesResponse.class);
        OffsetDateTime arrivalTime = response.results().get(0).passingTimes().get(0).expectedArrivalTime();

        assertThat(arrivalTime.getOffset()).isEqualTo(ZoneOffset.ofHours(1));
        assertThat(arrivalTime.getHour()).isEqualTo(18);
        assertThat(arrivalTime.getMinute()).isEqualTo(51);
    }
}
