package com.Uber.controller;


import com.Uber.entity.DriverProfile;
import com.Uber.repository.DriverProfileRepository;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverController {

    private final DriverProfileRepository repository;

    public DriverController(DriverProfileRepository repository) {
        this.repository = repository;
    }

    // Creates a new driver profile in Postgres
    @PostMapping
    public Mono<DriverProfile> createDriver(@RequestBody DriverProfile profile) {
        return repository.save(profile);
    }

    // Fetches all drivers reactively
    @GetMapping
    public Flux<DriverProfile> getAllDrivers() {
        return repository.findAll();
    }
}