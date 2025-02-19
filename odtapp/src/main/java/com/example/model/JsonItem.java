package com.example.model;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class JsonItem {
    @NonNull
    private String name;

    @NonNull
    private String type;

    private List<String> importBlocks;

    private LinkedList<JsonItem> children;

    private String error;

    public static String DIRECTORY_TYPE = "directory";
    public static String FILE_TYPE = "file";

    public boolean isDirectoryType() {
        return this.type.equals(DIRECTORY_TYPE);
    }

    public boolean isFileType() {
        return this.type.equals(FILE_TYPE);
    }
}
