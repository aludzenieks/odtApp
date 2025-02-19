package com.example;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import org.xml.sax.SAXException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.ProviderNotFoundException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import com.example.odt.OdtFile;
import com.example.util.FileUtil;

public class OdtFileTest {

    @Mock
    private OdtFile odtFileMock;

    private static final String TEST_TEMPLATES_ZIP = "src/test/resources/test_templates.zip";
    private static final String TEST_TEMPLATES_DIRECTORY = "src/test/resources/test_templates";

    @BeforeEach
    public void setUpTestTemplates() throws Exception {
        // Unzip the test templates archive into the test directory
        FileUtil.unzip(Paths.get(TEST_TEMPLATES_ZIP),
                Paths.get(TEST_TEMPLATES_DIRECTORY));

        System.gc();
        Thread.sleep(3000);
    }

    @AfterEach
    public void cleanUpTestTemplates() throws Exception {
        System.gc();
        Thread.sleep(3000);
        Path testDir = Paths.get(TEST_TEMPLATES_DIRECTORY);
        if (Files.exists(testDir)) {
            FileUtil.deleteDirectory(testDir);
        }
    }

    @Test
    public void testReplaceImportBlockInHeader() {

        String blockToReplace = "[import footer_1.odt]";
        String newBlock = "[import footer_1_test.odt]";
        assertDoesNotThrow(() -> {
            OdtFile file = new OdtFile(Paths.get(TEST_TEMPLATES_DIRECTORY, "template_bb02.odt").toString());
            file.replaceImportBlocks(blockToReplace, newBlock);
        });

        // Verify that the block has been replaced
        List<String> importBlocksAfterReplace = FileUtil
                .getImportBlocks(Paths.get(TEST_TEMPLATES_DIRECTORY, "template_bb02.odt"));
        assertEquals(3, importBlocksAfterReplace.size());
        assertTrue(importBlocksAfterReplace.contains(newBlock),
                "The new block should be present in the file");
        assertTrue(importBlocksAfterReplace.contains("[import block_1.odt]"));
        assertTrue(importBlocksAfterReplace.contains("[import header_1.odt]"));
    }

    @Test
    public void testReplaceImportBlockInFooter() {

        String blockToReplace = "[import header_1.odt]";
        String newBlock = "[import header_1_test.odt]";

        assertDoesNotThrow(() -> {
            OdtFile file = new OdtFile(Paths.get(TEST_TEMPLATES_DIRECTORY, "template_bb02.odt").toString());
            file.replaceImportBlocks(blockToReplace, newBlock);
        });

        // Verify that the block has been replaced
        List<String> importBlocksAfterReplace = FileUtil
                .getImportBlocks(Paths.get(TEST_TEMPLATES_DIRECTORY, "template_bb02.odt"));
        assertEquals(3, importBlocksAfterReplace.size());
        assertTrue(importBlocksAfterReplace.contains(newBlock),
                "The new block should be present in the file");
        assertTrue(importBlocksAfterReplace.contains("[import block_1.odt]"));
        assertTrue(importBlocksAfterReplace.contains("[import footer_1.odt]"));
    }

    @Test
    public void testReplaceImportBlockInContent() {

        String blockToReplace = "[import block_1.odt]";
        String newBlock = "[import block_1_test.odt]";

        assertDoesNotThrow(() -> {
            OdtFile file = new OdtFile(Paths.get(TEST_TEMPLATES_DIRECTORY, "template_bb02.odt").toString());
            file.replaceImportBlocks(blockToReplace, newBlock);
        });

        // Verify that the block has been replaced
        List<String> importBlocksAfterReplace = FileUtil
                .getImportBlocks(Paths.get(TEST_TEMPLATES_DIRECTORY, "template_bb02.odt"));
        assertEquals(3, importBlocksAfterReplace.size());
        assertTrue(importBlocksAfterReplace.contains(newBlock),
                "The new block should be present in the file");
        assertTrue(importBlocksAfterReplace.contains("[import header_1.odt]"));
        assertTrue(importBlocksAfterReplace.contains("[import footer_1.odt]"));
    }

    @Test
    public void testDoNotReplaceNonExistentImportBlock() {

        String blockToReplace = "[import block_10.odt]";
        String newBlock = "[import block_10_test.odt]";

        assertDoesNotThrow(() -> {
            OdtFile file = new OdtFile(Paths.get(TEST_TEMPLATES_DIRECTORY, "template_bb02.odt").toString());
            file.replaceImportBlocks(blockToReplace, newBlock);
        });

        // Verify that the block has been replaced
        List<String> importBlocksAfterReplace = FileUtil
                .getImportBlocks(Paths.get(TEST_TEMPLATES_DIRECTORY, "template_bb02.odt"));
        assertEquals(3, importBlocksAfterReplace.size());
        assertTrue(importBlocksAfterReplace.contains("[import block_1.odt]"));
        assertTrue(importBlocksAfterReplace.contains("[import header_1.odt]"));
        assertTrue(importBlocksAfterReplace.contains("[import footer_1.odt]"));
    }

    @Test
    public void testDoNotReplaceImportBlockInInvalidOdtFile() {

        String blockToReplace = "[import block_1.odt]";
        String newBlock = "[import block_1_test.odt]";

        assertThrows(ProviderNotFoundException.class, () -> {
            OdtFile file = new OdtFile(
                    Paths.get(TEST_TEMPLATES_DIRECTORY, "subdirectory2", "invalid_odt_file.odt").toString());
            file.replaceImportBlocks(blockToReplace, newBlock);
        });
    }

    @Test
    public void testThrowExceptionWhenReplaceImportBlockInNotReadableFile() {

        String blockToReplace = "[import block_10.odt]";
        String newBlock = "[import block_10_test.odt]";
        OdtFile file = new OdtFile(Paths.get(TEST_TEMPLATES_DIRECTORY, "template_bb02.odt").toString());

        try (MockedStatic<FileUtil> utilities = mockStatic(FileUtil.class)) {
            // Mock the static FileUtil.canRead method
            utilities.when(() -> FileUtil.canRead(eq(file))).thenReturn(false);
            assertThrows(AccessDeniedException.class, () -> {
                file.replaceImportBlocks(blockToReplace, newBlock);
            });
        }
    }

    @Test
    public void testContainsImportBlockFound() throws IOException, ParserConfigurationException, SAXException {
        String blockToSearch = "[import block_1.odt]";
        OdtFile file = new OdtFile(Paths.get(TEST_TEMPLATES_DIRECTORY, "template_bb02.odt").toString());
        assertTrue(file.containsImportBlock(blockToSearch));
    }

    @Test
    public void testContainsImportBlockNotFound() throws IOException, ParserConfigurationException, SAXException {
        String blockToSearch = "[import block_not_existent.odt]";
        OdtFile file = new OdtFile(Paths.get(TEST_TEMPLATES_DIRECTORY, "template_bb02.odt").toString());
        assertFalse(file.containsImportBlock(blockToSearch));
    }

}
