package com.sonicdevelopment.testconfiguration.api;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com.sonicdevelopment.driving.adapter"
})
public class APITestContextConfiguration {

}
