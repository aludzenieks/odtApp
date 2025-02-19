package com.example;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.example.exception.InvalidActionException;
import com.example.exception.InvalidArgumentException;
import com.example.util.FileUtil;
import com.example.validation.ArgumentValidator;

public class ArgumentValidatorTest {

    private static final String OUTPUT_JSON = "jsonPath/output.json";
    private static final String DIRECTORY_PATH = "directoryPath";

    @Test
    public void testValidateArgumentsValidJsonAction() {
        String[] args = { ArgumentValidator.JSON_ACTION, DIRECTORY_PATH, OUTPUT_JSON };
        try {
            try (MockedStatic<FileUtil> utilities = mockStatic(FileUtil.class)) {
                utilities.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
                utilities.when(() -> FileUtil.isValidPath(eq(Paths.get(OUTPUT_JSON).getParent())))
                        .thenReturn(true);
                ArgumentValidator.validateArguments(args);
            }
        } catch (Exception e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testValidateArgumentsValidReplaceAction() {
        String[] args = { ArgumentValidator.REPLACE_ACTION, DIRECTORY_PATH, "[import file.odt]",
                "[import newfile.odt]" };
        try {
            try (MockedStatic<FileUtil> utilities = mockStatic(FileUtil.class)) {
                utilities.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
                ArgumentValidator.validateArguments(args);
            }
        } catch (Exception e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testValidateArgumentsInvalidAction() {
        String[] args = { "invalidAction", DIRECTORY_PATH, OUTPUT_JSON };
        try (MockedStatic<FileUtil> utilities = mockStatic(FileUtil.class)) {
            utilities.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
            Exception exception = assertThrows(InvalidActionException.class, () -> {
                ArgumentValidator.validateArguments(args);
            });
            assertTrue(exception.getMessage().contains("Unknown action"), "Expected exception for unknown action");
        }
    }

    @Test
    public void testValidateArgumentsInsufficientArgumentsForJsonAction() {
        String[] args = { ArgumentValidator.JSON_ACTION, DIRECTORY_PATH };
        try (MockedStatic<FileUtil> utilities = mockStatic(FileUtil.class)) {
            utilities.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
            Exception exception = assertThrows(InvalidArgumentException.class, () -> {
                ArgumentValidator.validateArguments(args);
            });
            assertTrue(exception.getMessage().contains("Usage: java App <action>"),
                    "Expected exception for insufficient arguments");
        }
    }

    @Test
    public void testValidateArgumentsInvalidExtraArgumentsForJasonAction() {
        String[] args = { ArgumentValidator.JSON_ACTION, DIRECTORY_PATH, "extra1", "extra2", "extra3" };
        try (MockedStatic<FileUtil> utilities = mockStatic(FileUtil.class)) {
            utilities.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
            Exception exception = assertThrows(InvalidArgumentException.class, () -> {
                ArgumentValidator.validateArguments(args);
            });
            assertTrue(exception.getMessage().contains("Usage: java App <action>"),
                    "Expected exception for invalid extra arguments");
        }
    }

    @Test
    public void testValidateArgumentsInvalidNumberArgumentsForJsonAction() {
        String[] args = { ArgumentValidator.JSON_ACTION, DIRECTORY_PATH, OUTPUT_JSON, "extra" };
        try (MockedStatic<FileUtil> utilities = mockStatic(FileUtil.class)) {
            utilities.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
            Exception exception = assertThrows(InvalidArgumentException.class, () -> {
                ArgumentValidator.validateArguments(args);
            });
            assertTrue(exception.getMessage().contains("Usage for json action"),
                    "Expected exception for invalid number of arguments for json action");
        }
    }

    @Test
    public void testValidateArgumentsInsufficientArgumentsForReplace() {
        String[] args = { ArgumentValidator.REPLACE_ACTION, DIRECTORY_PATH, "[import file.odt]" };
        try (MockedStatic<FileUtil> utilities = mockStatic(FileUtil.class)) {
            utilities.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
            Exception exception = assertThrows(InvalidArgumentException.class, () -> {
                ArgumentValidator.validateArguments(args);
            });
            assertTrue(exception.getMessage().contains("Usage for replace action"),
                    "Expected exception for insufficient arguments for replace action");
        }
    }

    @Test
    public void testValidateArgumentsInvalidImportBlockToReplace() {
        String[] args = { ArgumentValidator.REPLACE_ACTION, DIRECTORY_PATH, "invalidBlock", "[import newfile.odt]" };
        try (MockedStatic<FileUtil> utilities = mockStatic(FileUtil.class)) {
            utilities.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
            Exception exception = assertThrows(InvalidArgumentException.class, () -> {
                ArgumentValidator.validateArguments(args);
            });
            assertTrue(exception.getMessage().contains("Invalid format of 'block to replace'"),
                    "Expected exception for invalid 'block to replace'");
        }
    }

    @Test
    public void testValidateArgumentsInvalidNewImportBlock() {
        String[] args = { ArgumentValidator.REPLACE_ACTION, DIRECTORY_PATH, "[import file.odt]", "invalidBlock" };
        try (MockedStatic<FileUtil> utilities = mockStatic(FileUtil.class)) {
            utilities.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
            Exception exception = assertThrows(InvalidArgumentException.class, () -> {
                ArgumentValidator.validateArguments(args);
            });
            assertTrue(exception.getMessage().contains("Invalid format of 'new block'"),
                    "Expected exception for invalid 'new block'");
        }
    }

    @Test
    public void testIsValidImportBlockValidBlock() {
        assertTrue(ArgumentValidator.isValidImportBlock("[import file.odt]"));
        assertTrue(ArgumentValidator.isValidImportBlock("[import file1.odt]"));
        assertTrue(ArgumentValidator.isValidImportBlock("[import 1.odt]"));
        assertTrue(ArgumentValidator.isValidImportBlock("[import 1_file.odt]"));
        assertTrue(ArgumentValidator.isValidImportBlock("[import file_1.odt]"));
    }

    @Test
    public void testIsValidImportBlockInvalidBlock() {
        assertFalse(ArgumentValidator.isValidImportBlock("invalidBlock"));
        assertFalse(ArgumentValidator.isValidImportBlock("file.odt"));
        assertFalse(ArgumentValidator.isValidImportBlock("import  file.odt"));
        assertFalse(ArgumentValidator.isValidImportBlock("import_file.odt"));
        assertFalse(ArgumentValidator.isValidImportBlock("import  1.odt"));
        assertFalse(ArgumentValidator.isValidImportBlock("1.odt"));
        assertFalse(ArgumentValidator.isValidImportBlock("import file"));
        assertFalse(ArgumentValidator.isValidImportBlock("import file.csv"));
        assertFalse(ArgumentValidator.isValidImportBlock("file.odt import"));
        assertFalse(ArgumentValidator.isValidImportBlock(""));
    }

    @Test
    public void testValidateArgumentsInvalidDirectoryPathJsonAction() {
        String[] args = { ArgumentValidator.JSON_ACTION, DIRECTORY_PATH, OUTPUT_JSON };
        try (MockedStatic<FileUtil> utilities = mockStatic(FileUtil.class)) {
            utilities.when(() -> FileUtil.isValidPath(eq(Paths.get(OUTPUT_JSON).getParent())))
                    .thenReturn(true);
            Exception exception = assertThrows(InvalidArgumentException.class, () -> {
                ArgumentValidator.validateArguments(args);
            });
            assertTrue(exception.getMessage().contains(String.format("Invalid path provided: %s", DIRECTORY_PATH)),
                    "Expected exception for invalid path");
        }
    }

    @Test
    public void testValidateArgumentsInvalidJsonPathJsonAction() {
        String[] args = { ArgumentValidator.JSON_ACTION, DIRECTORY_PATH, OUTPUT_JSON };
        try (MockedStatic<FileUtil> utilities = mockStatic(FileUtil.class)) {
            utilities.when(() -> FileUtil.isValidPath(DIRECTORY_PATH)).thenReturn(true);
            Exception exception = assertThrows(InvalidArgumentException.class, () -> {
                ArgumentValidator.validateArguments(args);
            });
            assertTrue(
                    exception.getMessage().contains(
                            String.format("The directory does not exist: %s", Paths.get(OUTPUT_JSON).getParent())),
                    "Expected exception for invalid path");
        }
    }

    @Test
    public void testValidateArgumentsInvalidDirectoryPathReplaceAction() {
        String[] args = { ArgumentValidator.REPLACE_ACTION, DIRECTORY_PATH, "[import file.odt]", "invalidBlock" };
        Exception exception = assertThrows(InvalidArgumentException.class, () -> {
            ArgumentValidator.validateArguments(args);
        });
        assertTrue(exception.getMessage().contains(String.format("Invalid path provided: %s", DIRECTORY_PATH)),
                "Expected exception for invalid path");

    }
}
