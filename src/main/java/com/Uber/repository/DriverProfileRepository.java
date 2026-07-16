package com.Uber.repository;


import com.Uber.entity.DriverProfile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface DriverProfileRepository extends ReactiveCrudRepository<DriverProfile, Long> {

    // Example of a custom query method returning a reactive stream of profiles
    Flux<DriverProfile> findByHomeCity(String homeCity);
}