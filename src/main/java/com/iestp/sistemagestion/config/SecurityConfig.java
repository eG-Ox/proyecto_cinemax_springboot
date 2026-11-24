package com.iestp.sistemagestion.config;

import com.iestp.sistemagestion.schemas.SesionDTO;
import com.iestp.sistemagestion.servicio.CuentaAccesoService;
import jakarta.servlet.DispatcherType;
import java.util.Map;
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
import tools.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {
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
            DaoAuthenticationProvider provider, ObjectMapper mapper) {
        http
            .authenticationProvider(provider)
            .cors(Customizer.withDefaults())
            .csrf(Customizer.withDefaults())
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
                .successHandler((request, response, authentication) ->
                    RespuestaSeguridad.escribir(mapper, response, 200, SesionDTO.fromAuthentication(authentication)))
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
