package com.valorant.devopshub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Application startup test. If the Spring context fails to load (bad
 * configuration, missing bean, etc.) this test fails - this is exactly the
 * kind of failure Jenkins should catch before a bad build reaches Docker.
 */
@SpringBootTest
class DevopsHubApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty: a successful context load is the assertion.
    }
}
