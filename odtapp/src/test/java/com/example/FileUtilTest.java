package com.example;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.example.exception.AbortException;
import com.example.exception.InvalidFileException;
import com.example.model.JsonItem;
import com.example.util.FileUtil;
import com.example.util.JsonUtil;

public class FileUtilTest {

    private static final String TEST_TEMPLATES_DIRECTORY = "src/test/resources/test_templates";
    private static final String TEST_TEMPLATES_DIRECTORY_NO_ODT_FILES = "src/test/resources/test_templates/subdirectory_no_odt_files";
    private static final String TEST_TEMPLATES_ZIP = "src/test/resources/test_templates.zip";
    private static final String OUTPUT_TEST_TEMPLATES_JSON = "src/test/resources/output_test_templates.json";
    private static final String REFERENCE_TEST_TEMPLATES_JSON = "src/test/resources/reference_test_templates.json";
    private static final String REFERENCE_TEST_DIRECTORY_JSON = "src/test/resources/reference_test_directory.json";
    private static final String OUTPUT_TEST_DIRECTORY_JSON = "src/test/resources/output_test_directory.json";
    private static final String TEST_DIRECTORY = "src/test/resources/test_directory";
    private static final String SUBDIR1 = TEST_DIRECTORY + "/subdir1";
    private static final String SUBDIR2 = TEST_DIRECTORY + "/subdir2";
    private static final String SUBSUBDIR1 = SUBDIR2 + "/subsubdir1";
    private static final String TEST_FILE1_ODT = "test_file1.odt";
    private static final String TEST_FILE2_TXT = "test_file2.txt";
    private static final String TEST_FILE3_ODT = "test_file3.odt";
    private static final String TEST_FILE4_TXT = "test_file4.txt";
    private static final String TEST_FILE5_ODT = "test_file5.odt";

    private static final String NON_EXISTENT_DIRECTORY = "src/test/resources/non_existent_directory";
    private static final String NON_EXISTENT_FILE = "src/test/resources/non_existent_file.txt";

    @BeforeAll
    public static void setUp() throws Exception {
        // Create test directory and files
        Files.createDirectories(Path.of(SUBDIR1));
        Files.createDirectories(Path.of(SUBDIR2));
        Files.createDirectories(Path.of(SUBSUBDIR1));

        // Create files in subdir1
        Files.createFile(Path.of(SUBDIR1, TEST_FILE1_ODT));
        Files.createFile(Path.of(SUBDIR1, TEST_FILE2_TXT));

        // Create files in subsubdir1 within subdir2
        Files.createFile(Path.of(SUBSUBDIR1, TEST_FILE3_ODT));
        Files.createFile(Path.of(SUBSUBDIR1, TEST_FILE4_TXT));

        // Create file in the root of test_directory
        Files.createFile(Path.of(TEST_DIRECTORY, TEST_FILE5_ODT));
        System.gc();
        Thread.sleep(2000);
    }

    @AfterAll
    public static void cleanUp() throws Exception {
        System.gc();
        Thread.sleep(2000);
        Files.deleteIfExists(Path.of(TEST_DIRECTORY, TEST_FILE5_ODT));
        Files.deleteIfExists(Path.of(SUBSUBDIR1, TEST_FILE3_ODT));
        Files.deleteIfExists(Path.of(SUBSUBDIR1, TEST_FILE4_TXT));
        Files.deleteIfExists(Path.of(SUBDIR1, TEST_FILE1_ODT));
        Files.deleteIfExists(Path.of(SUBDIR1, TEST_FILE2_TXT));
        Files.deleteIfExists(Path.of(SUBSUBDIR1));
        Files.deleteIfExists(Path.of(SUBDIR1));
        Files.deleteIfExists(Path.of(SUBDIR2));
        Files.deleteIfExists(Path.of(TEST_DIRECTORY));
    }

    @BeforeEach
    public void setUpTestTemplates() throws Exception {
        // Unzip the test templates archive into the test directory
        FileUtil.unzip(Paths.get(TEST_TEMPLATES_ZIP),
                Paths.get(TEST_TEMPLATES_DIRECTORY));
        System.gc();
        Thread.sleep(2000);
    }

