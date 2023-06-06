package dev.byblos.webapi;

import com.netflix.iep.config.ConfigManager;
import com.netflix.iep.service.ClassFactory;
import com.netflix.iep.service.DefaultClassFactory;
import com.typesafe.config.Config;
import dev.byblos.eval.db.Database;
import dev.byblos.eval.db.DatabaseSupplier;
import dev.byblos.eval.graph.DefaultSettings;
import dev.byblos.eval.graph.GraphConfigFactory;
import dev.byblos.eval.graph.Grapher;
import dev.byblos.webapi.security.EmptyClientRegistrationRepository;
import dev.byblos.webapi.security.SecuritySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
public class ApiConfiguration {
    @Bean
    Config config() {
        return ConfigManager.dynamicConfig();
    }

    @Bean
    ClassFactory classFactory() {
        return new DefaultClassFactory();
    }

    @Bean
    DefaultSettings defaultSettings(Config config) {
        return DefaultSettings.fromConfig(config);
    }

    @Bean
    ApiSettings apiSettings(Config config) {
        return ApiSettings.fromConfig(config);
    }

    @Bean
    SecuritySettings securitySettings(Config config) {
        return SecuritySettings.fromConfig(config);
    }

    @Bean
    GraphConfigFactory graphConfigFactory(DefaultSettings defaultSettings) {
        return new GraphConfigFactory(defaultSettings);
    }

    @Bean
    DatabaseSupplier databaseSupplier(Config config, ClassFactory classFactory) {
        return new DatabaseSupplier(config.getConfig("byblos.eval.db"), classFactory);
    }

    @Bean
    Database database(DatabaseSupplier databaseSupplier) {
        return databaseSupplier.get();
    }

    @Bean
    Grapher grapher(DefaultSettings settings, Database database) {
        return new Grapher(settings, database);
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(SecuritySettings settings) {
        if (settings.enabled()) {
            return new InMemoryClientRegistrationRepository(clientRegistration(settings));
        }
        return EmptyClientRegistrationRepository.INSTANCE;
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
