package com.example;

import java.io.FileNotFoundException;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.example.exception.AbortException;
import com.example.exception.InvalidActionException;
import com.example.exception.InvalidArgumentException;
import com.example.util.FileUtil;
import com.example.validation.ArgumentValidator;

public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        try {
            LOGGER.log(Level.INFO, "Starting application with arguments: {0}", String.join(", ", args));
            ArgumentValidator.validateArguments(args);

            String action = args[0];

            switch (action) {
                case ArgumentValidator.JSON_ACTION:
                    FileUtil.checkAndPromptOverwrite(args[2]);
                    LOGGER.info("Executing JSON action");
                    FileUtil.createJsonFile(args[1], args[2]);
                    break;
                case ArgumentValidator.REPLACE_ACTION:
                    LOGGER.info("Executing replace action");
                    FileUtil.replaceBlocks(args[1], args[2], args[3]);
                    break;
            }
        } catch (InvalidActionException | InvalidArgumentException e) {
            LOGGER.log(Level.SEVERE, "Validation error: {0}", e.getMessage());
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "File not found error: {0}", e.getMessage());
        } catch (AbortException e) {
            LOGGER.log(Level.INFO, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred: {0}", e.getMessage());
        } finally {
            LOGGER.log(Level.INFO, "Application finished execution");
        }
    }

}
