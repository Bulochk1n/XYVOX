package exchange.xyvox.Configs;

import exchange.xyvox.Authentication.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final RoleHierarchy roleHierarchy;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder,
                          RoleHierarchy roleHierarchy) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.roleHierarchy = roleHierarchy;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(userDetailsService)
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/xyvox/api/v1/auth","/xyvox/api/v1/auth/**", "/xyvox/api/v1/news", "/xyvox/api/v1/auth/main-page", "/xyvox/api/v1/trade", "/h2-console/**").permitAll()
                                .requestMatchers("/images/**", "/fonts/**", "/styles/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/xyvox/api/v1/auth?form=login")
                        .loginProcessingUrl("/xyvox/api/v1/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/xyvox/api/v1/users/my-profile", true)
                        .failureUrl("/xyvox/api/v1/auth?form=login&error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/xyvox/api/v1/auth/logout")
                        .logoutSuccessUrl("/xyvox/api/v1/auth?form=login&logout")
                        .permitAll()
                );

        return http.build();
    }
}
