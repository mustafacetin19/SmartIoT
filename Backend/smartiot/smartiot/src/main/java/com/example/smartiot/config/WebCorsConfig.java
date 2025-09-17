// PATH: src/main/java/com/example/smartiot/config/WebCorsConfig.java
package com.example.smartiot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class WebCorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cfg = new CorsConfiguration();

        // Geliştirme için yaygın senaryolar: localhost + LAN IP’leri (+ isteğe bağlı https)
        cfg.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://10.*:*",
                "http://172.16.*:*", "http://172.17.*:*", "http://172.18.*:*", "http://172.19.*:*",
                "http://172.20.*:*", "http://172.21.*:*", "http://172.22.*:*", "http://172.23.*:*",
                "http://172.24.*:*", "http://172.25.*:*", "http://172.26.*:*", "http://172.27.*:*",
                "http://172.28.*:*", "http://172.29.*:*", "http://172.30.*:*", "http://172.31.*:*",
                "http://192.168.*:*",
                "https://localhost:*",
                "https://127.0.0.1:*",
                "https://10.*:*",
                "https://192.168.*:*"
        ));

        // Metotlar / header’lar
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("*"));

        // Eğer cookie/tabanlı oturum kullanıyorsan true; token/localStorage ise false bırakabilirsin.
        cfg.setAllowCredentials(true);

        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(src);
    }
}
