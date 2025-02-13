package com.sonicdevelopment.testconfiguration.persistence;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {
        "com.sonicdevelopment.driven.adapter.persistence"
})
@EnableJpaRepositories(basePackages = {
        "com.sonicdevelopment.driven.adapter.persistence"
})
@ComponentScan(basePackages = {
        "com.sonicdevelopment.driven.adapter"
})
public class PersistenceTestContextConfiguration {
}
