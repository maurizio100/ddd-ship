package com.sonicdevelopment.application

import org.junit.jupiter.api.Tag

/** Marks a test that needs Docker (Testcontainers). Runs only under `./mvnw verify -Pdb`. */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Tag("db")
annotation class DbTest
