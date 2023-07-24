package dev.byblos.webapi;

import com.netflix.iep.config.ConfigManager;
import com.netflix.iep.service.ClassFactory;
import com.netflix.iep.service.DefaultClassFactory;
import com.typesafe.config.Config;
import dev.byblos.eval.backend.Backend;
import dev.byblos.eval.backend.BackendSupplier;
import dev.byblos.eval.graph.DefaultSettings;
import dev.byblos.eval.graph.GraphConfigFactory;
import dev.byblos.eval.graph.Grapher;
import dev.byblos.webapi.security.SecuritySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    BackendSupplier backendSupplier(Config config, ClassFactory classFactory) {
        return new BackendSupplier(config.getConfig("byblos.eval.backend"), classFactory);
    }

    @Bean
    Backend backend(BackendSupplier backendSupplier) {
        return backendSupplier.get();
    }

    @Bean
    Grapher grapher(DefaultSettings settings, Backend backend) {
        return new Grapher(settings, backend);
    }
}
