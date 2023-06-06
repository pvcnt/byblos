package dev.byblos.webapi;

import dev.byblos.webapi.security.SecuritySettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    SecuritySettings settings;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (settings.enabled()) {
            http
                    .authorizeHttpRequests()
                    .antMatchers("/health").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .oauth2Login(withDefaults());
        } else {
            http.authorizeRequests().antMatchers("/").permitAll();
        }
    }
}
