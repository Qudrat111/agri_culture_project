package com.agri.order.infrastructure.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    
    private static final String CLIENT_ID_HEADER = "X-Client-Id";
    private static final int MAX_CACHE_SIZE = 10000;
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    @Value("${procurement.order.ratelimit.tokens-per-minute:100}")
    private long tokensPerMinute;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientId = httpRequest.getHeader(CLIENT_ID_HEADER);
        
        if (clientId == null || clientId.isBlank()) {
            log.warn("Request missing X-Client-Id header from {}", httpRequest.getRemoteAddr());
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.getWriter().write("{\"error\":\"X-Client-Id header is required\"}");
            return;
        }
        
        Bucket bucket = buckets.computeIfAbsent(clientId, this::createBucket);
        
        // Simple cache size limit
        if (buckets.size() > MAX_CACHE_SIZE) {
            log.warn("Rate limiter cache size exceeded, clearing old entries");
            buckets.clear();
        }
        
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for client: {}", clientId);
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setHeader("X-Rate-Limit-Retry-After-Seconds", "60");
            httpResponse.getWriter().write("{\"error\":\"Too many requests. Rate limit exceeded.\"}");
        }
    }
    
    private Bucket createBucket(String clientId) {
        Bandwidth limit = Bandwidth.classic(
            tokensPerMinute,
            Refill.intervally(tokensPerMinute, Duration.ofMinutes(1))
        );
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
}
