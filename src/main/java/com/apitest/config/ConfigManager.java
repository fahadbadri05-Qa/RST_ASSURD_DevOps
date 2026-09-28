package com.apitest.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralized config loader.
 * Resolution order for every value: environment variable  ->  env-specific
 * properties file (config-<env>.properties)  ->  default config.properties.
 * <p>
 * Run against a specific environment with: mvn test -Denv=staging
 * Override the base URL in CI without touching files: export API_BASE_URL=...
 */
public class ConfigManager {

    private static final Properties properties = new Properties();

    static {
        String env = System.getProperty("env", "qa");
        loadPropertiesFile("config-" + env + ".properties");
        loadPropertiesFile("config.properties"); // fallback defaults, does not overwrite existing keys
    }

    private static void loadPropertiesFile(String fileName) {
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input != null) {
                Properties fileProps = new Properties();
                fileProps.load(input);
                fileProps.forEach((key, value) -> properties.putIfAbsent(key, value));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fileName, e);
        }
    }

    public static String getBaseUrl() {
        return resolve("API_BASE_URL", "base.url", "https://restful-booker.herokuapp.com");
    }

    public static String getUsername() {
        return resolve("API_USERNAME", "username", "admin");
    }

    public static String getPassword() {
        return resolve("API_PASSWORD", "password", "password123");
    }

    private static String resolve(String envVar, String propKey, String fallback) {
        String envValue = System.getenv(envVar);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        return properties.getProperty(propKey, fallback);
    }
}
