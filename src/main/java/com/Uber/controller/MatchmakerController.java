package com.Uber.controller;

import com.Uber.dto.DriverPingDTO;
import com.Uber.dto.RideMatchRequestDTO;
import com.Uber.service.MatchmakerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/match")
public class MatchmakerController {

    private final MatchmakerService matchmakerService;

    public MatchmakerController(MatchmakerService matchmakerService) {
        this.matchmakerService = matchmakerService;
    }

    // Drivers hit this to update their live coordinates
    @PostMapping("/driver/ping")
    public Mono<ResponseEntity<String>> pingLocation(@RequestBody DriverPingDTO pingDTO) {
        return matchmakerService.updateDriverLocation(pingDTO)
                .map(res -> ResponseEntity.ok("Location Ingested"));
    }

    // Riders hit this to discover the nearest 5 drivers
    @PostMapping("/search")
    public Flux<String> searchDrivers(@RequestBody RideMatchRequestDTO requestDTO) {
        return matchmakerService.findNearestDrivers(requestDTO);
    }

    // Standard booking endpoint leveraging the atomic lock mechanism
    @PostMapping("/book/{driverId}")
    public Mono<ResponseEntity<String>> bookDriver(@PathVariable String driverId) {
        return matchmakerService.acquireDriverLock(driverId)
                .map(isLocked -> {
                    if (isLocked) {
                        return ResponseEntity.ok("Booking confirmed. Driver locked.");
                    } else {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Driver already matched with another request.");
                    }
                });
    }
}