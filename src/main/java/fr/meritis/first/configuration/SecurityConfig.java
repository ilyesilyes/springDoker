package fr.meritis.first.configuration;

import fr.meritis.first.filter.CustomAuthorisationFilter;
import fr.meritis.first.handler.CustomAccessDeniedHandler;
import fr.meritis.first.handler.CustomAuthentificationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
//@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class SecurityConfig {
    private final BCryptPasswordEncoder encoder;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthentificationEntryPoint customAuthentificationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final CustomAuthorisationFilter customAuthorisationFilter;
    private static final String[] PUBLIC_URLS = {"/user/login/**", "/user/verify/code/**", "/user/register/**", "/user/resetpassword/**", "/user/verify/password/**", "/user/verify/account/**", "/user/refresh/token/**"};
    //private static final String[] PUBLIC_URLS = {"/**"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable().cors().disable();
        http.sessionManagement().sessionCreationPolicy(STATELESS);
        http.authorizeRequests().requestMatchers(PUBLIC_URLS).permitAll();
        http.authorizeRequests().requestMatchers(DELETE, "/users/delete/**").hasAnyAuthority("DELETE:USER");
        http.authorizeRequests().requestMatchers(DELETE, "/costomer/delete/**").hasAnyAuthority("DELETE:COSTOMER");
        http.exceptionHandling().accessDeniedHandler(customAccessDeniedHandler).authenticationEntryPoint(customAuthentificationEntryPoint);
        http.authorizeRequests().anyRequest().authenticated();
        http.addFilterBefore(customAuthorisationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(encoder);
        return new ProviderManager(authProvider);
    }
}
