package com.lantanagroup.sandboxazure.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping(value = "/environment", produces = MediaType.APPLICATION_JSON_VALUE)
public class EnvironmentController {
    private final ConfigurableEnvironment environment;
    private final ObjectMapper objectMapper;

    public EnvironmentController(ConfigurableEnvironment environment, ObjectMapper objectMapper) {
        this.environment = environment;
        this.objectMapper = objectMapper;
    }

    @RequestMapping("/profile")
    public List<String> getProfiles() {
        return Arrays.asList(environment.getActiveProfiles());
    }

    @RequestMapping("/source")
    public Object getSources(
            @RequestParam(value = "name", required = false) String sourceName,
            @RequestParam(value = "index", required = false) Integer sourceIndex) {
        if (sourceName != null && sourceIndex != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot specify both name and index");
        }
        if (sourceName != null) {
            return getOneSource(environment.getPropertySources().get(sourceName));
        }
        if (sourceIndex != null) {
            List<PropertySource<?>> propertySources = environment.getPropertySources().stream().toList();
            if (sourceIndex < 0 || sourceIndex >= propertySources.size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Index out of range");
            }
            return getOneSource(propertySources.get(sourceIndex));

        }
        return getAllSources();
    }

    private List<String> getAllSources() {
        return environment.getPropertySources().stream()
                .map(PropertySource::getName)
                .toList();
    }

    private Map<String, Object> getOneSource(PropertySource<?> source) {
        if (source == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Source not found");
        }
        if (!(source instanceof EnumerablePropertySource<?> enumerableSource)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source is not enumerable");
        }
        Map<String, Object> properties = new TreeMap<>();
        for (String propertyName : enumerableSource.getPropertyNames()) {
            properties.put(propertyName, enumerableSource.getProperty(propertyName));
        }
        return properties;
    }

    @RequestMapping("/property")
    public Object getProperties(@RequestParam(value = "name", required = false) String propertyName) {
        return propertyName == null ? getAllProperties() : getOneProperty(propertyName);
    }

    private Map<String, String> getAllProperties() {
        List<String> propertyNames = environment.getPropertySources().stream()
                .filter(EnumerablePropertySource.class::isInstance)
                .map(EnumerablePropertySource.class::cast)
                .flatMap(source -> Arrays.stream(source.getPropertyNames()))
                .distinct()
                .toList();
        Map<String, String> properties = new TreeMap<>();
        for (String propertyName : propertyNames) {
            properties.put(propertyName, environment.getProperty(propertyName));
        }
        return properties;
    }

    private String getOneProperty(String propertyName) {
        try {
            return objectMapper.writeValueAsString(environment.getProperty(propertyName));
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
