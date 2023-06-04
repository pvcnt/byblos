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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebApiConfiguration {
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
}
