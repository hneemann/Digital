/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.ide;

import java.io.IOException;

/**
 * Represents a file to create
 */
public class FileToCreate {
    private final String name;
    private final String content;
    private final boolean overwrite;
    private final boolean filter;

    /**
     * The file to create
     *
     * @param name      the name of the file
     * @param content   the files content
     * @param overwrite overwrite every time a command is executed
     * @param filter    the files content needs to be filtered
     */
    public FileToCreate(String name, String content, boolean overwrite, boolean filter) {
        this.name = name;
        this.content = content;
        this.overwrite = overwrite;
        this.filter = filter;
    }

    /**
     * @return The file name of the file. Is always filtered
     */
    public String getName() {
        return name;
    }

    /**
     * @return true if the file needs to be overwritten, every time a command is executed.
     */
    public boolean isOverwrite() {
        return overwrite;
    }

    /**
     * @return true if the files contend needs to be filtered.
     */
    public boolean isFilter() {
        return filter;
    }

    /**
     * @return the files content
     * @throws IOException if no file content is available
     */
    public String getContent() throws IOException {
        if (content == null)
            throw new IOException("no file content given!");
        return content;
    }
}
