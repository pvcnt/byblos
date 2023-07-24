package dev.byblos.eval.backend;

import com.netflix.iep.service.ClassFactory;
import com.typesafe.config.Config;

import java.util.function.Supplier;

public final class BackendSupplier implements Supplier<Backend> {
    private final Backend backend;

    public BackendSupplier(Config config, ClassFactory classFactory) {
        backend = newInstance(config, classFactory);
    }

    @Override
    public Backend get() {
        return backend;
    }

    private static Backend newInstance(Config config, ClassFactory classFactory) {
        var className = config.getString("class");
        try {
            return classFactory.newInstance(className, clz -> config);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("backend class [" + className + "] does not exist", e);
        }
    }
}
