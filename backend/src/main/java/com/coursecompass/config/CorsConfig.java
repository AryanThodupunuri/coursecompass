package com.coursecompass.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration.
 *
 * Notes:
 * - For local dev, we allow http://localhost:3000 and http://localhost:8080.
 * - For prod, you should add your extension's origin:
 *   chrome-extension://<EXTENSION_ID>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedMethods("GET", "POST", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(false)
        .allowedOriginPatterns(getAllowedOriginPatterns());
  }

  private String[] getAllowedOriginPatterns() {
    // You can override via env var/coursecompass.allowedOrigins later if you want,
    // but keeping it simple for now.
    List<String> origins = List.of(
        "http://localhost:8080",
        "http://localhost:3000",
        "https://louslist.org",
        "https://www.louslist.org",
        "chrome-extension://*"
    );
    return origins.toArray(new String[0]);
  }
}
