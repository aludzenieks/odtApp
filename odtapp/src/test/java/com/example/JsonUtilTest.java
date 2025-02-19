package com.example;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;

import com.example.util.JsonUtil;

public class JsonUtilTest {

    private static final String TEST_JSON_EQUAL_1 = "src/test/resources/test_json_equal_1.json";
    private static final String TEST_JSON_EQUAL_2 = "src/test/resources/test_json_equal_2.json";
    private static final String TEST_JSON_NOT_EQUAL = "src/test/resources/test_json_not_equal.json";
    private static final String TEST_JSON_NOT_EXSISTS = "src/test/resources/test_json.json";

    @Test
    public void testJsonFilesAreEqual() {
        File json_1 = new File(TEST_JSON_EQUAL_1);
        File json_2 = new File(TEST_JSON_EQUAL_2);
        assertDoesNotThrow(() -> {
            boolean result = JsonUtil.areJsonFilesEqual(json_1, json_2);
            assertTrue(result);
        });
    }

    @Test
    public void testJsonFilesAreNotEqual() {
        File json_1 = new File(TEST_JSON_EQUAL_1);
        File json_3 = new File(TEST_JSON_NOT_EQUAL);
        assertDoesNotThrow(() -> {
            boolean result = JsonUtil.areJsonFilesEqual(json_1, json_3);
            assertFalse(result);
        });
    }

    @Test
    public void testThrowsExceptionIfFileDoesNotExistsJsonFilesEqual() {
        File json_1 = new File(TEST_JSON_EQUAL_1);
        File json_3 = new File(TEST_JSON_NOT_EXSISTS);
        assertThrows(IOException.class, () -> {
            JsonUtil.areJsonFilesEqual(json_1, json_3);
        });
    }

}