    @AfterEach
    public void cleanUpTestTemplates() throws Exception {
        System.gc();
        Thread.sleep(2000);
        Path testDir = Paths.get(TEST_TEMPLATES_DIRECTORY);
        // Delete test templates directory
        if (Files.exists(testDir)) {
            FileUtil.deleteDirectory(testDir);
        }
    }

    @Test
    public void testListOdtFiles() {
        List<Path> odtFiles = FileUtil.listOdtFiles(TEST_DIRECTORY);
        assertEquals(3, odtFiles.size(), "There should be three .odt files in the directory structure");
        assertTrue(odtFiles.stream().anyMatch(path -> path.toString().endsWith(TEST_FILE1_ODT)),
                "The file should be in subdir1");
        assertTrue(odtFiles.stream().anyMatch(path -> path.toString().endsWith(TEST_FILE3_ODT)),
                "The file should be in subsubdir1 within subdir2");
        assertTrue(odtFiles.stream().anyMatch(path -> path.toString().endsWith(TEST_FILE5_ODT)),
                "The file should be in the root of test_directory");
    }

    @Test
    public void testListOdtFilesVisitFileFailed() throws IOException {
        Path startPath = Paths.get("testDir");
        Path failedFilePath = startPath.resolve("failedFile.odt");

        // Mock the static methods of Files class
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {

            // Mock the behavior of Files.walkFileTree to simulate visitFileFailed
            filesMock.when(() -> Files.walkFileTree(eq(startPath), anySet(), anyInt(), any()))
                    .thenAnswer(invocation -> {
                        SimpleFileVisitor<Path> visitor = invocation.getArgument(3);
                        visitor.visitFileFailed(failedFilePath, new IOException("Simulated failure"));
                        return null;
                    });
            List<Path> result = FileUtil.listOdtFiles(startPath.toString());
            assertEquals(Collections.emptyList(), result,
                    "Verify that the result is empty, as no .odt files should be added");
        }
    }

    @Test
    public void testGenerateDataForDirectory() {
        JsonItem jsonItem = FileUtil.generateData(new File(TEST_DIRECTORY));
        assertEquals("directory", jsonItem.getType(), "The JsonItem type should be directory");
        assertFalse(jsonItem.getChildren().isEmpty(), "The directory should have children");
    }

    @Test
    public void testGenerateDataForOdtFile() {
        JsonItem jsonItem = FileUtil.generateData(new File(Path.of(SUBDIR1, TEST_FILE1_ODT).toString()));
        assertEquals(JsonItem.FILE_TYPE, jsonItem.getType(), "The JsonItem type should be file");
        assertEquals(TEST_FILE1_ODT, jsonItem.getName(), "The JsonItem name should be the .odt file name");
    }

    @Test
    public void testGenerateDataForNonOdtFile() {
        JsonItem jsonItem = FileUtil.generateData(new File(Path.of(SUBDIR1, TEST_FILE2_TXT).toString()));
        assertEquals(null, jsonItem, "The JsonItem should be null for non .odt files");
    }

    @Test
    public void testCreateJsonFileForTestDirectory() throws Exception {
        FileUtil.createJsonFile(TEST_DIRECTORY, OUTPUT_TEST_DIRECTORY_JSON);

        File outputFile = new File(OUTPUT_TEST_DIRECTORY_JSON);
        assertTrue(outputFile.exists(), "The JSON output file should be created");

        File referenceJson = Paths.get(REFERENCE_TEST_DIRECTORY_JSON).toFile();
        File outputJson = Paths.get(OUTPUT_TEST_DIRECTORY_JSON).toFile();

        assertTrue(JsonUtil.areJsonFilesEqual(referenceJson, outputJson),
                "Assuming referenceJson and outputJson have identical contentl");
    }

    @Test
    public void testThrowsExceptionWhenCreateJsonFileForDirectoryWithoutOdtFile() throws Exception {
        Exception exception = assertThrows(FileNotFoundException.class, () -> {
            FileUtil.createJsonFile(TEST_TEMPLATES_DIRECTORY_NO_ODT_FILES, OUTPUT_TEST_DIRECTORY_JSON);
            ;
        });

        assertTrue(
                exception.getMessage()
                        .contains(String.format("The specified path %s does not contain an odt file.",
                                TEST_TEMPLATES_DIRECTORY_NO_ODT_FILES)),
                "Expected exception for empty directory");
    }

