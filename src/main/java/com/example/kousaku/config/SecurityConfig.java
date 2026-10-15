package com.example.kousaku.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/login", "/error").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/inquiries").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/inquiries/status").permitAll()
				.requestMatchers("/", "/inquiries", "/inquiries/status", "/css/**", "/js/**", "/images/**").permitAll()
				.requestMatchers("/api/staff/**").authenticated()
				.requestMatchers("/staff/**").authenticated()
				.requestMatchers("/api/reservations").permitAll()
				.anyRequest().authenticated())
			.formLogin(formLogin -> formLogin.permitAll())
			.csrf(csrf -> csrf.ignoringRequestMatchers("/api/reservations", "/api/inquiries", "/api/staff/**"));

		return http.build();
	}
}
