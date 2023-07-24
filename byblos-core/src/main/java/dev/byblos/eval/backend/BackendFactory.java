package dev.byblos.eval.backend;

import com.netflix.iep.service.ClassFactory;
import com.typesafe.config.Config;

import static java.util.Objects.requireNonNull;

public final class BackendFactory {
    private final Config config;
    private final ClassFactory classFactory;

    public BackendFactory(Config config, ClassFactory classFactory) {
        this.config = requireNonNull(config);
        this.classFactory = requireNonNull(classFactory);
    }

    public Backend newInstance() {
        var className = config.getString("class");
        try {
            return classFactory.newInstance(className, clz -> config);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("backend class [" + className + "] does not exist", e);
        }
    }
}
