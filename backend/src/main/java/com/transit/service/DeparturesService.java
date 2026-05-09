package com.transit.service;

import com.transit.client.StibApiClient;
import com.transit.client.StibApiException;
import com.transit.client.StibWaitingTimesResponse;
import com.transit.model.Departure;
import com.transit.model.DeparturesResponse;
import com.transit.model.RouteDepartures;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeparturesService {

    private record CommutRoute(String pointId, String stopName, String lineId, String direction) {}

    private static final CommutRoute WOEST = new CommutRoute("5008", "Woest", "51", "Gare du Midi");
    private static final CommutRoute PANNENHUIS = new CommutRoute("8784", "Pannenhuis", "6", "Elisabeth");
    private static final List<CommutRoute> COMMUTE_ROUTES = List.of(WOEST, PANNENHUIS);

    private static final Duration CACHE_TTL = Duration.ofSeconds(15);

    private final StibApiClient stibApiClient;
    private final Clock clock;

    private volatile StibWaitingTimesResponse cachedResponse;
    private volatile Instant cachedAt;

    public DeparturesService(StibApiClient stibApiClient, Clock clock) {
        this.stibApiClient = stibApiClient;
        this.clock = clock;
    }

    public DeparturesResponse getDepartures() {
        StibWaitingTimesResponse stibResponse = fetchWaitingTimesCached();

        OffsetDateTime now = OffsetDateTime.now(clock);

        List<RouteDepartures> routes = new ArrayList<>();
        for (CommutRoute route : COMMUTE_ROUTES) {
            List<Departure> departures = stibResponse.results().stream()
                    .filter(result -> route.pointId().equals(result.pointId()) && route.lineId().equals(result.lineId()))
                    .flatMap(result -> result.passingTimes().stream())
                    .filter(passingTime -> passingTime.destination() != null)
                    .map(passingTime -> {
                        int minutes = (int) ChronoUnit.MINUTES.between(now, passingTime.expectedArrivalTime()) + 1;
                        String dest = passingTime.destination().fr();
                        return new Departure(minutes, dest);
                    })
                    .toList();

            routes.add(new RouteDepartures(route.stopName(), route.lineId(), route.direction(), departures));
        }

        return new DeparturesResponse(now, routes);
    }

    private synchronized StibWaitingTimesResponse fetchWaitingTimesCached() {
        Instant now = clock.instant();
        if (cachedResponse != null && Duration.between(cachedAt, now).compareTo(CACHE_TTL) < 0) {
            return cachedResponse;
        }
        try {
            List<String> pointIds = COMMUTE_ROUTES.stream().map(CommutRoute::pointId).toList();
            StibWaitingTimesResponse fresh = stibApiClient.fetchWaitingTimes(pointIds);
            cachedResponse = fresh;
            cachedAt = now;
            return fresh;
        } catch (StibApiException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "STIB API unavailable: " + e.getMessage(), e);
        }
    }
}
