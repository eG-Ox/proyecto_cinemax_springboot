package com.iestp.sistemagestion.config;

import com.iestp.sistemagestion.schemas.SesionDTO;
import com.iestp.sistemagestion.dao.CuentaAccesoRepository;
import com.iestp.sistemagestion.servicio.CuentaAccesoService;
import com.iestp.sistemagestion.servicio.LimitadorIntentosService;
import jakarta.servlet.DispatcherType;
import java.util.Map;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {
    @Bean
    public Clock securityClock() {
        return Clock.systemUTC();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(CuentaAccesoService cuentas,
            PasswordEncoder encoder) {
        var provider = new DaoAuthenticationProvider(cuentas);
        provider.setPasswordEncoder(encoder);
        provider.setHideUserNotFoundExceptions(true);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            DaoAuthenticationProvider provider, ObjectMapper mapper, LimitadorIntentosService limite,
            CuentaAccesoRepository cuentas) {
        http
            .authenticationProvider(provider)
            .cors(Customizer.withDefaults())
            .csrf(Customizer.withDefaults())
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("frame-ancestors 'none'; object-src 'none'; base-uri 'self'"))
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.NO_REFERRER)))
            .addFilterBefore(new LimiteLoginFilter(limite, mapper), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new CuentaVigenteFilter(cuentas, mapper), AuthorizationFilter.class)
            .requestCache(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .anyRequest().hasRole("ADMIN"))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(fixation -> fixation.changeSessionId()))
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, ex) ->
                    RespuestaSeguridad.error(mapper, response, 401, "Debe iniciar sesion"))
                .accessDeniedHandler((request, response, ex) ->
                    RespuestaSeguridad.error(mapper, response, 403, "Acceso denegado o token CSRF invalido")))
            .formLogin(login -> login
                .loginPage("/api/auth/login")
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("usuario")
                .passwordParameter("contrasena")
                .successHandler((request, response, authentication) -> {
                    limite.restablecer(request.getRemoteAddr());
                    RespuestaSeguridad.escribir(mapper, response, 200, SesionDTO.fromAuthentication(authentication));
                })
                .failureHandler((request, response, ex) ->
                    RespuestaSeguridad.error(mapper, response, 401, "Usuario o contrasena incorrectos")))
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) ->
                    RespuestaSeguridad.escribir(mapper, response, 200, Map.of("detail", "Sesion cerrada"))));
        return http.build();
    }
}
