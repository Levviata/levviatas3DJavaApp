/*
 * This source file was generated by the Gradle 'init' task
 */
package me.levviata.levviatasjavapp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test void appHasAGreeting() {
        JavaApp classUnderTest = new JavaApp();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
    }
}
