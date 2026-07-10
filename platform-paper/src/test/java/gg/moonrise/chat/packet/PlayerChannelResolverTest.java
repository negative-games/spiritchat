package gg.moonrise.chat.packet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerChannelResolverTest {

    @Test
    void readsFieldsDeclaredOnSuperclasses() {
        Child child = new Child();

        assertEquals("value", PlayerChannelResolver.field(child, "inherited").orElseThrow());
    }

    @Test
    void returnsEmptyForMissingFieldsAndMethods() {
        Object target = new Object();

        assertTrue(PlayerChannelResolver.field(target, "missing").isEmpty());
        assertTrue(PlayerChannelResolver.invoke(target, "missing").isEmpty());
    }

    @Test
    void invokesNoArgMethods() {
        MethodTarget target = new MethodTarget();

        assertEquals("result", PlayerChannelResolver.invoke(target, "value").orElseThrow());
    }

    private static class Parent {
        private final String inherited = "value";
    }

    private static final class Child extends Parent {
    }

    private static final class MethodTarget {
        public String value() {
            return "result";
        }
    }
}
