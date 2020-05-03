package com.cloudweb.awsmetric;


import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class StatsDMetrics {
    @Value("${publish.metrics}")
    private boolean publishMetrics;

    @Bean
    public StatsDClient metricsClient() {

        if (publishMetrics) {
            return new NonBlockingStatsDClient("csye6225", "localhost", 8125);
        }

        return new NoOpStatsDClient();
    }

}