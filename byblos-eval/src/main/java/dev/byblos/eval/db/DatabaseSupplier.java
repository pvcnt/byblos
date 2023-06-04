package dev.byblos.eval.db;

import com.netflix.iep.service.ClassFactory;
import com.typesafe.config.Config;

import java.util.function.Supplier;

public final class DatabaseSupplier implements Supplier<Database> {
    private final Database database;

    public DatabaseSupplier(Config config, ClassFactory classFactory) {
        database = newInstance(config, classFactory);
    }

    @Override
    public Database get() {
        return database;
    }

    private static Database newInstance(Config config, ClassFactory classFactory) {
        var className = config.getString("class");
        try {
            return classFactory.newInstance(className, clz -> config);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("database class [" + className + "] does not exist", e);
        }
    }
}
