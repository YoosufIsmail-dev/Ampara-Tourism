package com.ampara.tourism.config;

import com.ampara.tourism.security.AppUserDetailsService;
import com.ampara.tourism.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AppUserDetailsService userDetailsService;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, AppUserDetailsService userDetailsService, RateLimitFilter rateLimitFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/attractions/**", "/api/hotels/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/places/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**", "/api/foods/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/food-orders").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/weather/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/live/**").permitAll()
                        .requestMatchers("/api/ai/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/config/**").permitAll()
                        .requestMatchers("/map.html", "/map.js", "/map.css", "/index.html", "/town.html", "/auth.html", "/css/**", "/js/**", "/assets/**", "/images/**", "/", "/favicon.ico").permitAll()

                        // New public features - read access for all visitors
                        .requestMatchers(HttpMethod.GET, "/api/emergency-contacts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/transport/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/nearby-facilities/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/gallery/**").permitAll()
                        .requestMatchers("/api/i18n/**").permitAll()
                        .requestMatchers("/api/voice-guide/**").permitAll()
                        .requestMatchers("/api/offline/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/analytics/view").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/push/status").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/push/register", "/api/push/unregister").permitAll()

                        // Admin only
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/analytics/dashboard", "/api/analytics/place/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/push/send").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/attractions/**", "/api/hotels/**", "/api/places/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/attractions/**", "/api/hotels/**", "/api/places/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/attractions/**", "/api/hotels/**", "/api/places/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/emergency-contacts/**", "/api/transport/**",
                                "/api/nearby-facilities/**", "/api/events/**", "/api/gallery/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/emergency-contacts/**", "/api/transport/**",
                                "/api/nearby-facilities/**", "/api/events/**", "/api/gallery/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/emergency-contacts/**", "/api/transport/**",
                                "/api/nearby-facilities/**", "/api/events/**", "/api/gallery/**").hasRole("ADMIN")

                        // Everything else needs a valid token (tourist or admin)
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // needed for H2 console
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
