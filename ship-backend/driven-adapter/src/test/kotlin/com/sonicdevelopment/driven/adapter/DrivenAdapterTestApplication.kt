package com.sonicdevelopment.driven.adapter

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/** Boot configuration that driven-adapter slice tests (`@DataJpaTest`) find by package search. */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaRepositories("com.sonicdevelopment.driven.adapter.persistence")
@EntityScan("com.sonicdevelopment.driven.adapter.persistence")
class DrivenAdapterTestApplication
