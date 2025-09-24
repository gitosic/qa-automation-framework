package com.qa.framework.tests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TagTest {

    @Test
    @Tag("SomeTag1")
    void testWithTag1() {
        System.out.println("✅ Test with SomeTag executed 1");
        assertTrue(true);
    }

    @Test
    @Tag("SomeTag1")
    void testWithOtherTag2() {
        System.out.println("✅ Test with OtherTag executed 2");
        assertTrue(true);
    }

    @Test
    void testWithoutTag() {
        System.out.println("✅ Test without tag executed");
        assertTrue(true);
    }
}