package com.example.kousaku.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/", "/css/**", "/js/**", "/images/**", "/api/reservations").permitAll()
				.requestMatchers("/staff/**").authenticated()
				.anyRequest().authenticated())
			.formLogin(formLogin -> formLogin.permitAll())
			.csrf(csrf -> csrf.ignoringRequestMatchers("/api/reservations"));

		return http.build();
	}
}
