package com.hunglevi.backend.config;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {
    @Value("${ml.service.url}")
    private String mlServiceUrl;

    @Bean
    public WebClient mlWebClient() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
            .responseTimeout(Duration.ofSeconds(5));

        return WebClient.builder()
            .baseUrl(mlServiceUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .build();
    }
}
