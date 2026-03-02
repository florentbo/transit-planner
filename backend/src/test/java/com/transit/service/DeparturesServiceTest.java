package com.transit.service;

import com.transit.client.StibApiClient;
import com.transit.client.StibApiException;
import com.transit.client.StibWaitingTimesResponse;
import com.transit.model.DeparturesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeparturesServiceTest {

    @Mock
    private StibApiClient stibApiClient;

    private DeparturesService departuresService;

    // Fixed clock: 2026-03-02T20:00:00+01:00
    private final Instant fixedInstant = Instant.parse("2026-03-02T19:00:00Z"); // 20:00 Brussels time
    private final ZoneId brusselsZone = ZoneId.of("Europe/Brussels");
    private final Clock fixedClock = Clock.fixed(fixedInstant, brusselsZone);

    @BeforeEach
    void setUp() {
        departuresService = new DeparturesService(stibApiClient, fixedClock);
    }

    @Test
    void returnsDeparturesForBothRoutes() {
        // Woest / line 51 result — departure in 4 min (20:04) and 9 min (20:09) Brussels time
        OffsetDateTime woestArrival1 = OffsetDateTime.parse("2026-03-02T20:04:00+01:00");
        OffsetDateTime woestArrival2 = OffsetDateTime.parse("2026-03-02T20:09:00+01:00");
        StibWaitingTimesResponse.PassingTime woestPt1 = new StibWaitingTimesResponse.PassingTime(
                new StibWaitingTimesResponse.Destination("Gare du Midi", "Zuidstation"), woestArrival1, "51");
        StibWaitingTimesResponse.PassingTime woestPt2 = new StibWaitingTimesResponse.PassingTime(
                new StibWaitingTimesResponse.Destination("Gare du Midi", "Zuidstation"), woestArrival2, "51");

        // Pannenhuis / line 6 result — departure in 3 min (20:03) and 8 min (20:08) Brussels time
        OffsetDateTime pannenhuisArrival1 = OffsetDateTime.parse("2026-03-02T20:03:00+01:00");
        OffsetDateTime pannenhuisArrival2 = OffsetDateTime.parse("2026-03-02T20:08:00+01:00");
        StibWaitingTimesResponse.PassingTime pannenhuisPt1 = new StibWaitingTimesResponse.PassingTime(
                new StibWaitingTimesResponse.Destination("Elisabeth", "Elisabeth"), pannenhuisArrival1, "6");
        StibWaitingTimesResponse.PassingTime pannenhuisPt2 = new StibWaitingTimesResponse.PassingTime(
                new StibWaitingTimesResponse.Destination("Elisabeth", "Elisabeth"), pannenhuisArrival2, "6");

        StibWaitingTimesResponse mockResponse = new StibWaitingTimesResponse(2, List.of(
                new StibWaitingTimesResponse.Result("5008", "51", List.of(woestPt1, woestPt2)),
                new StibWaitingTimesResponse.Result("8784", "6", List.of(pannenhuisPt1, pannenhuisPt2))
        ));

        when(stibApiClient.fetchWaitingTimes(anyList())).thenReturn(mockResponse);

        DeparturesResponse result = departuresService.getDepartures();

        assertThat(result.getRoutes()).hasSize(2);

        var woestRoute = result.getRoutes().get(0);
        assertThat(woestRoute.getStopName()).isEqualTo("Woest");
        assertThat(woestRoute.getLineNumber()).isEqualTo("51");
        assertThat(woestRoute.getDirection()).isEqualTo("Gare du Midi");
        assertThat(woestRoute.getDepartures()).hasSize(2);
        // Fixed clock is at 20:00:00 Brussels. Arrival at 20:04:00 = 4 minutes + 1 = 5
        assertThat(woestRoute.getDepartures().get(0).getMinutesUntilArrival()).isEqualTo(5);
        // Arrival at 20:09:00 = 9 minutes + 1 = 10
        assertThat(woestRoute.getDepartures().get(1).getMinutesUntilArrival()).isEqualTo(10);

        var pannenhuisRoute = result.getRoutes().get(1);
        assertThat(pannenhuisRoute.getStopName()).isEqualTo("Pannenhuis");
        assertThat(pannenhuisRoute.getLineNumber()).isEqualTo("6");
        assertThat(pannenhuisRoute.getDirection()).isEqualTo("Elisabeth");
        assertThat(pannenhuisRoute.getDepartures()).hasSize(2);
        // Arrival at 20:03:00 = 3 minutes + 1 = 4
        assertThat(pannenhuisRoute.getDepartures().get(0).getMinutesUntilArrival()).isEqualTo(4);
        // Arrival at 20:08:00 = 8 minutes + 1 = 9
        assertThat(pannenhuisRoute.getDepartures().get(1).getMinutesUntilArrival()).isEqualTo(9);
    }

    @Test
    void handlesStibApiFailure() {
        when(stibApiClient.fetchWaitingTimes(anyList())).thenThrow(new StibApiException("Connection refused"));

        assertThatThrownBy(() -> departuresService.getDepartures())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("STIB API unavailable");
    }

    @Test
    void filtersDeparturesByDirection() {
        // Line 6 from Pannenhuis goes both to Elisabeth and Roi Baudouin — only Elisabeth should appear
        OffsetDateTime arrival1 = OffsetDateTime.parse("2026-03-02T20:05:00+01:00");
        OffsetDateTime arrival2 = OffsetDateTime.parse("2026-03-02T20:10:00+01:00");

        StibWaitingTimesResponse.PassingTime toElisabeth = new StibWaitingTimesResponse.PassingTime(
                new StibWaitingTimesResponse.Destination("Elisabeth", "Elisabeth"), arrival1, "6");
        StibWaitingTimesResponse.PassingTime toRoiBaudouin = new StibWaitingTimesResponse.PassingTime(
                new StibWaitingTimesResponse.Destination("Roi Baudouin", "Koning Boudewijn"), arrival2, "6");

        StibWaitingTimesResponse mockResponse = new StibWaitingTimesResponse(2, List.of(
                // No Woest result in this test — Woest route will just have 0 departures
                new StibWaitingTimesResponse.Result("8784", "6", List.of(toElisabeth, toRoiBaudouin))
        ));

        when(stibApiClient.fetchWaitingTimes(anyList())).thenReturn(mockResponse);

        DeparturesResponse result = departuresService.getDepartures();

        var pannenhuisRoute = result.getRoutes().stream()
                .filter(r -> "Pannenhuis".equals(r.getStopName()))
                .findFirst()
                .orElseThrow();

        assertThat(pannenhuisRoute.getDepartures()).hasSize(1);
        assertThat(pannenhuisRoute.getDepartures().get(0).getDestination()).isEqualTo("Elisabeth");
    }
}
