package com.accantosystems.stratoss.vnfmdriver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration("WebSecurityConfiguration")
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final VNFMDriverProperties vnfmDriverProperties;

    @Autowired
    public WebSecurityConfiguration(VNFMDriverProperties vnfmDriverProperties) {
        this.vnfmDriverProperties = vnfmDriverProperties;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                    .authorizeRequests()
                    .antMatchers("/vnflcm/**").hasRole("USER")
                    .antMatchers("/grant/**").hasRole("USER")
                    .antMatchers("/vnfpkgm/v1/**").hasRole("USER")
                    .antMatchers("/management/**").hasRole("USER")
                    .anyRequest().denyAll()
                    .and()
                    .httpBasic();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/api/**", "/management/health", "/management/info", "/vnfpkgm/v2/**");
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build());
        manager.createUser(User.withDefaultPasswordEncoder().username("user_with_no_roles").password("password").roles("NONE").build());
        manager.createUser(User.withDefaultPasswordEncoder().username("locked_user").password("password").roles("USER").accountLocked(true).build());
        return manager;
    }

}
