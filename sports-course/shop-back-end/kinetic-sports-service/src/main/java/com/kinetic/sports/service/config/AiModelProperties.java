package com.kinetic.sports.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.model")
public class AiModelProperties {

    private boolean enabled = false;

    private String provider = "openai-compatible";

    private String baseUrl = "";

    private String apiKey = "";

    private String modelName = "";

    private Double temperature = 0.4D;

    private Integer timeoutSeconds = 60;

    private Integer retryCount = 1;
}
