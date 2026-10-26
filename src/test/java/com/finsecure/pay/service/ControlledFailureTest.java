package com.finsecure.pay.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

// Controlled failure used to demonstrate that a failing test stops the pipeline before any deployment.
class ControlledFailureTest {

    @Test
    void deliberatelyFailingTest() {
        assertEquals(1, 2, "controlled failure for the CI/CD demo");
    }
}
