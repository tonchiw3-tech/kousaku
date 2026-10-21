package com.example.kousaku.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
				.requestMatchers("/api/staff/**").hasRole("STAFF")
				.requestMatchers("/staff/**").hasRole("STAFF")
				.requestMatchers("/api/reservations").permitAll()
				.anyRequest().authenticated())
			.formLogin(formLogin -> formLogin.permitAll())
			.csrf(csrf -> csrf.ignoringRequestMatchers("/api/reservations", "/api/inquiries"));

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService users(@Value("${STAFF_USERNAME}") String staffName,
	                         @Value("${STAFF_PASSWORD}") String staffPassword,
	                         PasswordEncoder passwordEncoder) {
		UserDetails staff = User.withUsername(staffName).password(passwordEncoder.encode(staffPassword)).roles("STAFF").build();
		return new InMemoryUserDetailsManager(staff);
	}
}
