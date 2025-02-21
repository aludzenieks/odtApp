package com.example.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
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
        // Read the first JSON file into a JsonElement tree
        JsonElement tree1 = JsonParser.parseReader(new FileReader(file1));

        // Read the second JSON file into a JsonElement tree
        JsonElement tree2 = JsonParser.parseReader(new FileReader(file2));

        // Compare the two JsonElement trees and return the result
        return tree1.equals(tree2);
    }
}