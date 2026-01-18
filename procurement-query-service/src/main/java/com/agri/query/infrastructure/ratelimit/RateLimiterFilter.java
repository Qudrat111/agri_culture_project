package com.agri.query.infrastructure.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
@Slf4j
public class RateLimiterFilter implements Filter {

    private static final int CAPACITY = 200;
    private static final int TOKENS = 200;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientId = httpRequest.getHeader("X-Client-Id");
        if (clientId == null || clientId.isBlank()) {
            clientId = httpRequest.getRemoteAddr();
        }
        
        Bucket bucket = buckets.computeIfAbsent(clientId, this::createBucket);
        
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for client: {}", clientId);
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                "{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Maximum 200 requests per minute.\"}"
            );
        }
    }

    private Bucket createBucket(String clientId) {
        log.debug("Creating new rate limit bucket for client: {}", clientId);
        
        Bandwidth limit = Bandwidth.classic(
            CAPACITY,
            Refill.intervally(TOKENS, REFILL_DURATION)
        );
        
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Initializing RateLimiterFilter with capacity: {} tokens per {} minute(s)",
            TOKENS, REFILL_DURATION.toMinutes());
    }

    @Override
    public void destroy() {
        log.info("Destroying RateLimiterFilter");
        buckets.clear();
    }
}
