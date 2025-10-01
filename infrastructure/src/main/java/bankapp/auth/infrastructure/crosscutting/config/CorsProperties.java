package bankapp.auth.infrastructure.crosscutting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        List<String> exposedHeaders,
        boolean allowCredentials,
        long maxAge
) {
    public CorsProperties {
        // Handle both null AND empty lists
        allowedMethods = (allowedMethods == null || allowedMethods.isEmpty()) ?
                List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") : allowedMethods;

        allowedHeaders = (allowedHeaders == null || allowedHeaders.isEmpty()) ?
                List.of("*") : allowedHeaders;

        exposedHeaders = (exposedHeaders == null || exposedHeaders.isEmpty()) ?
                List.of("X-Correlation-ID") : exposedHeaders;

        maxAge = maxAge > 0 ? maxAge : 3600;
    }
}
