package com.example;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Paths;

import com.example.util.FileUtil;
import com.example.validation.ArgumentValidator;

@ExtendWith(MockitoExtension.class)
public class AppTest {

    @Test
    public void testJsonActionSuccess() throws Exception {
        try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
            String[] args = { ArgumentValidator.JSON_ACTION, "inputPath", "jsonPath/output.json" };
            fileUtilMock.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
            fileUtilMock.when(() -> FileUtil.isValidPath(eq(Paths.get("jsonPath/output.json").getParent())))
                    .thenReturn(true);
            App.main(args);
            fileUtilMock.verify(() -> FileUtil.createJsonFile("inputPath", "jsonPath/output.json"));
        }
    }

    @Test
    public void testReplaceActionSuccess() throws Exception {
        try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
            String[] args = { ArgumentValidator.REPLACE_ACTION, "directoryOrFilePath", "[import block_1.odt]",
                    "[import block_1_new.odt]" };
            fileUtilMock.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
            App.main(args);
            fileUtilMock.verify(() -> FileUtil.replaceBlocks("directoryOrFilePath", "[import block_1.odt]",
                    "[import block_1_new.odt]"));
        }
    }

    @Test
    public void testInvalidActionArgument() throws Exception {
        try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
            String[] args = { "InvalidAction", "directoryOrFilePath", "[import block_1.odt]",
                    "[import block_1_new.odt]" };
            fileUtilMock.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
            App.main(args);
            fileUtilMock.verify(() -> FileUtil.replaceBlocks("directoryOrFilePath", "[import block_1.odt]",
                    "[import block_1_new.odt]"), never());
        }
    }

    @Test
    public void testInvalidImportBlockToReplacedArgumentForReplaceAction() throws Exception {
        try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
            String[] args = { ArgumentValidator.REPLACE_ACTION, "directoryOrFilePath", "[block_1.odt]",
                    "[import block_1_new.odt]" };
            fileUtilMock.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
            App.main(args);
            fileUtilMock.verify(() -> FileUtil.replaceBlocks("directoryOrFilePath", "[block_1.odt]",
                    "[import block_1_new.odt]"), never());
        }
    }

    @Test
    public void testInvalidNewImportBlockArgumentForReplaceAction() throws Exception {
        try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
            String[] args = { ArgumentValidator.REPLACE_ACTION, "directoryOrFilePath", "[import block_1.odt]",
                    "[block_1_new.odt]" };
            fileUtilMock.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
            App.main(args);
            fileUtilMock.verify(() -> FileUtil.replaceBlocks("directoryOrFilePath", "[import block_1.odt]",
                    "[block_1_new.odt]"), never());
        }
    }

    @Test
    public void testInvalidNumberOfArgumentsForReplaceAction() throws Exception {
        try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
            String[] args = { ArgumentValidator.REPLACE_ACTION, "directoryOrFilePath", "[import block_1.odt]" };
            fileUtilMock.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
            App.main(args);
            fileUtilMock.verify(() -> FileUtil.replaceBlocks("directoryOrFilePath", "[import block_1.odt]",
                    null), never());
        }
    }

    @Test
    public void testInvalidNumberOfArgumentsForJsonAction() throws Exception {
        try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
            String[] args = { ArgumentValidator.JSON_ACTION, "directoryOrFilePath" };
            fileUtilMock.when(() -> FileUtil.isValidPath(anyString())).thenReturn(true);
            App.main(args);
            fileUtilMock.verify(() -> FileUtil.createJsonFile("directoryOrFilePath", null), never());
        }
    }

    @Test
    public void testInvalidDirectoryPathArgumentForReplaceAction() throws Exception {
        try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
            String[] args = { ArgumentValidator.REPLACE_ACTION, "directoryOrFilePath", "[import block_1.odt]",
                    "[import block_1_new.odt]" };
            App.main(args);
            fileUtilMock.verify(() -> FileUtil.replaceBlocks("directoryOrFilePath", "[import block_1.odt]",
                    "[import block_1_new.odt]"), never());
        }
    }

    @Test
    public void testInvalidDirectoryPathArgumentsForJsonAction() throws Exception {
        try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
            String[] args = { ArgumentValidator.JSON_ACTION, "directoryOrFilePath", "jsonPath/output.json" };
            fileUtilMock.when(() -> FileUtil.isValidPath(eq(Paths.get("jsonPath/output.json").getParent())))
                    .thenReturn(true);
            App.main(args);
            fileUtilMock.verify(() -> FileUtil.createJsonFile(null, "jsonPath/output.json"), never());
        }
    }

    @Test
    public void testInvalidJsonPathArgumentsForJsonAction() throws Exception {
        try (MockedStatic<FileUtil> fileUtilMock = mockStatic(FileUtil.class)) {
            String[] args = { ArgumentValidator.JSON_ACTION, "directoryOrFilePath", "jsonPath/output.json" };
            fileUtilMock.when(() -> FileUtil.isValidPath("directoryOrFilePath")).thenReturn(true);
            App.main(args);
            fileUtilMock.verify(() -> FileUtil.createJsonFile("directoryOrFilePath", null), never());
        }
    }

}
