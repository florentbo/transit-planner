package com.transit.service;

import com.transit.client.StibApiClient;
import com.transit.model.DeparturesResponse;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeparturesServiceTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private DeparturesService departuresService;

    // Fixed clock: 2026-03-02T20:00:00+01:00 (Brussels)
    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2026-03-02T19:00:00Z"),
            ZoneId.of("Europe/Brussels"));

    // Real STIB API response captured from production — includes message field on some entries
    static final String REAL_STIB_RESPONSE = """
            {
              "total_count": 2,
              "results": [
                {
                  "pointid": "5008",
                  "lineid": "51",
                  "passingtimes": "[{\\"destination\\": {\\"fr\\": \\"BELGICA\\", \\"nl\\": \\"BELGICA\\"}, \\"expectedArrivalTime\\": \\"2026-03-02T20:08:00+01:00\\", \\"lineId\\": \\"51\\"}, {\\"destination\\": {\\"fr\\": \\"BELGICA\\", \\"nl\\": \\"BELGICA\\"}, \\"expectedArrivalTime\\": \\"2026-03-02T20:15:00+01:00\\", \\"lineId\\": \\"51\\", \\"message\\": {\\"en\\": \\"Theoretical time\\", \\"fr\\": \\"Temps théorique\\", \\"nl\\": \\"Theoretische tijd\\"}}]"
                },
                {
                  "pointid": "8784",
                  "lineid": "6",
                  "passingtimes": "[{\\"destination\\": {\\"fr\\": \\"ELISABETH\\", \\"nl\\": \\"ELISABETH\\"}, \\"expectedArrivalTime\\": \\"2026-03-02T20:05:00+01:00\\", \\"lineId\\": \\"6\\"}]"
                }
              ]
            }
            """;

    @BeforeEach
    void setUp() {
        var stibApiClient = new StibApiClient(
                wireMock.baseUrl() + "/api/explore/v2.1",
                "test-key");
        departuresService = new DeparturesService(stibApiClient, fixedClock);
    }

    @Test
    void returnsDeparturesFromRealStibResponse() {
        wireMock.stubFor(get(urlPathEqualTo(
                "/api/explore/v2.1/catalog/datasets/waiting-time-rt-production/records"))
                .willReturn(okJson(REAL_STIB_RESPONSE)));

        DeparturesResponse result = departuresService.getDepartures();

        assertThat(result.getLastUpdated()).isNotNull();
        assertThat(result.getRoutes()).hasSize(2);

        // Woest / line 51 — 2 departures (including one with message field)
        var woest = result.getRoutes().getFirst();
        assertThat(woest.getStopName()).isEqualTo("Woest");
        assertThat(woest.getLineNumber()).isEqualTo("51");
        assertThat(woest.getDirection()).isEqualTo("Gare du Midi");
        assertThat(woest.getDepartures()).hasSize(2);
        // Clock at 20:00, arrival at 20:08 → 8 min + 1 = 9
        assertThat(woest.getDepartures().get(0).getMinutesUntilArrival()).isEqualTo(9);
        // Clock at 20:00, arrival at 20:15 → 15 min + 1 = 16
        assertThat(woest.getDepartures().get(1).getMinutesUntilArrival()).isEqualTo(16);

        // Pannenhuis / line 6 — 1 departure
        var pannenhuis = result.getRoutes().get(1);
        assertThat(pannenhuis.getStopName()).isEqualTo("Pannenhuis");
        assertThat(pannenhuis.getLineNumber()).isEqualTo("6");
        assertThat(pannenhuis.getDirection()).isEqualTo("Elisabeth");
        assertThat(pannenhuis.getDepartures()).hasSize(1);
        assertThat(pannenhuis.getDepartures().getFirst().getDestination()).isEqualTo("ELISABETH");
        // Clock at 20:00, arrival at 20:05 → 5 min + 1 = 6
        assertThat(pannenhuis.getDepartures().getFirst().getMinutesUntilArrival()).isEqualTo(6);
    }

    @Test
    void throwsWhenStibApiDown() {
        wireMock.stubFor(get(urlPathEqualTo(
                "/api/explore/v2.1/catalog/datasets/waiting-time-rt-production/records"))
                .willReturn(serverError()));

        assertThatThrownBy(() -> departuresService.getDepartures())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("STIB API unavailable");
    }
}
