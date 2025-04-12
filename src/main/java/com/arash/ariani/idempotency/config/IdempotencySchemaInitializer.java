package com.arash.ariani.idempotency.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
@ConditionalOnProperty(name = "idempotency.store", havingValue = "jpa")
public class IdempotencySchemaInitializer {

    @Bean
    @ConditionalOnProperty(name = "idempotency.init-schema", havingValue = "true", matchIfMissing = true)
    public CommandLineRunner initIdempotencyTable(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema.sql"));
            }
        };
    }
}
