package com.campus.lostfound.util;

import com.campus.lostfound.model.Item;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for exporting and importing Item data as JSON.
 * The JSON file is stored at the project root as {@code items.json}.
 */
public class ItemJsonHandler {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final File JSON_FILE = new File("items.json");

    /** Write the given list of items to {@code items.json}. */
    public static void writeItems(List<Item> items) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(JSON_FILE, items);
    }

    /** Read items from {@code items.json}. Returns an empty list if the file does not exist. */
    public static List<Item> readItems() throws IOException {
        if (!JSON_FILE.exists()) {
            return List.of();
        }
        return mapper.readValue(JSON_FILE, new TypeReference<List<Item>>() {});
    }
}
