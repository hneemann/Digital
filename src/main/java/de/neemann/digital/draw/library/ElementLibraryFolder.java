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
            int num = scanFolder(path, node);
            LOGGER.debug("found " + num + " files in " + path);
        } else if (node != null) {
            root.remove(node);
            node = null;
            changedNode = root;
        }
        return changedNode;
    }

    private static int scanFolder(File path, LibraryNode node) {
        int num = 0;
        File[] list = path.listFiles();
        if (list != null) {
            ArrayList<File> orderedList = new ArrayList<>(Arrays.asList(list));
            orderedList.sort((f1, f2) -> NumStringComparator.compareStr(f1.getName(), f2.getName()));
            for (File f : orderedList) {
                if (f.isDirectory()) {
                    LibraryNode n = new LibraryNode(f.getName());
                    num += scanFolder(f, n);
                    if (!n.isEmpty())
                        node.add(n);
                }
            }
            for (File f : orderedList) {
                final String name = f.getName();
                if (f.isFile() && name.endsWith(".dig")) {
                    node.add(new LibraryNode(f));
                    num++;
                }
            }
        }
        return num;
    }

}
