package com.qa.framework.tests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TagTest {

    @Test
    @Tag("all")
    @Tag("all2")
    @Tag("SomeTag1")
    void testWithTag1() throws InterruptedException {
        Thread.sleep(2000);
        System.out.println("✅ Test with SomeTag executed 1");
        assertTrue(true);
    }

    @Test
    @Tag("all")
    @Tag("SomeTag1")
    void testWithTag2() throws InterruptedException {
        Thread.sleep(2000);
        System.out.println("✅ Test with SomeTag executed 1");
        assertTrue(true);
    }

    @Test
    @Tag("all")
    @Tag("SomeTag1")
    void testWithTag3() throws InterruptedException {
        Thread.sleep(2000);
        System.out.println("✅ Test with SomeTag executed 1");
        assertTrue(true);
    }

    @Test
    @Tag("all")
    @Tag("SomeTag1")
    void testWithTag4() throws InterruptedException {
        Thread.sleep(2000);
        System.out.println("✅ Test with SomeTag executed 1");
        assertTrue(true);
    }

    @Test
    @Tag("all")
    @Tag("SomeTag1")
    void testWithTag5() throws InterruptedException {
        Thread.sleep(2000);
        System.out.println("✅ Test with SomeTag executed 1");
        assertTrue(true);
    }

    @Test
    void testWithoutTag() {
        System.out.println("✅ Test without tag executed");
        assertTrue(true);
    }
}