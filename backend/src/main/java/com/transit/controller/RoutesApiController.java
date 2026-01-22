package com.transit.controller;

import com.transit.api.RoutesApi;
import com.transit.model.SavedRouteRequest;
import com.transit.model.SavedRouteResponse;
import com.transit.service.RouteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RoutesApiController implements RoutesApi {

    private final RouteService routeService;

    public RoutesApiController(RouteService routeService) {
        this.routeService = routeService;
    }

    @Override
    public ResponseEntity<SavedRouteResponse> createRoute(SavedRouteRequest savedRouteRequest) {
        SavedRouteResponse response = routeService.createRoute(savedRouteRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<SavedRouteResponse>> listRoutes() {
        List<SavedRouteResponse> routes = routeService.getAllRoutes();
        return ResponseEntity.ok(routes);
    }
}
