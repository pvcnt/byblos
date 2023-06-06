package dev.byblos.webapi;

import dev.byblos.webapi.security.EmptyClientRegistrationRepository;
import dev.byblos.webapi.security.GitHubOAuthUserService;
import dev.byblos.webapi.security.SecuritySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecuritySettings settings) throws Exception {
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
        return http.build();
    }

    @Bean
    ClientRegistrationRepository clientRegistrationRepository(SecuritySettings settings) {
        if (settings.enabled()) {
            return new InMemoryClientRegistrationRepository(clientRegistration(settings));
        }
        return EmptyClientRegistrationRepository.INSTANCE;
    }

    @Bean
    WebClient rest(ClientRegistrationRepository clients, OAuth2AuthorizedClientRepository authz) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(clients, authz);
        return WebClient.builder().filter(oauth2).build();
    }

    @Bean
    OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService(SecuritySettings settings, WebClient rest) {
        return switch (settings.provider()) {
            case "github" -> githubUserService(settings, rest);
            default -> new DefaultOAuth2UserService();
        };
    }

    private OAuth2UserService<OAuth2UserRequest, OAuth2User> githubUserService(SecuritySettings settings, WebClient rest) {
        return new GitHubOAuthUserService(rest, settings.githubOrg().orElse(null));
    }

    private ClientRegistration clientRegistration(SecuritySettings settings) {
        return switch (settings.provider()) {
            case "google" -> googleClientRegistration(settings);
            case "github" -> githubClientRegistration(settings);
            case "okta" -> oktaClientRegistration(settings);
            default -> throw new IllegalArgumentException("Unknown security provider: " + settings.provider());
        };
    }

    private ClientRegistration googleClientRegistration(SecuritySettings settings) {
        return CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(settings.clientId())
                .clientSecret(settings.clientSecret())
                .build();
    }

    private ClientRegistration githubClientRegistration(SecuritySettings settings) {
        return CommonOAuth2Provider.GITHUB.getBuilder("github")
                .clientId(settings.clientId())
                .clientSecret(settings.clientSecret())
                .scope("user:email", "read:email", "read:org")
                .build();
    }

    private ClientRegistration oktaClientRegistration(SecuritySettings settings) {
        var subdomain = settings.subdomain();
        return CommonOAuth2Provider.OKTA.getBuilder("okta")
                .clientId(settings.clientId())
                .clientSecret(settings.clientSecret())
                .authorizationUri("https://" + subdomain + ".okta.com/oauth2/default/v1/authorize")
                .tokenUri("https://" + subdomain + ".okta.com/oauth2/default/v1/token")
                .jwkSetUri("https://" + subdomain + ".okta.com/oauth2/default/v1/keys")
                .issuerUri("https://" + subdomain + ".okta.com/oauth2/default")
                .build();
    }
}
