package com.Uber.service;

import com.Uber.dto.DriverPingDTO;
import com.Uber.dto.RideMatchRequestDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class MatchmakerService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public MatchmakerService(@Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 1. Ingests driver coordinates into a regional geohash shard.
     * Generates a sharded key like: {nyc}:active_drivers
     */
    public Mono<Long>   updateDriverLocation(DriverPingDTO ping) {
        String shardedKey = "{" + ping.getCity().toLowerCase() + "}:active_drivers";
        Point point = new Point(ping.getLongitude(), ping.getLatitude());

        return redisTemplate.opsForGeo()
                .add(shardedKey, point, ping.getDriverId());
    }

    /**
     * 2. Finds the 5 nearest drivers within a 3km radius inside the regional shard.
     */
    public Flux<String> findNearestDrivers(RideMatchRequestDTO request) {
        String shardedKey = "{" + request.getCity().toLowerCase() + "}:active_drivers";

        GeoReference<String> reference = GeoReference.fromCoordinate(request.getLongitude(), request.getLatitude());
        Distance distance = new Distance(3.0, RedisGeoCommands.DistanceUnit.KILOMETERS);

        RedisGeoCommands.GeoSearchCommandArgs args = RedisGeoCommands.GeoSearchCommandArgs
                .newGeoSearchArgs()
                .sortAscending()
                .limit(5);

        return redisTemplate.opsForGeo()
                .search(shardedKey, reference, distance, args)
                .map(geoResult -> geoResult.getContent().getName());
    }

    /**
     * 3. Concurrency Protection: Attempts to acquire a 10-second distributed lock
     * on the selected driver to prevent multiple concurrent riders from booking the same driver.
     */
    public Mono<Boolean> acquireDriverLock(String driverId) {
        String lockKey = "lock:driver:" + driverId;

        // SETNX implementation using reactive operations with an explicit TTL
        return redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "BOOKED", Duration.ofSeconds(10));
    }
}