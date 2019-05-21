/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.toolchain;

import java.io.IOException;

/**
 * Represents a file to create
 */
public class FileToCreate {
    private String name;
    private String content;
    private boolean overwrite;
    private boolean filter;
    private String id;
    private String referenceFilename;
    private String referenceId;

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
     * Returns the content of the file.
     *
     * @return the files content
     * @throws IOException if no file content is available
     */
    public String getContent() throws IOException {
        if (content == null)
            throw new IOException("no file content given!");
        return content;
    }

    /**
     * @return true if file has a content
     */
    boolean hasContent() {
        return content != null;
    }

    /**
     * @return the id of this file
     */
    public String getId() {
        return id;
    }

    /**
     * @return the file name of the referenced file
     * @throws IOException if name not given
     */
    String getReferenceFilename() throws IOException {
        if (referenceFilename == null)
            throw new IOException("no file given to look at (" + Configuration.LOOK_AT_ALIAS + "=\"...\")");
        return referenceFilename;
    }

    /**
     * @return the id of the referenced file
     * @throws IOException if id not given
     */
    String getReferenceId() throws IOException {
        if (referenceId == null)
            throw new IOException("no reference id given (" + Configuration.REF_ALIAS + "=\"...\")");
        return referenceId;
    }
}
