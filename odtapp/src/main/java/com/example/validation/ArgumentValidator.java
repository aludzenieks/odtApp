package com.example.validation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import com.example.exception.InvalidActionException;
import com.example.exception.InvalidArgumentException;
import com.example.util.FileUtil;

public class ArgumentValidator {

    public static final String JSON_ACTION = "json";
    public static final String REPLACE_ACTION = "replace";
    private static final Pattern IMPORT_BLOCK_PATTERN = Pattern.compile("\\[import\\s[^\\s]+?\\.odt\\]");

    public static void validateArguments(String[] args) throws InvalidActionException, InvalidArgumentException {
        if (args.length < 3 || args.length > 4) {
            throw new InvalidArgumentException(
                    "Usage: java App <action> <directory/file path> <additional arguments>");
        }

        String action = args[0];

        switch (action) {
            case JSON_ACTION:
                if (args.length != 3) {
                    throw new InvalidArgumentException(
                            "Usage for json action: java App json <input directory/file path> <output file path>");
                }
                if (!FileUtil.isValidPath(args[1])) {
                    throw new InvalidArgumentException("Invalid path provided: " + args[1]);
                }
                Path path = Paths.get(args[2]);
                Path parentPath = path.getParent();
                if (!FileUtil.isValidPath(parentPath)) {
                    throw new InvalidArgumentException("The directory does not exist: " + parentPath);
                }
                break;
            case REPLACE_ACTION:
                if (args.length != 4) {
                    throw new InvalidArgumentException(
                            "Usage for replace action: java App replace <directory/file path> <block to replace> <new block>");
                }
                if (!FileUtil.isValidPath(args[1])) {
                    throw new InvalidArgumentException("Invalid path provided: " + args[1]);
                }
                if (!isValidImportBlock(args[2])) {
                    throw new InvalidArgumentException(String.format(
                            "Invalid format of 'block to replace'. Must match pattern: %s", IMPORT_BLOCK_PATTERN));
                }
                if (!isValidImportBlock(args[3])) {
                    throw new InvalidArgumentException(String.format(
                            "Invalid format of 'new block'. Must match pattern: %s", IMPORT_BLOCK_PATTERN));
                }
                break;
            default:
                throw new InvalidActionException("Unknown action: " + action);
        }
    }

    public static boolean isValidImportBlock(String block) {
        return IMPORT_BLOCK_PATTERN.matcher(block).matches();
    }

}
