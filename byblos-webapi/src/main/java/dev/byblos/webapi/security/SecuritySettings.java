package dev.byblos.webapi.security;

import com.typesafe.config.Config;

public final class SecuritySettings {
    private final Config config;
    private final boolean enabled;
    private static final String PREFIX = "byblos.webapi.security";
    private static final String PROVIDER_KEY = "provider";
    private static final String CLIENT_ID_KEY = "client-id";
    private static final String CLIENT_SECRET_KEY = "client-secret";
    private static final String SUBDOMAIN_KEY = "subdomain";

    public SecuritySettings(Config config) {
        this.config = config;
        this.enabled = config.getBoolean("enabled");
    }

    public static SecuritySettings fromConfig(Config root) {
        return new SecuritySettings(root.getConfig(PREFIX));
    }

    public boolean enabled() {
        return enabled;
    }

    public String provider() {
        checkKeyExists(PROVIDER_KEY);
        return config.getString(PROVIDER_KEY);
    }

    public String clientId() {
        checkKeyExists(CLIENT_ID_KEY);
        return config.getString(CLIENT_ID_KEY);
    }

    public String clientSecret() {
        checkKeyExists(CLIENT_SECRET_KEY);
        return config.getString(CLIENT_SECRET_KEY);
    }

    public String subdomain() {
        checkKeyExists(SUBDOMAIN_KEY);
        return config.getString(SUBDOMAIN_KEY);
    }

    private void checkKeyExists(String key) {
        if (!config.hasPath(key)) {
            throw new IllegalArgumentException("Config " + PREFIX + "." + key + " must be specified when security is enabled");
        }
    }
}
