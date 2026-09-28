package com.example.payment.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita autorização para granular em nível de médoto (@PreAuthorize)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

//        return http
//                .csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        // Public Endpoints (Health, Metrics, OpenAPI)
//                        // Métricas, documentação e Health são públicos
//                        .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
//
//                        // Apenas tokens com o escopo 'payment:write' podem criar pagamentos (POST)
//                        .requestMatchers(HttpMethod.POST, "/api/v1/payments").hasAuthority("SCOPE_payment:write")
//
//                        // Apenas tokens com o escopo 'payment:read' podem consultar (GET)
//                        .requestMatchers(HttpMethod.GET, "/api/v1/payments/**").hasAuthority("SCOPE_payment:read")
//
//                        // Qualquer outra requisição exige token válido
//                        .anyRequest().authenticated()
//                )
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
//                .build();

        // Para testes Localmente
        return http
                // Desativa CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // Garante que não usaremos sessão
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Libera explicitamente qualquer rota HTTP
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                // Liberação Necessária para o H2 console (iFrames)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                // Desativa as telas e prompts de login padrao
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }

    // Roles personalizadas do Keycloak/Azure (ex: ROLE_ADMIN, ROLE_OPERATOR)
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Altera o mapeamento para procurar a claim 'roles' e adicionar o prefixo 'ROLE_'
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
