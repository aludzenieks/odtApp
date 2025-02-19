package com.example.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class JsonUtil {

    /**
     * Compares if JSON files are equal.
     *
     * @param file1 The file to compare.
     * @param file2 The file to compare.
     * @return true if JSON files are equal.
     */
    public static boolean areJsonFilesEqual(File file1, File file2) throws IOException {
        // Create an ObjectMapper instance for reading the JSON files
        ObjectMapper objectMapper = new ObjectMapper();

        // Read the first JSON file into a JsonNode tree
        JsonNode tree1 = objectMapper.readTree(file1);

        // Read the second JSON file into a JsonNode tree
        JsonNode tree2 = objectMapper.readTree(file2);

        // Compare the two JsonNode trees and return the result
        return tree1.equals(tree2);
    }

}
