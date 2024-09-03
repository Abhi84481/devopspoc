package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class HelloWorldTest {

    @Test
    public void testMain() {
        HelloWorld.main(new String[]{});
        assertTrue(true); // Simple test to ensure the program runs
    }
}
