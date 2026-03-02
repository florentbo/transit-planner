package com.transit;

import com.transit.client.StibApiClient;
import com.transit.model.Departure;
import com.transit.model.DeparturesResponse;
import com.transit.model.RouteDepartures;
import com.transit.service.DeparturesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DeparturesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeparturesService departuresService;

    @MockitoBean
    private StibApiClient stibApiClient;

    @Test
    void endpointReturnsJsonCorrectly() throws Exception {
        var response = new DeparturesResponse(
                OffsetDateTime.parse("2026-03-02T20:00:00+01:00"),
                List.of(
                        new RouteDepartures("Woest", "51", "Gare du Midi",
                                List.of(new Departure(5, "BELGICA"))),
                        new RouteDepartures("Pannenhuis", "6", "Elisabeth",
                                List.of(new Departure(3, "ELISABETH")))));

        when(departuresService.getDepartures()).thenReturn(response);

        mockMvc.perform(get("/api/departures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastUpdated").exists())
                .andExpect(jsonPath("$.routes.length()").value(2))
                .andExpect(jsonPath("$.routes[0].stopName").value("Woest"))
                .andExpect(jsonPath("$.routes[0].lineNumber").value("51"))
                .andExpect(jsonPath("$.routes[0].departures[0].minutesUntilArrival").value(5))
                .andExpect(jsonPath("$.routes[1].stopName").value("Pannenhuis"))
                .andExpect(jsonPath("$.routes[1].departures[0].destination").value("ELISABETH"));
    }

    @Test
    void endpointReturns502OnServiceFailure() throws Exception {
        when(departuresService.getDepartures())
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "STIB API unavailable"));

        mockMvc.perform(get("/api/departures"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("STIB_API_ERROR"));
    }
}
