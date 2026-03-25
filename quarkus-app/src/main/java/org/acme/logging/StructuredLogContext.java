package org.acme.logging;

import org.jboss.logging.MDC;

import java.util.HashMap;
import java.util.Map;

public final class StructuredLogContext {

    private StructuredLogContext() {
    }

    public static Object get(String key) {
        return MDC.get(key);
    }

    public static String getString(String key) {
        Object value = MDC.get(key);
        return value == null ? null : String.valueOf(value);
    }

    public static Scope open(Map<String, Object> fields) {
        return new Scope(fields);
    }

    public static final class Scope implements AutoCloseable {
        private final Map<String, Object> previousValues = new HashMap<>();
        private final Map<String, Object> currentValues = new HashMap<>();

        private Scope(Map<String, Object> fields) {
            fields.forEach((key, value) -> {
                previousValues.put(key, MDC.get(key));
                currentValues.put(key, value);
                if (value == null) {
                    MDC.remove(key);
                } else {
                    MDC.put(key, value);
                }
            });
        }

        @Override
        public void close() {
            currentValues.keySet().forEach(key -> {
                Object previous = previousValues.get(key);
                if (previous == null) {
                    MDC.remove(key);
                } else {
                    MDC.put(key, previous);
                }
            });
        }
    }
}