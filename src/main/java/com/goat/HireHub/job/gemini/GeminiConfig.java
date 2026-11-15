package com.goat.HireHub.job.gemini;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {
    @Bean
    public Client geminiClient(@Value("${gemini.api-key}") String apiKey) {
        HttpOptions httpOptions= HttpOptions.builder()
                .timeout(30_000)
                .retryOptions(
                         HttpRetryOptions.builder()
                                 .attempts(3)
                                 .initialDelay(1.0)
                                 .maxDelay(10.0)
                                 .expBase(2.0)
                                 .httpStatusCodes(408, 429, 500, 502, 503, 504)
                                 .build()
                )
                .build();
        return Client.builder()
                .apiKey(apiKey)
                .httpOptions(httpOptions)
                .build();
    }
}