    @Test
    public void testCreateJsonFileForNonExistentDirectory() throws Exception {
        String nonExistentDirectoryPath = "NonExistentDirectory";

        Exception exception = assertThrows(FileNotFoundException.class, () -> {
            FileUtil.createJsonFile(nonExistentDirectoryPath, OUTPUT_TEST_DIRECTORY_JSON);
        });

        assertTrue(
                exception.getMessage()
                        .contains(String.format("The specified path %s does not exist.",
                                nonExistentDirectoryPath)),
                "Expected exception for non existent path");
    }

    @Test
    public void testCreateJsonFileForTestTemplates() throws Exception {
        FileUtil.createJsonFile(TEST_TEMPLATES_DIRECTORY, OUTPUT_TEST_TEMPLATES_JSON);

        File outputFile = new File(OUTPUT_TEST_DIRECTORY_JSON);
        assertTrue(outputFile.exists(), "The JSON output file should be created");

        File referenceJson = Paths.get(REFERENCE_TEST_TEMPLATES_JSON).toFile();
        File outputJson = Paths.get(OUTPUT_TEST_TEMPLATES_JSON).toFile();
        assertTrue(JsonUtil.areJsonFilesEqual(referenceJson, outputJson), "The JSON files should be equal");
    }

    @Test
    public void testThrowsExceptionForReplaceBlocksInNonExistentDirectory() {
        String nonExistentDirectoryPath = "NonExistentDirectory";
        String blockToReplace = "[import oldfile.odt]";
        String newBlock = "[import newfile.odt]";

        Exception exception = assertThrows(FileNotFoundException.class, () -> {
            FileUtil.replaceBlocks(nonExistentDirectoryPath, blockToReplace, newBlock);
        });

        assertTrue(
                exception.getMessage()
                        .contains(String.format("The specified path %s does not exist.",
                                nonExistentDirectoryPath)),
                "Expected exception for non existent path");
    }

    @Test
    public void testReplaceBlocks() {
        String blockToReplace = "[import oldfile.odt]";
        String newBlock = "[import newfile.odt]";
        assertDoesNotThrow(() -> {
            FileUtil.replaceBlocks(TEST_TEMPLATES_DIRECTORY, blockToReplace, newBlock);
        });
    }

    @Test
    public void testReplaceImportBlockInDirectory() {
        Path template09Path = Paths.get(TEST_TEMPLATES_DIRECTORY, "subdirectory", "subsubdirectory3",
                "template_09.odt");
        Path template10Path = Paths.get(TEST_TEMPLATES_DIRECTORY, "subdirectory", "subsubdirectory3",
                "template_10.odt");
        Path template11Path = Paths.get(TEST_TEMPLATES_DIRECTORY, "subdirectory", "subsubdirectory3",
                "template_11.odt");

        List<String> importBlocksBeforeReplaceTemplate10 = FileUtil.getImportBlocks(template10Path);

        String blockToReplace = "[import block_1.odt]";
        String newBlock = "[import block_1_test.odt]";

        assertDoesNotThrow(() -> {
            FileUtil.replaceBlocks(Paths.get(TEST_TEMPLATES_DIRECTORY, "subdirectory", "subsubdirectory3").toString(),
                    blockToReplace, newBlock);
        });

        List<String> importBlocksAfterReplaceTemplate09 = FileUtil.getImportBlocks(template09Path);
        assertEquals(3, importBlocksAfterReplaceTemplate09.size());
        assertTrue(importBlocksAfterReplaceTemplate09.contains(newBlock));
        assertTrue(importBlocksAfterReplaceTemplate09.contains("[import footer_1.odt]"));
        assertTrue(importBlocksAfterReplaceTemplate09.contains("[import header_1.odt]"));

        List<String> importBlocksAfterReplaceTemplate10 = FileUtil.getImportBlocks(template10Path);
        assertEquals(importBlocksBeforeReplaceTemplate10, importBlocksAfterReplaceTemplate10);

        List<String> importBlocksAfterReplaceTemplate11 = FileUtil.getImportBlocks(template11Path);
        assertEquals(3, importBlocksAfterReplaceTemplate11.size());
        assertTrue(importBlocksAfterReplaceTemplate11.contains(newBlock));
        assertTrue(importBlocksAfterReplaceTemplate11.contains("[import footer_1.odt]"));
        assertTrue(importBlocksAfterReplaceTemplate11.contains("[import header_1.odt]"));
    }

