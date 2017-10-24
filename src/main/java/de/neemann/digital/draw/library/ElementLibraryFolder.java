package de.neemann.digital.draw.library;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Handles a single folder
 * Created by hneemann on 17.07.17.
 */
public class ElementLibraryFolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElementLibraryFolder.class);
    private static final int MAX_FILES_TO_SCAN = 5000;

    private final LibraryNode root;
    private final String menuTitle;
    private LibraryNode node;
    private File lastPath;

    /**
     * create a new folder manager
     *
     * @param root      the root node
     * @param menuTitle string to show in menu
     */
    public ElementLibraryFolder(LibraryNode root, String menuTitle) {
        this.root = root;
        this.menuTitle = menuTitle;
    }

    /**
     * @return the managed node
     */
    public LibraryNode getNode() {
        return node;
    }

    /**
     * scans the given folder
     *
     * @param path the path to scan
     * @return the node which has changed
     */
    public LibraryNode scanFolder(File path) {
        LibraryNode changedNode = null;
        if (path != null) {
            if (node == null) {
                node = new LibraryNode(menuTitle);
                root.add(node);
                changedNode = root;
            } else {
                node.removeAll();
                changedNode = node;
            }
            final ScanCounter scanCounter = new ScanCounter();
            scanFolder(path, node, scanCounter);
            LOGGER.debug("found " + scanCounter.getCircuitCounter() + " files in " + path);
        } else if (node != null) {
            root.remove(node);
            node = null;
            changedNode = root;
        }
        return changedNode;
    }

    private static void scanFolder(File path, LibraryNode node, ScanCounter scanCounter) {
        File[] list = path.listFiles();
        if (list != null && scanCounter.getFileCounter() < MAX_FILES_TO_SCAN) {
            ArrayList<File> orderedList = new ArrayList<>(Arrays.asList(list));
            orderedList.sort((f1, f2) -> NumStringComparator.compareStr(f1.getName(), f2.getName()));
            for (File f : orderedList) {
                if (f.isDirectory() && !f.isHidden()) {
                    LibraryNode n = new LibraryNode(f.getName());
                    scanFolder(f, n, scanCounter);
                    if (!n.isEmpty())
                        node.add(n);
                }
            }
            for (File f : orderedList) {
                scanCounter.incFile();
                final String name = f.getName();
                if (f.isFile() && name.endsWith(".dig")) {
                    node.add(new LibraryNode(f));
                    scanCounter.incCircuit();
                }
            }
        }
    }

    private static final class ScanCounter {
        private int fileCounter;
        private int circuitCounter;

        private void incFile() {
            fileCounter++;
        }

        private int getFileCounter() {
            return fileCounter;
        }

        private void incCircuit() {
            circuitCounter++;
        }

        private int getCircuitCounter() {
            return circuitCounter;
        }
    }
}
