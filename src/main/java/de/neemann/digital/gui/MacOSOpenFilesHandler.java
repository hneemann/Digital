/*
 * Copyright (c) 2026 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.fsm.gui.FSMFrame;
import de.neemann.digital.gui.components.table.TableDialog;
import de.neemann.digital.gui.tutorial.InitialTutorial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Opens the files macOS asks the application to open, e.g. after a double click
 * on a *.dig file in the Finder. In this case macOS does not pass the file as a
 * command line argument but sends an "open documents" event to the running application.
 * <p>
 * The handler is registered by reflection because the java.awt.desktop API
 * is not available in Java 8, which is still the target platform of Digital.
 * If the API is missing or not supported, nothing happens.
 */
public final class MacOSOpenFilesHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MacOSOpenFilesHandler.class);

    // accessed only from the event dispatch thread
    private static final ArrayList<File> PENDING = new ArrayList<>();
    private static Main mainWindow;
    private static boolean mainWindowReplaceable;
    private static File startupFile;

    private MacOSOpenFilesHandler() {
    }

    /**
     * Registers the handler at the desktop.
     * Should be called as early as possible, so that the files which caused the
     * start of the application are not lost. Files received before the main window
     * is available are queued until {@link #setMainWindow(Main, boolean)} is called.
     */
    public static void register() {
        try {
            Class<?> desktopClass = Class.forName("java.awt.Desktop");
            if (!(Boolean) desktopClass.getMethod("isDesktopSupported").invoke(null))
                return;
            Object desktop = desktopClass.getMethod("getDesktop").invoke(null);

            Class<?> actionClass = Class.forName("java.awt.Desktop$Action");
            Object openFileAction = actionClass.getField("APP_OPEN_FILE").get(null);
            if (!(Boolean) desktopClass.getMethod("isSupported", actionClass).invoke(desktop, openFileAction))
                return;

            Class<?> handlerClass = Class.forName("java.awt.desktop.OpenFilesHandler");
            Method getFiles = Class.forName("java.awt.desktop.FilesEvent").getMethod("getFiles");
            Object handler = Proxy.newProxyInstance(MacOSOpenFilesHandler.class.getClassLoader(),
                    new Class<?>[]{handlerClass},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "openFiles":
                                List<?> files = (List<?>) getFiles.invoke(args[0]);
                                SwingUtilities.invokeLater(() -> received(files));
                                return null;
                            case "hashCode":
                                return System.identityHashCode(proxy);
                            case "equals":
                                return proxy == args[0];
                            case "toString":
                                return MacOSOpenFilesHandler.class.getSimpleName();
                            default:
                                return null;
                        }
                    });
            desktopClass.getMethod("setOpenFileHandler", handlerClass).invoke(desktop, handler);
            LOGGER.debug("macOS open files handler registered");
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.debug("macOS open files handler not registered: " + e);
        }
    }

    /**
     * Removes and returns the first queued circuit, if there is one.
     * Used at startup to open the circuit directly in the main window.
     * Must be called on the event dispatch thread.
     *
     * @return the first queued *.dig file or null
     */
    static File takePendingCircuit() {
        for (int i = 0; i < PENDING.size(); i++)
            if (isCircuit(PENDING.get(i)))
                return PENDING.remove(i);
        return null;
    }

    /**
     * Sets the main window and opens all queued files.
     * Must be called on the event dispatch thread after the main window was created.
     * The main window is used only after the circuit it loads at startup (e.g. the
     * most recently used one) is loaded, so this circuit can not replace a file
     * opened by the Finder.
     *
     * @param main        the main window
     * @param replaceable true if the main window shows what was loaded without being
     *                    requested, e.g. the most recently used circuit. As long as it
     *                    is unchanged, it is replaced by the first file opened by the Finder.
     *                    False if the main window shows a file the user asked to open.
     */
    static void setMainWindow(Main main, boolean replaceable) {
        SwingUtilities.invokeLater(() -> {
            mainWindow = main;
            mainWindowReplaceable = replaceable;
            startupFile = main.getBaseFileName();
            ArrayList<File> files = new ArrayList<>(PENDING);
            PENDING.clear();
            for (File f : files)
                open(f);
        });
    }

    private static void received(List<?> files) {
        for (Object o : files)
            if (o instanceof File) {
                File f = ((File) o).getAbsoluteFile();
                LOGGER.debug("macOS requests to open " + f);
                if (mainWindow == null)
                    PENDING.add(f);
                else
                    open(f);
            }
    }

    private static boolean isCircuit(File f) {
        return f.getName().toLowerCase(Locale.ROOT).endsWith(".dig");
    }

    private static void open(File f) {
        if (!f.exists())
            return;
        String name = f.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".fsm"))
            FSMFrame.openFile(f);
        else if (name.endsWith(".tru"))
            TableDialog.openFile(f);
        else if (mainWindow == null || !mainWindow.isDisplayable()) {
            mainWindow = new Main.MainBuilder().setFileToOpen(f).build();
            mainWindowReplaceable = false;
            mainWindow.setVisible(true);
        } else if (isUnchangedStartupState()) {
            // the main window shows only what was loaded at startup, so it is replaced
            mainWindowReplaceable = false;
            closeTutorial();
            mainWindow.open(f, false);
            mainWindow.toFront();
        } else
            mainWindow.open(f, true);
    }

    /**
     * The tutorial needs an empty circuit. It is shown again at the next start without a file.
     */
    private static void closeTutorial() {
        for (Window w : mainWindow.getOwnedWindows())
            if (w instanceof InitialTutorial)
                w.dispose();
    }

    private static boolean isUnchangedStartupState() {
        if (!mainWindowReplaceable || mainWindow.isStateChanged())
            return false;
        File current = mainWindow.getBaseFileName();
        return current == null || current.equals(startupFile);
    }
}
