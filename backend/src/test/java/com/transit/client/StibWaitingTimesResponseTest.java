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
    void shouldDeserializeRealStibResponseWithMessageField() throws Exception {
        // Real STIB response — some passingtimes entries have an extra "message" field
        String json = """
                {
                  "total_count": 2,
                  "results": [
                    {
                      "pointid": "5008",
                      "lineid": "51",
                      "passingtimes": "[{\\"destination\\": {\\"fr\\": \\"BELGICA\\", \\"nl\\": \\"BELGICA\\"}, \\"expectedArrivalTime\\": \\"2026-03-02T21:38:00+01:00\\", \\"lineId\\": \\"51\\"}, {\\"destination\\": {\\"fr\\": \\"BELGICA\\", \\"nl\\": \\"BELGICA\\"}, \\"expectedArrivalTime\\": \\"2026-03-02T21:53:00+01:00\\", \\"lineId\\": \\"51\\", \\"message\\": {\\"en\\": \\"Theoretical time\\", \\"fr\\": \\"Temps théorique\\", \\"nl\\": \\"Theoretische tijd\\"}}]"
                    },
                    {
                      "pointid": "8784",
                      "lineid": "6",
                      "passingtimes": "[{\\"destination\\": {\\"fr\\": \\"ELISABETH\\", \\"nl\\": \\"ELISABETH\\"}, \\"expectedArrivalTime\\": \\"2026-03-02T21:35:00+01:00\\", \\"lineId\\": \\"6\\"}]"
                    }
                  ]
                }
                """;

        StibWaitingTimesResponse response = objectMapper.readValue(json, StibWaitingTimesResponse.class);

        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.results()).hasSize(2);

        StibWaitingTimesResponse.Result woestResult = response.results().get(0);
        assertThat(woestResult.pointId()).isEqualTo("5008");
        assertThat(woestResult.lineId()).isEqualTo("51");
        assertThat(woestResult.passingTimes()).hasSize(2);
        assertThat(woestResult.passingTimes().get(0).destination().fr()).isEqualTo("BELGICA");
        // Second entry has the "message" field — should be ignored, not cause failure
        assertThat(woestResult.passingTimes().get(1).destination().fr()).isEqualTo("BELGICA");

        StibWaitingTimesResponse.Result pannenhuisResult = response.results().get(1);
        assertThat(pannenhuisResult.pointId()).isEqualTo("8784");
        assertThat(pannenhuisResult.passingTimes()).hasSize(1);
        assertThat(pannenhuisResult.passingTimes().get(0).destination().fr()).isEqualTo("ELISABETH");
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
