package org.automation.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public final class Endpoints {

    private static final Properties PROPERTIES = load();

    private Endpoints() {
    }

    public static String get(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("No endpoint defined for key: " + key);
        }
        return value;
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream in = Endpoints.class.getClassLoader().getResourceAsStream("endpoints.properties")) {
            properties.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return properties;
    }
}
