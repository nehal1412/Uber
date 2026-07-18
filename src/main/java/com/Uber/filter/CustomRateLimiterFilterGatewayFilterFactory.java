package com.Uber.filter;

import com.Uber.service.RateLimiterService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CustomRateLimiterFilterGatewayFilterFactory
        extends AbstractGatewayFilterFactory<CustomRateLimiterFilterGatewayFilterFactory.Config> {

    private final RateLimiterService rateLimiterService;

    public CustomRateLimiterFilterGatewayFilterFactory(RateLimiterService rateLimiterService) {
        super(Config.class);
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Extract the client IP to use as the Redis tracking key
            String clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();

            // Execute the non-blocking Redis check
            return rateLimiterService.isAllowed(clientIp, config.getLimit(), config.getWindowMillis())
                    .flatMap(isAllowed -> {
                        if (isAllowed) {
                            return chain.filter(exchange); // Forward the request
                        } else {
                            // Reject with HTTP 429 Too Many Requests
                            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                            return exchange.getResponse().setComplete();
                        }
                    });
        };
    }

    // Spring automatically binds the variables from application.yml to this class
    public static class Config {
        private int limit;
        private long windowMillis;

        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }
        public long getWindowMillis() { return windowMillis; }
        public void setWindowMillis(long windowMillis) { this.windowMillis = windowMillis; }
    }
}