    @Test
    public void testReplaceImportBlockInDirectory2() {
        Path template01Path = Paths.get(TEST_TEMPLATES_DIRECTORY, "subdirectory", "subsubdirectory1",
                "template_01.odt");
        Path template05Path = Paths.get(TEST_TEMPLATES_DIRECTORY, "subdirectory", "subsubdirectory1",
                "template_05.odt");
        Path template07Path = Paths.get(TEST_TEMPLATES_DIRECTORY, "subdirectory", "subsubdirectory1",
                "subsubsubdirectory", "template_07.odt");
        Path template08Path = Paths.get(TEST_TEMPLATES_DIRECTORY, "subdirectory", "subsubdirectory1",
                "subsubsubdirectory", "template_08.odt");
        Path template12Path = Paths.get(TEST_TEMPLATES_DIRECTORY, "subdirectory", "subsubdirectory1",
                "subsubsubdirectory", "template_12.odt");

        List<String> importBlocksBeforeReplaceTemplate05 = FileUtil.getImportBlocks(template05Path);
        List<String> importBlocksBeforeReplaceTemplate07 = FileUtil.getImportBlocks(template07Path);
        List<String> importBlocksBeforeReplaceTemplate08 = FileUtil.getImportBlocks(template08Path);

        String blockToReplace = "[import block_1a.odt]";
        String newBlock = "[import block_1a_test.odt]]";

        assertDoesNotThrow(() -> {
            FileUtil.replaceBlocks(Paths.get(TEST_TEMPLATES_DIRECTORY, "subdirectory", "subsubdirectory1").toString(),
                    blockToReplace, newBlock);
        });

        // Check ODT file with replaced block
        List<String> importBlocksAfterReplaceTemplate01 = FileUtil.getImportBlocks(template01Path);
        assertEquals(4, importBlocksAfterReplaceTemplate01.size());
        assertTrue(importBlocksAfterReplaceTemplate01.contains(newBlock),
                "Expected new replaced import block");
        assertTrue(importBlocksAfterReplaceTemplate01.contains("[import block_1.odt]"));
        assertTrue(importBlocksAfterReplaceTemplate01.contains("[import footer_1.odt]"));
        assertTrue(importBlocksAfterReplaceTemplate01.contains("[import header_1.odt]"));

        // ODT with no replaces
        List<String> importBlocksAfterReplaceTemplate05 = FileUtil.getImportBlocks(template05Path);
        assertEquals(importBlocksBeforeReplaceTemplate05, importBlocksAfterReplaceTemplate05,
                "Expected no replaces");

        // ODT with no replaces
        List<String> importBlocksAfterReplaceTemplate07 = FileUtil.getImportBlocks(template07Path);
        assertEquals(importBlocksBeforeReplaceTemplate07, importBlocksAfterReplaceTemplate07,
                "Expected no replaces");

        // ODT with no replaces
        List<String> importBlocksAfterReplaceTemplate08 = FileUtil.getImportBlocks(template08Path);
        assertEquals(importBlocksBeforeReplaceTemplate08, importBlocksAfterReplaceTemplate08,
                "Expected no replaces");

        // Check ODT file with replaced block
        List<String> importBlocksAfterReplaceTemplate12 = FileUtil.getImportBlocks(template12Path);
        assertEquals(4, importBlocksAfterReplaceTemplate12.size());
        assertTrue(importBlocksAfterReplaceTemplate12.contains(newBlock),
                "Expected new replaced import block");
        assertTrue(importBlocksAfterReplaceTemplate12.contains("[import block_1.odt]"));
        assertTrue(importBlocksAfterReplaceTemplate12.contains("[import footer_1.odt]"));
    }

