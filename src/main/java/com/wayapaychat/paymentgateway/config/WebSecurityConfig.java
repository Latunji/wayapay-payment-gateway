package com.wayapaychat.paymentgateway.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.
                cors().and().csrf().disable()
                .authorizeRequests()
                .antMatchers(
                        "/api/v1/payment-gateway/**",
                        "/api/v1/switch/**", "/api/v1/payment-gateway/**",
                        "/api/v1/wayaCallBack",
                        "/test-email-notification",
                        "/callback", "/waya/callback")
                .permitAll()
                .antMatchers("/api/v1/report/query/**").fullyAuthenticated()
                .antMatchers("/api/v1/transactions/**").fullyAuthenticated()
                .antMatchers("/api/v1/transactions/settlements/**").fullyAuthenticated()
                .antMatchers("/api/v1/test-recurrent-payment/**").permitAll()
                .antMatchers("/api/v1/recurrent-transactions/**").fullyAuthenticated()
                .antMatchers("/api/v1/revenue/query/**").fullyAuthenticated()
                .antMatchers("/api/v1/transactions/report/year-month-stats/**").fullyAuthenticated()
                .antMatchers("/api/v1/transactions/report/overview/**").fullyAuthenticated()
                .antMatchers("/api/v1/transactions/report/revenue-stats/**").fullyAuthenticated()
                .antMatchers("/api/v1//transaction/report/**").fullyAuthenticated()
//                .antMatchers("/api/v1/transaction/status/**").fullyAuthenticated()
                .antMatchers("/api/v1/request/ussd/**").permitAll()
                .antMatchers("/v2/api-docs", "/configuration/**", "/swagger*/**", "/actuator/**", "/webjars/**", "/api/v1/**").permitAll()
                .anyRequest().authenticated().and()
                .addFilter(new AuthorizationFilter(authenticationManager())).sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("*"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public HttpFirewall defaultHttpFirewall() {
        return new DefaultHttpFirewall();
    }
}
