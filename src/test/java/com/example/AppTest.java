package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for App class
 */
public class AppTest {

    @Test
    public void testGetMessage() {
        App app = new App();
        assertEquals("Hello, Jenkins Pipeline!", app.getMessage());
    }

    @Test
    public void testMessageNotNull() {
        App app = new App();
        assertNotNull(app.getMessage());
    }

    @Test
    public void testMessageContainsJenkins() {
        App app = new App();
        assertTrue(app.getMessage().contains("Jenkins"));
    }
}
