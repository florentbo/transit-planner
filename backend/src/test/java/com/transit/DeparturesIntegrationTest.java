package com.transit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DeparturesIntegrationTest {

    static WireMockServer wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    @Autowired
    private MockMvc mockMvc;

    // Real STIB API response captured from production
    static final String REAL_STIB_RESPONSE = """
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
                  "passingtimes": "[{\\"destination\\": {\\"fr\\": \\"ELISABETH\\", \\"nl\\": \\"ELISABETH\\"}, \\"expectedArrivalTime\\": \\"2026-03-02T21:35:00+01:00\\", \\"lineId\\": \\"6\\"}, {\\"destination\\": {\\"fr\\": \\"ELISABETH\\", \\"nl\\": \\"ELISABETH\\"}, \\"expectedArrivalTime\\": \\"2026-03-02T21:45:00+01:00\\", \\"lineId\\": \\"6\\"}]"
                }
              ]
            }
            """;

    @BeforeAll
    static void startWireMock() {
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @BeforeEach
    void resetWireMock() {
        wireMock.resetAll();
    }

    @DynamicPropertySource
    static void configureStibApi(DynamicPropertyRegistry registry) {
        registry.add("stib.api.base-url", () -> wireMock.baseUrl() + "/api/explore/v2.1");
        registry.add("stib.api.key", () -> "test-key");
    }

    @Test
    void getDeparturesReturnsRealStibData() throws Exception {
        wireMock.stubFor(
                com.github.tomakehurst.wiremock.client.WireMock.get(
                        urlPathEqualTo("/api/explore/v2.1/catalog/datasets/waiting-time-rt-production/records"))
                        .willReturn(okJson(REAL_STIB_RESPONSE)));

        mockMvc.perform(get("/api/departures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastUpdated").exists())
                .andExpect(jsonPath("$.routes").isArray())
                .andExpect(jsonPath("$.routes.length()").value(2))
                // Woest / line 51
                .andExpect(jsonPath("$.routes[0].stopName").value("Woest"))
                .andExpect(jsonPath("$.routes[0].lineNumber").value("51"))
                .andExpect(jsonPath("$.routes[0].direction").value("Gare du Midi"))
                .andExpect(jsonPath("$.routes[0].departures").isArray())
                .andExpect(jsonPath("$.routes[0].departures.length()").value(2))
                .andExpect(jsonPath("$.routes[0].departures[0].destination").value("BELGICA"))
                .andExpect(jsonPath("$.routes[0].departures[0].minutesUntilArrival").isNumber())
                // Pannenhuis / line 6
                .andExpect(jsonPath("$.routes[1].stopName").value("Pannenhuis"))
                .andExpect(jsonPath("$.routes[1].lineNumber").value("6"))
                .andExpect(jsonPath("$.routes[1].direction").value("Elisabeth"))
                .andExpect(jsonPath("$.routes[1].departures").isArray())
                .andExpect(jsonPath("$.routes[1].departures.length()").value(2))
                .andExpect(jsonPath("$.routes[1].departures[0].destination").value("ELISABETH"));
    }

    @Test
    void getDeparturesReturns502WhenStibApiDown() throws Exception {
        wireMock.stubFor(
                com.github.tomakehurst.wiremock.client.WireMock.get(
                        urlPathEqualTo("/api/explore/v2.1/catalog/datasets/waiting-time-rt-production/records"))
                        .willReturn(serverError()));

        mockMvc.perform(get("/api/departures"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("STIB_API_ERROR"));
    }
}
