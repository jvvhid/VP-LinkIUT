package edu.iutdhaka.linkiut.config;

import edu.iutdhaka.linkiut.model.AppUser;
import edu.iutdhaka.linkiut.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Permit static resources and H2 console
            // Permit static resources, H2 console, and landing page
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/h2-console/**", "/css/**", "/js/**", "/images/**", "/error", "/register").permitAll()
                .anyRequest().authenticated()
            )
            // Form login
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/feed", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            // Disable CSRF for HTMX compatibility and H2 console
            .csrf(csrf -> csrf.disable())
            // Allow H2 console in iframes
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    /**
     * Loads users from the database (app_user table).
     * Password in DB is BCrypt-hashed. The seed data uses hash of "password".
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            AppUser appUser = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            return User.builder()
                    .username(appUser.getEmail())
                    .password(appUser.getPasswordHash())
                    .roles(appUser.getRole().name())
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
