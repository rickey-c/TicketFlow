package com.ticketflow.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * @Description: 服务监控权限配置
 * @Author: rickey-c
 * @Date: 2025/1/24 14:33
 */
@Configuration
public class SecurityConfig {
 
    private final String adminContextPath;
    
    @Autowired
    public SecurityConfig(AdminServerProperties adminServerProperties) {
        this.adminContextPath = adminServerProperties.getContextPath();
    }
 
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
 
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminContextPath + "/");

        
        http
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(adminContextPath + "/assets/**").permitAll()
                    .requestMatchers(adminContextPath + "/login", "/css/**", "/js/**", "/image/*").permitAll()
                    .requestMatchers(adminContextPath + "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated())
            .formLogin(form -> form
                    .loginPage(adminContextPath + "/login")
                    .successHandler(successHandler))
            .logout(logout -> logout
                    .logoutUrl(adminContextPath + "/logout"))
            .httpBasic(withDefaults())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/instances", "/actuator/**","/v3/api-docs/**",
                    adminContextPath + "/instances/**", adminContextPath + "/actuator/**", adminContextPath + "/v3/api-docs/**"));    
        return http.build();
    }
}