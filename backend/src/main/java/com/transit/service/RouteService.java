package com.transit.service;

import com.transit.model.SavedRouteRequest;
import com.transit.model.SavedRouteResponse;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RouteService {

    private final Map<String, SavedRouteResponse> routes = new ConcurrentHashMap<>();

    public SavedRouteResponse createRoute(SavedRouteRequest request) {
        UUID id = UUID.randomUUID();
        SavedRouteResponse response = new SavedRouteResponse()
                .id(id)
                .name(request.getName())
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .createdAt(OffsetDateTime.now());

        routes.put(id.toString(), response);
        return response;
    }

    public List<SavedRouteResponse> getAllRoutes() {
        return new ArrayList<>(routes.values());
    }
}
