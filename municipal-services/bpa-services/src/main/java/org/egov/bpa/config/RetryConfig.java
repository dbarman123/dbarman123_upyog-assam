package org.egov.bpa.config;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;


/**
 * Configuration class to set up RetryTemplate with custom retry policies.
 * Retries are configured for specific exceptions with exponential backoff strategy.
 *
 */
@Configuration
@EnableRetry
@Slf4j
public class RetryConfig {

    @Autowired
    private BPAConfiguration config;


    @Bean
    public RetryTemplate retryTemplate() {
        log.info("Creating RetryTemplate bean with maxAttempts={}, backoffMs={}",
                config.getMaxAttempts(), config.getBackoffMs());
        RetryTemplate retryTemplate = new RetryTemplate();

        // Backoff
       // FixedBackOffPolicy backOff = new FixedBackOffPolicy();
        //backOff.setBackOffPeriod(backoffMs);

        // --- Exponential Backoff ---
        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(config.getBackoffMs());   // starting backoff
        backOff.setMultiplier(2.0);              // doubles each retry
        backOff.setMaxInterval(30000);           // cap at 30 sec
        retryTemplate.setBackOffPolicy(backOff);

        // Retry ON specific exceptions only
        Map<Class<? extends Throwable>, Boolean> retryable = new HashMap<>();
        retryable.put(ConnectException.class, true);            // pod down / restarting
        retryable.put(SocketTimeoutException.class, true);      // pod not ready
        retryable.put(ResourceAccessException.class, true);    // Connection failures wrapper
        retryable.put(HttpServerErrorException.ServiceUnavailable.class, true); // ONLY 503

        // retryable.put(HttpServerErrorException.class, true); // 5xx


        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                config.getMaxAttempts(),
                retryable,
                true
        );

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOff);

        return retryTemplate;
    }
}

