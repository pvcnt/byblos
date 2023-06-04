package dev.byblos;

import com.netflix.iep.config.ConfigManager;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

/**
 * Provides a simple way to start up a standalone server. Usage:
 * <p>
 * ```
 * $ java -jar byblos.jar config1.conf config2.conf
 * ```
 */
@SpringBootApplication
public class ByblosApplication {
    private static final Logger logger = LoggerFactory.getLogger(ByblosApplication.class);

    public static void main(String[] args) {
        try {
            loadAdditionalConfigFiles((args.length > 0) ? args : new String[]{"static.conf"});
            SpringApplication.run(ByblosApplication.class, args);
        } catch (Throwable t) {
            logger.error("server failed to start, shutting down", t);
            System.exit(1);
        }
    }

    private static void loadAdditionalConfigFiles(String[] files) {
        for (var f : files) {
            logger.info("loading config file: " + f);
            var file = new File(f);
            var c = file.exists() ? ConfigFactory.parseFileAnySyntax(file) : ConfigFactory.parseResourcesAnySyntax(f);
            ConfigManager.dynamicConfigManager().setOverrideConfig(c);
        }
    }
}