package com.ontic.Garuda.commons.props;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class PropertyReader {

    private final Environment environment;

    public PropertyReader(Environment environment) {
        this.environment = environment;
    }

    public String readStringProperty(String key) {
        return environment.getProperty(key);
    }

    public Integer readIntegerProperty(String key) {
        return environment.getProperty(key, Integer.class);
    }

    public boolean readBooleanProperty(String key) {
        Boolean property = environment.getProperty(key, Boolean.class);
        if (property == null) {
            return false;
        }
        return property;
    }

    public Long readLongProperty(String key) {
        return environment.getProperty(key, Long.class);
    }
}