    @Test
    public void testThrowsExceptionForReplaceImportBlockInNotOdtFile() {
        String blockToReplace = "[import block_1.odt]";
        String newBlock = "[import block_1_test.odt]";
        String filePath = Paths.get(TEST_TEMPLATES_DIRECTORY, "template.txt").toString();

        Exception exception = assertThrows(InvalidFileException.class, () -> {
            FileUtil.replaceBlocks(filePath, blockToReplace, newBlock);
        });

        assertTrue(
                exception.getMessage()
                        .contains(String.format("The specified file %s is not ODT file.",
                                filePath)),
                "Expected exception for not ODT file");
    }

    @Test
    public void testThrowsExceptionForReplaceImportBlockInDirectoryWithoutOdtFiles() {
        String blockToReplace = "[import block_1.odt]";
        String newBlock = "[import block_1_test.odt]";
        String directoryPath = Paths.get(TEST_TEMPLATES_DIRECTORY, "subdirectory_no_odt_files").toString();

        Exception exception = assertThrows(FileNotFoundException.class, () -> {
            FileUtil.replaceBlocks(directoryPath, blockToReplace, newBlock);
        });

        assertTrue(
                exception.getMessage()
                        .contains(String.format("The specified path %s does not contains ODT files.",
                                directoryPath)),
                "Expected exception for directory with no ODT files");
    }

    @Test
    public void testCheckAndPromptOverwriteYes() {
        Path jsonOutputPath = Paths.get(OUTPUT_TEST_DIRECTORY_JSON);
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(jsonOutputPath)).thenReturn(true);
            Scanner scannerMock = mock(Scanner.class);
            when(scannerMock.nextLine()).thenReturn("yes");
            System.setIn(new java.io.ByteArrayInputStream("yes\n".getBytes()));
            assertDoesNotThrow(() -> {
                FileUtil.checkAndPromptOverwrite(jsonOutputPath.toString());
            });
        }
    }

    @Test
    public void testCheckAndPromptOverwriteNo() {
        Path jsonOutputPath = Paths.get(OUTPUT_TEST_DIRECTORY_JSON);
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(jsonOutputPath)).thenReturn(true);
            Scanner scannerMock = mock(Scanner.class);
            when(scannerMock.nextLine()).thenReturn("no");
            System.setIn(new java.io.ByteArrayInputStream("no\n".getBytes()));
            assertThrows(AbortException.class, () -> {
                FileUtil.checkAndPromptOverwrite(jsonOutputPath.toString());
            });
        }
    }

    public static boolean isValidPath(Path path) {
        File file = new File(path.toString());
        if (!file.exists() || (!file.isFile() && !file.isDirectory())) {
            return false;
        } else {
            return true;
        }
    }

    @Test
    public void testIsValidPathForDirectory() {
        assertTrue(FileUtil.isValidPath(TEST_TEMPLATES_DIRECTORY));
    }

    @Test
    public void testIsValidPathForFile() {
        assertTrue(FileUtil.isValidPath(TEST_TEMPLATES_ZIP));
    }

    @Test
    public void testIsValidPathForDirectoryPath() {
        assertTrue(FileUtil.isValidPath(Paths.get(TEST_TEMPLATES_DIRECTORY)));
    }

    @Test
    public void testIsValidPathForFilePath() {
        assertTrue(FileUtil.isValidPath(Paths.get(TEST_TEMPLATES_ZIP)));
    }

    @Test
    public void testIsNotValidPathForDirectory() {
        assertFalse(FileUtil.isValidPath(NON_EXISTENT_DIRECTORY));
    }

    @Test
    public void testIsNotValidPathForFile() {
        assertFalse(FileUtil.isValidPath(NON_EXISTENT_FILE));
    }

    @Test
    public void testIsNotValidPathForDirectoryPath() {
        assertFalse(FileUtil.isValidPath(Paths.get(NON_EXISTENT_DIRECTORY)));
    }

    @Test
    public void testIsNotValidPathForFilePath() {
        assertFalse(FileUtil.isValidPath(Paths.get(NON_EXISTENT_FILE)));
    }

}
