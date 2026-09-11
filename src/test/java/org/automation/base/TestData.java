package org.automation.base;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

public final class TestData {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TestData() {
    }

    /** Loads a JSON fixture file from src/test/resources/testdata/. */
    public static JsonNode load(String fileName) {
        try (InputStream in = TestData.class.getClassLoader().getResourceAsStream("testdata/" + fileName)) {
            return MAPPER.readTree(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Converts a fixture object node into a request body usable with RestAssured's .body(...). */
    public static Map<String, Object> asMap(JsonNode node) {
        return MAPPER.convertValue(node, new TypeReference<Map<String, Object>>() {
        });
    }
}
