/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital;

import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.gui.FileHistory;
import de.neemann.digital.gui.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Helper to find a file if only the filename is known.
 */
public class FileLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileLocator.class);
    private static final int MAX_FILE_COUNTER = 5000;

    private final String filename;
    private File file;
    private FileHistory history;
    private File libraryRoot;
    private File baseFile;


    /**
     * Creates a new instance
     *
     * @param file the file to search for
     */
    public FileLocator(File file) {
        this(file == null ? null : file.getName());
        this.file = file;
    }

    /**
     * Creates a new instance
     *
     * @param filename the file name
     */
    public FileLocator(String filename) {
        this.filename = filename;
    }

    /**
     * Sets the relevant file history
     *
     * @param history the file history
     * @return this for chained calls
     */
    public FileLocator setHistory(FileHistory history) {
        this.history = history;
        return this;
    }

    /**
     * Sets the used library.
     * If called the library folder is scanned to locate the file.
     *
     * @param library the library
     * @return this for chained calls
     */
    public FileLocator setLibrary(ElementLibrary library) {
        this.libraryRoot = library.getRootFilePath();
        return this;
    }

    /**
     * Sets the projects root folder
     *
     * @param libraryRoot the folder
     * @return this for chained calls
     */
    public FileLocator setLibraryRoot(File libraryRoot) {
        if (libraryRoot.isFile())
            this.libraryRoot = libraryRoot.getParentFile();
        else
            this.libraryRoot = libraryRoot;
        return this;
    }

    /**
     * The base file from which the specified file name originates.
     * Often the base file is a file with a different extension,
     * which is located in the same directory.
     *
     * @param baseFile the base file
     * @return this for chained calls
     */
    public FileLocator setBaseFile(File baseFile) {
        this.baseFile = baseFile;
        return this;
    }

    /**
     * Configures the file locator with the given main
     *
     * @param main the main class
     * @return this for chained calls
     */
    public FileLocator setupWithMain(Main main) {
        if (main != null) {
            setBaseFile(main.getBaseFileName());
            setLibrary(main.getLibrary());
        }
        return this;
    }

    /**
     * Tries to locate the given file.
     *
     * @return the file or null if not found
     */
    public File locate() {
        if (file != null && file.exists())
            return file;

        if (filename == null)
            return null;

        if (baseFile != null) {
            File f = new File(baseFile.getParentFile(), filename);
            if (f.isFile() && f.exists()) {
                LOGGER.debug(filename + " found in base file folder");
                return f;
            }
        }

        if (history != null) {
            for (File h : history.getFiles()) {
                if (h.getName().equals(filename) && h.exists()) {
                    LOGGER.debug(filename + " found in file history");
                    return h;
                }
            }
        }

        if (libraryRoot != null) {
            LOGGER.debug(filename + ": start library folder lookup");
            ArrayList<File> foundFiles = new ArrayList<>();
            try {
                search(libraryRoot, foundFiles, MAX_FILE_COUNTER);
                if (foundFiles.size() == 1) {
                    LOGGER.debug(filename + " found in library folder");
                    return foundFiles.get(0);
                }
            } catch (IOException e) {
                LOGGER.debug(filename + ": " + e.getMessage());
            }
        }
        LOGGER.debug(filename + " not found");
        return file;
    }

    private int search(File path, ArrayList<File> foundFiles, int fileCounter) throws IOException {
        File[] list = path.listFiles();
        if (list != null) {
            for (File f : list) {
                if (f.isFile() && f.getName().equals(filename)) {
                    foundFiles.add(f);
                    if (foundFiles.size() > 1)
                        throw new IOException("multiple matching files: " + foundFiles);
                }
                fileCounter--;
                if (fileCounter < 0)
                    throw new IOException("to many files");
            }
            for (File f : list)
                if (f.isDirectory() && !f.getName().startsWith("."))
                    fileCounter = search(f, foundFiles, fileCounter);
        }
        return fileCounter;
    }

}
