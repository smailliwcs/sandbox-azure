package com.lantanagroup.sandboxazure.configurations;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AzureConfiguration {
    @Bean
    @Profile("default-credential")
    public ConfigurationClientCustomizer configurationClientCustomizer() {
        return ((builder, endpoint) -> builder.credential(new DefaultAzureCredentialBuilder().build()));
    }
}
