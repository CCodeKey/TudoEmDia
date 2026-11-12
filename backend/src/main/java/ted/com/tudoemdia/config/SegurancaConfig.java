package ted.com.tudoemdia.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SegurancaConfig {

	@Bean
	public SecurityFilterChain filtroSeguranca(HttpSecurity http, SecurityContextRepository contextoRepository,
			@Value("${app.seguranca.exigir-https}") boolean exigirHttps) throws Exception {
		if (exigirHttps) {
			// Acessos por HTTP são redirecionados para HTTPS (RNF13)
			http.redirectToHttps(Customizer.withDefaults());
		}
		http
				.cors(cors -> {
				})
				// Token CSRF no cookie XSRF-TOKEN, devolvido pelo front no cabeçalho X-XSRF-TOKEN
				.csrf(csrf -> csrf.spa())
				.securityContext(contexto -> contexto.securityContextRepository(contextoRepository))
				.authorizeHttpRequests(rotas -> rotas
						.requestMatchers(HttpMethod.GET, "/api/auth/csrf", "/api/convites/*").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/cadastro", "/api/auth/login").permitAll()
						.requestMatchers("/error", "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
						// Toda rota com dados da empresa exige login (RNF01)
						.anyRequest().authenticated())
				.exceptionHandling(erros -> erros.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(logout -> logout
						.logoutUrl("/api/auth/logout")
						.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)));
		return http.build();
	}

	@Bean
	public SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

	// BCrypt com custo 10 (RNF02)
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(10);
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource(@Value("${app.frontend-url}") String frontendUrl) {
		CorsConfiguration cors = new CorsConfiguration();
		cors.setAllowedOrigins(List.of(frontendUrl));
		cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		cors.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
		cors.setExposedHeaders(List.of("Content-Disposition"));
		cors.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
		fonte.registerCorsConfiguration("/api/**", cors);
		return fonte;
	}
}
