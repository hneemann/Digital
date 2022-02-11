/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.gui.InfoDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

/**
 * Handler for exceptions which are occurred and not shown to the user.
 */
public class DigitalUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DigitalUncaughtExceptionHandler.class);
    private final DateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        throwable.printStackTrace();
        File home = new File(System.getProperty("user.home"));
        File log = new File(home, "Digital_" + formatDate.format(new Date()) + ".log");
        LOGGER.info("uncaught exception log written to " + log);
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(log), StandardCharsets.UTF_8))) {
            writeLog(w, thread, throwable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLog(BufferedWriter w, Thread thread, Throwable throwable) throws IOException {
        w.write("This file was created because there was an unexpected exception in Digital.");
        w.newLine();
        w.write("Please send this file to digital-simulator@web.de or create an issue at");
        w.newLine();
        w.write("GitHub: https://github.com/hneemann/Digital/issues");
        w.newLine();
        w.newLine();
        w.write("Manifest: ");
        w.newLine();

        for (InfoDialog.Manifest m : InfoDialog.getInstance()) {
            writeManifest(w, m);
        }

        w.newLine();
        w.write("System properties:");
        w.newLine();
        writeProperties(w, "java.");
        w.newLine();
        writeProperties(w, "os.");
        w.newLine();
        w.write("thread: ");
        w.write(thread.getName());
        w.newLine();
        w.newLine();
        w.write("exception: ");
        w.newLine();

        throwable.printStackTrace(new PrintWriter(w));
    }

    private void writeManifest(BufferedWriter w, InfoDialog.Manifest m) throws IOException {
        String path = m.getUrl().getPath();
        int p = path.lastIndexOf("!/");
        if (p >= 0) {
            path = path.substring(0, p);
            p = path.lastIndexOf('/');
            if (p >= 0) {
                w.write(path.substring(p + 1));
                w.write(":");
                w.newLine();
            }
        }
        for (Map.Entry<String, String> a : m.getEntries().entrySet()) {
            w.write(a.getKey());
            w.write("=");
            w.write(a.getValue());
            w.newLine();
        }
    }

    private void writeProperties(BufferedWriter w, String suffix) throws IOException {
        Enumeration<?> names = System.getProperties().propertyNames();
        while (names.hasMoreElements()) {
            String n = names.nextElement().toString();
            if (n.startsWith(suffix)) {
                w.write(n);
                w.write("=");
                w.write(System.getProperty(n));
                w.newLine();
            }
        }
    }

}
