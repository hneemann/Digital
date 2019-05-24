/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.toolchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Helper to avoid loading a config several times.
 */
class ConfigCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCache.class);
    private final File initialFile;
    private final HashMap<String, Configuration> cache;

    /**
     * Creates a new instance
     *
     * @param initialFile the intial config file
     */
    ConfigCache(File initialFile) {
        this.initialFile = initialFile;
        cache = new HashMap<>();
    }

    /**
     * Loads the give config.
     *
     * @param filename the configs file name
     * @return the config
     * @throws IOException IOException
     */
    Configuration getConfig(String filename) throws IOException {
        if (initialFile == null)
            throw new IOException("No initial config file given!");


        Configuration c = cache.get(filename);
        if (c == null) {
            final File file = new File(initialFile.getParentFile(), filename);
            LOGGER.info("load config " + file);
            c = Configuration.load(file);
            cache.put(filename, c);
        }
        return c;
    }
}
