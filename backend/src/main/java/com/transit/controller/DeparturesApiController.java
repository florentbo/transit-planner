package com.transit.controller;

import com.transit.api.DeparturesApi;
import com.transit.model.DeparturesResponse;
import com.transit.model.ErrorResponse;
import com.transit.service.DeparturesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class DeparturesApiController implements DeparturesApi {

    private final DeparturesService departuresService;

    public DeparturesApiController(DeparturesService departuresService) {
        this.departuresService = departuresService;
    }

    @Override
    public ResponseEntity<DeparturesResponse> getDepartures() {
        DeparturesResponse response = departuresService.getDepartures();
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        if (ex.getStatusCode() == HttpStatus.BAD_GATEWAY) {
            ErrorResponse error = new ErrorResponse("STIB_API_ERROR", ex.getReason());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
        }
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
