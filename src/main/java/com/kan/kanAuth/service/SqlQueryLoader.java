package com.kan.kanAuth.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

@Service
public class SqlQueryLoader {
    private final Properties sqlProperties = new Properties();

    @PostConstruct
    public void loadQueries() throws IOException {
        try (InputStream input = getClass().getResourceAsStream("/sql.properties")) {
            sqlProperties.load(input);
        }
    }

    public String get(String key) {
        return sqlProperties.getProperty(key);
    }
}
