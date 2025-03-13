package com.lantanagroup.sandboxazure.configurations;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;
import org.springframework.context.annotation.Bean;

public class AzureConfiguration {
    @Bean
    public SecretClientCustomizer secretClientCustomizer() {
        return ((builder, endpoint) -> builder.credential(new DefaultAzureCredentialBuilder().build()));
    }
}
