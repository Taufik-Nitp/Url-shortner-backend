package com.parspecassignment.urlshortner.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;

@Component
public class TokenBucketRateLimitingFilter implements Filter {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Boolean> rateLimitScript;

    private static final int CAPACITY = 10;             // Max 10 requests
    private static final int REFILL_RATE = 1;           // Refill 1 token per 6 seconds → 10 per minute

    public TokenBucketRateLimitingFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        rateLimitScript = new DefaultRedisScript<>();
        rateLimitScript.setLocation(new ClassPathResource("token_bucket.lua"));
        rateLimitScript.setResultType(Boolean.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientIp = httpRequest.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = request.getRemoteAddr();

        String key = "token_bucket:" + clientIp;

        Boolean allowed = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(REFILL_RATE),
                String.valueOf(CAPACITY),
                String.valueOf(System.currentTimeMillis()),
                "1"
        );

        if (allowed == null || !allowed) {
            response.setContentType("text/plain");
            response.getWriter().write("Too Many Requests");
            response.getWriter().flush();
            return;
        }

        chain.doFilter(request, response);
    }
}
