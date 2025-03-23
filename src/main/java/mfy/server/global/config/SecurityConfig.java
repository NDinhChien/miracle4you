package mfy.server.global.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import mfy.server.global.auth.TokenProvider;
import mfy.server.global.security.JwtAuthorizationFilter;
import mfy.server.global.security.UserDetailsServiceImpl;
import mfy.server.global.security.handler.CustomAccessDeniedHandler;
import mfy.server.global.security.handler.CustomAuthenticationEntryPoint;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Value("${security.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${security.cors.allowed-methods}")
    private String[] allowedMethods;

    @Value("${security.cors.allowed-headers}")
    private String[] allowedHeaders;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(tokenProvider);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
        http.sessionManagement(
                sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                .requestMatchers("/api/v1/auth/logout", "/api/v1/auth/password", "/api/v1/auth/check").authenticated()
                .requestMatchers("/api/v1/auth/**").permitAll()

                .requestMatchers("/api/v1/user/profile", "/api/v1/user/item", "/api/v1/user/project").authenticated()
                .requestMatchers("/api/v1/user/**").permitAll()

                .requestMatchers("/api/v1/project/create", "/api/v1/project/join", "/api/v1/project/update")
                .authenticated()
                .requestMatchers("/api/v1/project/**").permitAll()

                .requestMatchers(HttpMethod.POST, "/api/v1/message/system", "/api/v1/message/global",
                        "/api/v1/message/private", "/api/v1/message/project")
                .authenticated()
                .requestMatchers("/api/v1/message/attachment").authenticated()
                .requestMatchers("/api/v1/message/**").permitAll()

                .requestMatchers("/api/v1/notification/**").authenticated()

                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/topic/**", "/queue/**").permitAll()
                .anyRequest().authenticated());

        http.addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(this.authenticationEntryPoint)
                .accessDeniedHandler(this.accessDeniedHandler));
        return http.build();
    }

    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
