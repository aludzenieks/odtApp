package com.example.util;

import com.example.exception.AbortException;
import com.example.exception.InvalidFileException;
import com.example.model.JsonItem;
import com.example.odt.OdtFile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

    private static final Logger LOGGER = Logger.getLogger(FileUtil.class.getName());
    public static final String FILE_EXTENSION_ODT = ".odt";

    /**
     * Check if file extension is .odt.
     *
     * @param file The file to check.
     * @return true if filename ends with .odt extension.
     */
    public static boolean isOdtFile(File file) {
        return file.getName().toLowerCase().endsWith(FILE_EXTENSION_ODT);
    }

    /**
     * Check if file extension is .odt.
     *
     * @param path The path to check.
     * @return true if filename ends with .odt extension.
     */
    public static boolean isOdtFile(String path) {
        return path.toLowerCase().endsWith(FILE_EXTENSION_ODT);
    }

    /**
     * Check if file is readable.
     *
     * @param path The file to check.
     * @return true if file is readable.
     */
    public static boolean canRead(File file) {
        return file.canRead();
    }

    /**
     * Check if file exists.
     *
     * @param path The path string to check.
     * @return true if file exists and is file or directory.
     */
    public static boolean isValidPath(String path) {
        File file = new File(path);
        if (!file.exists() || (!file.isFile() && !file.isDirectory())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check if file exists.
     *
     * @param path The path to check.
     * @return true if file exists and is file or directory.
     */
    public static boolean isValidPath(Path path) {
        File file = new File(path.toString());
        if (!file.exists() || (!file.isFile() && !file.isDirectory())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checks if file for path string exists and promts file overwrite.
     *
     * @param pathString The file path in string.
     *
     * @throws AbortException if user aborted file overwrite.
     */
    public static void checkAndPromptOverwrite(String pathString) throws AbortException {
        // Convert the provided path string to a Path object
        Path outputPath = Paths.get(pathString);

        // Check if the file exists at the specified path
        if (Files.exists(outputPath)) {
            // Open a Scanner to read user input from the console
            try (Scanner scanner = new Scanner(System.in)) {
                // Prompt the user for confirmation to overwrite the existing file
                System.out.println(
                        String.format("File %s already exists. Do you want to overwrite it? (yes/no): ", pathString));

                // Read the user's response
                String response = scanner.nextLine();

                // If the user's response is not "yes" (case-insensitive), throw an
                // AbortException
                if (!response.equalsIgnoreCase("yes")) {
                    throw new AbortException("Operation aborted by the user. JSON file not overwritten.");
                }
            }
        }
    }

    /**
     * Unzips a zip archive into a target directory.
     *
     * @param sourceZip       The source zip file.
     * @param targetDirectory The target directory where the zip content will be
     *                        extracted.
     * @throws IOException if an I/O error occurs.
     */
    public static void unzip(Path sourceZip, Path targetDirectory) throws IOException {
        // Open a ZipInputStream for reading the contents of the zip file
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(sourceZip))) {
            ZipEntry entry;

            // Iterate through each entry in the zip file
            while ((entry = zipInputStream.getNextEntry()) != null) {
                // Resolve the target path for the current entry
                Path targetPath = targetDirectory.resolve(entry.getName()).normalize();

                // If the entry is a directory, create the directory
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    // If the entry is a file, create its parent directories if needed
                    Files.createDirectories(targetPath.getParent());
                    // Copy the file from the zip input stream to the target path
                    Files.copy(zipInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /**
     * Deletes a directory recursively.
     *
     * @param path The path of the directory to delete.
     * @throws IOException if an I/O error occurs.
     */
    public static void deleteDirectory(Path path) throws IOException {
        // Walk the file tree starting from the specified path
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    // Attempt to delete the file
                    Files.delete(file);
                } catch (NoSuchFileException e) {
                    // Ignore as the file might have been already deleted
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                try {
                    // Attempt to delete the directory
                    Files.delete(dir);
                } catch (NoSuchFileException e) {
                    // Ignore as the directory might have been already deleted
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Lists all .odt files in a directory.
     *
     * @param directoryPath The directory path to search.
     * @return A list of paths to .odt files.
     */
    public static List<Path> listOdtFiles(String directoryPath) {
        // List to hold the paths of ODT files found in the directory
        List<Path> odtFiles = new ArrayList<>();

        // Convert the directory path string to a Path object
        Path startPath = Paths.get(directoryPath);

        try {
            // Walk the file tree starting from the specified path
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    // If the file has an ODT extension, add it to the list
                    if (file.toString().toLowerCase().endsWith(FILE_EXTENSION_ODT)) {
                        odtFiles.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exception) {
                    // Log an error message if a file visit fails
                    LOGGER.log(Level.SEVERE, String.format("Failed to access file: %s Error: %s", file.toString(),
                            exception.getMessage()));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            // Log an error message if an I/O exception occurs while walking the file tree
            LOGGER.log(Level.SEVERE, "Error walking file tree: {0}", e.getMessage());
        }

        // Sort the list of ODT files
        Collections.sort(odtFiles);

        // Return the list of ODT files
        return odtFiles;
    }

    /**
     * Get list of import blocks for specified path.
     *
     * @param path The path to process.
     * @return A list of strings of import blocks.
     */
    public static List<String> getImportBlocks(Path path) {
        // List ODT files in the specified path and process each file
        return FileUtil.listOdtFiles(path.toString()).stream()
                .map(odtFilePath -> {
                    try {
                        // Create an OdtFile object for the current file path
                        OdtFile odtFile = new OdtFile(odtFilePath.toString());
                        // Get the import blocks from the OdtFile, or return an empty list if not
                        // present
                        return odtFile.getImportBlocks().orElseGet(ArrayList::new);
                    } catch (Exception e) {
                        // Log an error message if an exception occurs while getting import blocks
                        LOGGER.log(Level.SEVERE,
                                String.format("Failed to get import blocks for file: %s Error: %s",
                                        odtFilePath.toString(), e.getMessage()));
                        // Return an empty list if an error occurs
                        return new ArrayList<String>();
                    }
                })
                // Flatten the list of import blocks from multiple files into a single list
                .flatMap(List::stream)
                // Collect the flattened list into a single list and return it
                .collect(Collectors.toList());
    }

    /**
     * Get JsonItem for file or directory.
     *
     * @param fileOrDirectory The file or directory to process.
     * @return JsonItem.
     */
    public static JsonItem generateData(File fileOrDirectory) {
        // Create a new JsonItem object for the file or directory, specifying the name
        // and type
        JsonItem item = new JsonItem(fileOrDirectory.getName(),
                fileOrDirectory.isDirectory() ? JsonItem.DIRECTORY_TYPE : JsonItem.FILE_TYPE);
        try {
            // Check if the file or directory is readable
            if (!fileOrDirectory.canRead()) {
                // If not readable, set an error message in the JsonItem
                item.setError(String.format("Failed to read %s. It is not readable.", fileOrDirectory.toPath()));
                LOGGER.log(Level.SEVERE, "Failed to read {0}. It is not readable.",
                        fileOrDirectory.toPath().toString());
            } else {
                // If it is a directory, get data for the directory
                if (fileOrDirectory.isDirectory()) {
                    item = getDataForDirectory(fileOrDirectory);
                } else {
                    // If it is a file, create an OdtFile object and get data for the file
                    OdtFile odtFile = new OdtFile(fileOrDirectory.getPath());
                    item = getDataForFile(odtFile);
                }
            }
        } catch (SecurityException e) {
            // If a SecurityException occurs, set an error message in the JsonItem
            item.setError(String.format("Access denied. Error message: %s", e.getMessage()));
            LOGGER.log(Level.SEVERE, String.format("Access denied for %s. Error message: %s",
                    fileOrDirectory.toPath().toString(), e.getMessage()));

        } catch (Exception e) {
            // If any other exception occurs, set the error message in the JsonItem
            item.setError(e.getMessage());
            LOGGER.log(Level.SEVERE,
                    String.format("Failed to process %s. %s", fileOrDirectory.toPath().toString(), e.getMessage()));
        }

        // Return the JsonItem if it is not an empty directory or ODT file, or if it
        // contains an error
        return (item.isDirectoryType() && item.getChildren() != null)
                || (item.isFileType() && FileUtil.isOdtFile(item.getName()))
                || item.getError() != null ? item : null;
    }

    /**
     * Get JsonItem for directory.
     *
     * @param directory The directory to process.
     * @return JsonItem.
     */
    private static JsonItem getDataForDirectory(File directory) {
        // Create a new JsonItem object for the directory, specifying the directory name
        // and type
        JsonItem item = new JsonItem(directory.getName(), JsonItem.DIRECTORY_TYPE);

        // Create a LinkedList to hold the contents of the directory
        LinkedList<JsonItem> folderContents = new LinkedList<>();

        // List the files in the directory
        File[] files = directory.listFiles();

        // Check if the files array is not null
        if (files != null) {
            // Iterate through the files in the directory
            for (File file : files) {
                // Generate data for each file and add it to the folderContents list if not null
                JsonItem childItem = generateData(file);
                if (childItem != null) {
                    folderContents.add(childItem);
                }
            }
        } else {
            // If the files array is null, set an error message in the JsonItem
            item.setError(String.format("Failed to list contents of directory %s. Access denied.", directory.toPath()));
            // Log an error message indicating the failure to list the directory contents
            LOGGER.log(Level.SEVERE, "Failed to list contents of directory {0}. Access denied.",
                    directory.toPath().toString());
        }

        // If the folderContents list is not empty, set it as the children of the
        // JsonItem
        if (!folderContents.isEmpty()) {
            item.setChildren(folderContents);
        }

        // Return the JsonItem object
        return item;
    }

    /**
     * Get JsonItem for file.
     *
     * @param file The file to process.
     * @return JsonItem.
     */
    private static JsonItem getDataForFile(File file) {
        // Create a new JsonItem object for the file, specifying the file name and type
        JsonItem item = new JsonItem(file.getName(), JsonItem.FILE_TYPE);

        // Check if the file is an ODT file
        if (FileUtil.isOdtFile(file)) {
            try {
                // Create an OdtFile object for the file
                OdtFile odtFile = new OdtFile(file.getPath());

                // Get the import blocks from the OdtFile, if any
                Optional<List<String>> importBlocks = odtFile.getImportBlocks();

                // If import blocks are present, set them in the JsonItem
                importBlocks.ifPresent(item::setImportBlocks);
            } catch (Exception e) {
                // If an exception occurs, set the error message in the JsonItem
                item.setError(e.getMessage());

                // Log an error message indicating the file processing failure
                LOGGER.log(Level.SEVERE, String.format("Failed to process file %s. %s", file.toString(),
                        file.toPath().toString(), e.getMessage()));
            }
        }

        // Return the JsonItem object
        return item;
    }

    /**
     * Creates a JSON file representing the directory structure.
     *
     * @param directoryOrFilePath The input directory or file path.
     * @param outputPath          The output JSON file path.
     * @throws FileNotFoundException if the input path does not exist.
     */
    public static void createJsonFile(String directoryOrFilePath, String outputPath) throws FileNotFoundException {
        // Create a File object for the specified directory or file path
        File directoryOrFile = new File(directoryOrFilePath);

        // Check if the directory or file exists
        if (!directoryOrFile.exists()) {
            throw new FileNotFoundException(
                    String.format("The specified path %s does not exist.", directoryOrFilePath));
        }

        // Generate data from the directory or file
        JsonItem jsonItem = generateData(directoryOrFile);

        if (jsonItem == null) {
            throw new FileNotFoundException(
                    String.format("The specified path %s does not contain an odt file.", directoryOrFilePath));
        }

        // Create a Gson object with pretty printing enabled
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Convert the JsonItem object to a JSON string
        String jsonString = gson.toJson(jsonItem);

        // Convert the output path string to a Path object
        Path jsonOutputPath = Paths.get(outputPath);

        // Write the JSON string to the specified output file
        try (BufferedWriter writer = Files.newBufferedWriter(jsonOutputPath)) {
            writer.write(jsonString);
            LOGGER.log(Level.INFO, "JSON file created successfully at {0}", outputPath.toString());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error writing JSON file: {0}", e.getMessage());
        }
    }

    /**
     * Replaces blocks in .odt files within the specified directory.
     *
     * @param directoryOrFilePath The directory or file path.
     * @param blockToReplace      The block to replace.
     * @param newBlock            The new block.
     * @throws FileNotFoundException if the specified path does not exist or
     *                               directory does not contain ODT file.
     * @throws InvalidFileException  if the specified is not ODT file.
     */
    public static void replaceBlocks(String directoryOrFilePath, String blockToReplace, String newBlock)
            throws FileNotFoundException, InvalidFileException {
        // Create a File object for the specified directory or file path
        File directoryOrFile = new File(directoryOrFilePath);

        // Check if the directory or file exists
        if (!directoryOrFile.exists()) {
            throw new FileNotFoundException(
                    String.format("The specified path %s does not exist.", directoryOrFilePath));
        }

        // Check if the path is a file and if it is not an ODT file
        if (directoryOrFile.isFile() && !isOdtFile(directoryOrFile)) {
            throw new InvalidFileException(
                    String.format("The specified file %s is not ODT file.", directoryOrFilePath));
        }

        // Get the list of ODT file paths in the specified directory or file
        List<Path> odtFilesPaths = listOdtFiles(directoryOrFilePath);

        // Check if the list of ODT files is empty
        if (odtFilesPaths.isEmpty()) {
            throw new FileNotFoundException(
                    String.format("The specified path %s does not contains ODT files.", directoryOrFilePath));
        }

        // Iterate through the list of ODT file paths
        for (Path odtFilePath : odtFilesPaths) {
            try {
                // Create an OdtFile object for the current file path
                OdtFile odtFile = new OdtFile(odtFilePath.toString());
                // Replace the import blocks in the OdtFile
                odtFile.replaceImportBlocks(blockToReplace, newBlock);
            } catch (ProviderNotFoundException e) {
                // Log an error message if the provider is not found for the file
                LOGGER.log(Level.SEVERE,
                        String.format(
                                "Provider not found for file: %s. It might not be a valid ODT file. Error message: %s",
                                odtFilePath.toString(), e.getMessage()));
            } catch (AccessDeniedException | SecurityException e) {
                // Log an error message if access is denied for the file
                LOGGER.log(Level.SEVERE,
                        String.format(
                                "Access denied for file: %s. Error message: %s",
                                odtFilePath.toString(), e.getMessage()));
            } catch (IOException e) {
                // Log an error message if an I/O exception occurs for the file
                LOGGER.log(Level.SEVERE,
                        String.format(
                                "IOException for file: %s. Error message: %s",
                                odtFilePath.toString(), e.getMessage()));
            } catch (Exception e) {
                // Log a general error message if an exception occurs while processing the file
                LOGGER.log(Level.SEVERE,
                        String.format(
                                "Error processing file: %s. Error message: %s",
                                odtFilePath.toString(), e.getMessage()));
            }
        }
    }

}
