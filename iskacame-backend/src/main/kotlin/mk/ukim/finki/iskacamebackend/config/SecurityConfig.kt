package mk.ukim.finki.iskacamebackend.config

import mk.ukim.finki.iskacamebackend.security.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
  private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {

  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {

    http
      .csrf { it.disable() }
      .headers { headers -> headers.frameOptions { frame -> frame.sameOrigin() } }
      .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
      .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
      .authorizeHttpRequests { auth ->
        auth
          .requestMatchers(
            "/api/auth/**",
            "/h2/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/favicon.ico",
            "/ws/**")
          .permitAll()
          .anyRequest().authenticated()
      }
      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

    return http.build()
  }

  @Bean
  fun authenticationManager(
    http: HttpSecurity,
    passwordEncoder: PasswordEncoder,
    userDetailsService: UserDetailsService
  ): AuthenticationManager {

    val authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder::class.java)

    authManagerBuilder
      .userDetailsService(userDetailsService)
      .passwordEncoder(passwordEncoder)

    return authManagerBuilder.build()
  }

  @Bean
  fun passwordEncoder(): PasswordEncoder {
    return BCryptPasswordEncoder()
  }

  @Bean
  fun corsConfigurationSource(): CorsConfigurationSource {
    val configuration = CorsConfiguration()
    configuration.allowedOrigins = listOf("http://localhost:8081", "http://localhost:63342")
    configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
    configuration.allowedHeaders = listOf("Authorization", "Content-Type", "Cache-Control")
    configuration.allowCredentials = true
    configuration.maxAge = 3600

    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", configuration)
    return source
  }
}