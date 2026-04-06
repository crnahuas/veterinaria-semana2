package com.duoc.veterinaria.config;

import com.duoc.veterinaria.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${app.http.port:8080}")
    private int httpPort;

    @Value("${server.port:8384}")
    private int httpsPort;

    @Value("${app.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*,https://localhost:*,https://127.0.0.1:*}")
    private String allowedOriginPatterns;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (sslEnabled) {
            http.portMapper(portMapper -> portMapper.http(httpPort).mapsTo(httpsPort));
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autorizado")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, Constants.API_LOGIN_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/home", "/login", "/pacientes", "/citas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/index.html", "/login.html", "/pacientes.html", "/citas.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/css/**", "/js/**", "/favicon.ico").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pacientes/**").hasAnyRole("RECEPCIONISTA", "VETERINARIO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/pacientes/**").hasAnyRole("RECEPCIONISTA", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/pacientes/**").hasAnyRole("RECEPCIONISTA", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/citas/**").hasAnyRole("RECEPCIONISTA", "VETERINARIO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/citas/**").hasAnyRole("RECEPCIONISTA", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/citas/**").hasAnyRole("RECEPCIONISTA", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/facturas/**").hasAnyRole("RECEPCIONISTA", "VETERINARIO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/facturas/**").hasAnyRole("RECEPCIONISTA", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/veterinarios/**").hasAnyRole("RECEPCIONISTA", "VETERINARIO", "ADMIN")
                        .requestMatchers("/greetings").authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(parseAllowedOriginPatterns());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of(Constants.HEADER_AUTHORIZATION_KEY));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> parseAllowedOriginPatterns() {
        return Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .filter(pattern -> !pattern.isEmpty())
                .toList();
    }
}
