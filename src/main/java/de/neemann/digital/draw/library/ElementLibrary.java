package de.neemann.digital.draw.library;

import de.neemann.digital.core.arithmetic.*;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.flipflops.FlipflopD;
import de.neemann.digital.core.flipflops.FlipflopJK;
import de.neemann.digital.core.flipflops.FlipflopRS;
import de.neemann.digital.core.flipflops.FlipflopT;
import de.neemann.digital.core.io.*;
import de.neemann.digital.core.memory.*;
import de.neemann.digital.core.pld.DiodeBackward;
import de.neemann.digital.core.pld.DiodeForeward;
import de.neemann.digital.core.pld.PullDown;
import de.neemann.digital.core.pld.PullUp;
import de.neemann.digital.core.switching.NFET;
import de.neemann.digital.core.switching.PFET;
import de.neemann.digital.core.switching.Relay;
import de.neemann.digital.core.switching.Switch;
import de.neemann.digital.core.wiring.*;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.Tunnel;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.data.DummyElement;
import de.neemann.digital.gui.components.graphics.GraphicCard;
import de.neemann.digital.gui.components.terminal.Keyboard;
import de.neemann.digital.gui.components.terminal.Terminal;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author hneemann
 */
public class ElementLibrary implements Iterable<ElementLibrary.ElementContainer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElementLibrary.class);

    private final HashMap<String, LibraryNode> map = new HashMap<>();
    private final ArrayList<LibraryListener> listeners = new ArrayList<>();
    private final LibraryNode root;
    private ShapeFactory shapeFactory;
    private LibraryNode customNode;
    private File rootLibraryPath;

    /**
     * Creates a new instance.
     */
    public ElementLibrary() {
        root = new LibraryNode("root");

        LibraryNode node = new LibraryNode(Lang.get("lib_Logic"));
        node.add(And.DESCRIPTION);
        node.add(NAnd.DESCRIPTION);
        node.add(Or.DESCRIPTION);
        node.add(NOr.DESCRIPTION);
        node.add(XOr.DESCRIPTION);
        node.add(XNOr.DESCRIPTION);
        node.add(Not.DESCRIPTION);
        node.add(LookUpTable.DESCRIPTION);
        node.add(Delay.DESCRIPTION);
        root.add(node);

        node = new LibraryNode(Lang.get("lib_io"));
        node.add(Out.DESCRIPTION);
        node.add(Out.LEDDESCRIPTION);
        node.add(In.DESCRIPTION);
        node.add(Clock.DESCRIPTION);
        node.add(Button.DESCRIPTION);
        node.add(Probe.DESCRIPTION);
        node.add(Out.SEVENDESCRIPTION);
        node.add(Out.SEVENHEXDESCRIPTION);
        node.add(DummyElement.DATADESCRIPTION);
        node.add(DummyElement.TEXTDESCRIPTION);
        node.add(Keyboard.DESCRIPTION);
        node.add(Terminal.DESCRIPTION);
        root.add(node);

        node = new LibraryNode(Lang.get("lib_wires"));
        node.add(Const.DESCRIPTION);
        node.add(Ground.DESCRIPTION);
        node.add(VDD.DESCRIPTION);
        node.add(Tunnel.DESCRIPTION);
        node.add(Splitter.DESCRIPTION);
        node.add(PullUp.DESCRIPTION);
        node.add(PullDown.DESCRIPTION);
        node.add(Driver.DESCRIPTION);
        node.add(DriverInvSel.DESCRIPTION);
        root.add(node);

        node = new LibraryNode(Lang.get("lib_mux"));
        node.add(Multiplexer.DESCRIPTION);
        node.add(Demultiplexer.DESCRIPTION);
        node.add(Decoder.DESCRIPTION);
        root.add(node);

        node = new LibraryNode(Lang.get("lib_flipFlops"));
        node.add(FlipflopRS.DESCRIPTION);
        node.add(FlipflopJK.DESCRIPTION);
        node.add(FlipflopD.DESCRIPTION);
        node.add(FlipflopT.DESCRIPTION);
        root.add(node);

        node = new LibraryNode(Lang.get("lib_memory"));
        node.add(Register.DESCRIPTION);
        node.add(ROM.DESCRIPTION);
        node.add(RAMDualPort.DESCRIPTION);
        node.add(RAMSinglePort.DESCRIPTION);
        node.add(RAMSinglePortSel.DESCRIPTION);
        node.add(GraphicCard.DESCRIPTION);
        node.add(Counter.DESCRIPTION);
        root.add(node);

        node = new LibraryNode(Lang.get("lib_arithmetic"));
        node.add(Add.DESCRIPTION);
        node.add(Sub.DESCRIPTION);
        node.add(Mul.DESCRIPTION);
        node.add(Comparator.DESCRIPTION);
        node.add(Neg.DESCRIPTION);
        node.add(BitCount.DESCRIPTION);
        root.add(node);

        node = new LibraryNode(Lang.get("lib_cplx"));
        //  add(Diode.DESCRIPTION, menu); // see class DiodeTest for further information
        node.add(DiodeForeward.DESCRIPTION);
        node.add(DiodeBackward.DESCRIPTION);
        node.add(Switch.DESCRIPTION);
        node.add(Relay.DESCRIPTION);
        node.add(NFET.DESCRIPTION);
        node.add(PFET.DESCRIPTION);
        node.add(Reset.DESCRIPTION);
        node.add(Break.DESCRIPTION);
        root.add(node);

        node = new LibraryNode(Lang.get("lib_test"));
        node.add(TestCaseElement.TESTCASEDESCRIPTION);
        root.add(node);

        try {
            populateNodeMap();
        } catch (IOException e) {
            // can not happen because there are no custom elements yet
        }
    }

    /**
     * Sets the shape factory used to import sub circuits
     *
     * @param shapeFactory the shape factory
     */
    public void setShapeFactory(ShapeFactory shapeFactory) {
        this.shapeFactory = shapeFactory;
    }

    private void populateNodeMap() throws IOException {
        map.clear();
        String dn = root.traverse(new PopulateModelVisitor(map)).getDoubleNode();
        if (dn != null)
            throw new IOException(Lang.get("err_file_N0_ExistsTwiceBelow_N1", dn, rootLibraryPath));
    }

    /**
     * sets the root library path
     *
     * @param rootLibraryPath the path
     * @throws IOException IOException
     */
    public void setFilePath(File rootLibraryPath) throws IOException {
        if (rootLibraryPath == null) {
            if (this.rootLibraryPath != null) {
                this.rootLibraryPath = null;
                rescanFolder();
            }
        } else if (!rootLibraryPath.equals(this.rootLibraryPath)) {
            this.rootLibraryPath = rootLibraryPath;
            rescanFolder();
        }
    }

    /**
     * Returns a {@link ElementTypeDescription} by a given name.
     * If not found its tried to load it.
     *
     * @param elementName the elements name
     * @return the {@link ElementTypeDescription} ore null if not found
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public ElementTypeDescription getElementType(String elementName) throws ElementNotFoundException {
        try {
            LibraryNode description = map.get(elementName);
            if (description != null)
                return description.getDescription();

            // effects only some old files!
            elementName = elementName.replace("\\", "/");
            if (elementName.contains("/")) {
                elementName = new File(elementName).getName();
            }

            description = map.get(elementName);
            if (description != null)
                return description.getDescription();

            if (rootLibraryPath == null)
                throw new RuntimeException("no root path set");

            rescanFolder();

            description = map.get(elementName);
            if (description != null)
                return description.getDescription();
        } catch (IOException e) {
            throw new ElementNotFoundException(Lang.get("msg_errorImportingModel"), e);
        }

        throw new ElementNotFoundException(Lang.get("err_element_N_notFound", elementName));
    }

    private void rescanFolder() throws IOException {
        LOGGER.debug("rescan folder");
        if (customNode == null) {
            customNode = new LibraryNode(Lang.get("menu_custom"));
            root.add(customNode);
        } else customNode.removeAll();

        if (rootLibraryPath != null)
            scanFolder(rootLibraryPath, customNode);

        populateNodeMap();

        fireLibraryChanged();
    }

    private void fireLibraryChanged() {
        for (LibraryListener l : listeners)
            l.libraryChanged();
    }

    private void scanFolder(File path, LibraryNode node) {
        File[] list = path.listFiles();
        if (list != null) {
            ArrayList<File> orderedList = new ArrayList<>(Arrays.asList(list));
            orderedList.sort((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
            for (File f : orderedList) {
                if (f.isDirectory()) {
                    LibraryNode n = new LibraryNode(f.getName());
                    scanFolder(f, n);
                    if (!n.isEmpty())
                        node.add(n);
                }
            }
            for (File f : orderedList) {
                final String name = f.getName();
                if (f.isFile() && name.endsWith(".dig"))
                    node.add(importElement(f));
            }
        }
    }

    /**
     * Adds a listener to this library
     *
     * @param listener the listener to add
     */
    public void addListener(LibraryListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener from this library
     *
     * @param listener the listener to remove
     */
    public void removeListener(LibraryListener listener) {
        listeners.remove(listener);
    }


    @Override
    public Iterator<ElementContainer> iterator() {
        ArrayList<ElementContainer> nodes = new ArrayList<>();
        for (LibraryNode n : getRoot())
            addToList(nodes, n, "");
        return nodes.iterator();
    }

    private void addToList(ArrayList<ElementContainer> nodes, LibraryNode node, String path) {
        if (node.isLeaf()) {
            if (node.isDescriptionLoaded()) {
                try {
                    nodes.add(new ElementContainer(node.getDescription(), path));
                } catch (IOException e) {
                    // can not happen because description is present!
                }
            }
        } else
            for (LibraryNode n : node)
                addToList(nodes, n, concat(path, node.getName()));
    }

    private String concat(String path, String name) {
        if (path.length() == 0)
            return name;
        return path + " - " + name;

    }

    /**
     * Removes an element from the library
     *
     * @param name the elements name
     */
    public void removeElement(File name) {
        map.remove(name.getName());
    }

    /**
     * Updates all entries
     *
     * @throws IOException IOException
     */
    public void updateEntries() throws IOException {
        rescanFolder();
    }

    /**
     * @return the root element
     */
    public LibraryNode getRoot() {
        return root;
    }

    private LibraryNode importElement(File file) {
        return new LibraryNode(file.getName(), () -> {
            try {
                LOGGER.debug("load element " + file);
                Circuit circuit;
                try {
                    circuit = Circuit.loadCircuit(file, shapeFactory);
                } catch (IOException e) {
                    throw new IOException(Lang.get("err_couldNotFindIncludedFile_N0", file));
                }
                ElementTypeDescriptionCustom description =
                        new ElementTypeDescriptionCustom(file,
                                attributes -> new CustomElement(circuit, ElementLibrary.this, file),
                                circuit.getAttributes(), circuit.getInputNames());
                description.setShortName(createShortName(file));

                String descriptionText = circuit.getAttributes().get(Keys.DESCRIPTION);
                if (descriptionText != null && descriptionText.length() > 0) {
                    description.setDescription(descriptionText);
                }
                return description;
            } catch (PinException e) {
                throw new IOException(Lang.get("msg_errorImportingModel"), e);
            }
        });
    }

    private String createShortName(File file) {
        return createShortName(file.getName());
    }

    private String createShortName(String name) {
        if (name.endsWith(".dig")) return name.substring(0, name.length() - 4);

        String transName = Lang.getNull("elem_" + name);
        if (transName == null)
            return name;
        else
            return transName;
    }

    /**
     * The description of a nested element.
     * This is a complete circuit which is used as a element.
     */
    public static class ElementTypeDescriptionCustom extends ElementTypeDescription {
        private final File file;
        private final ElementAttributes attributes;
        private String description;

        /**
         * Creates a new element
         *
         * @param file           the file which is loaded
         * @param elementFactory a element factory which is used to create concrete elements if needed
         * @param attributes     the attributes of the element
         * @param inputNames     the names of the input signals
         */
        public ElementTypeDescriptionCustom(File file, ElementFactory elementFactory, ElementAttributes attributes, PinDescription... inputNames) {
            super(file.getName(), elementFactory, inputNames);
            this.file = file;
            this.attributes = attributes;
            setShortName(file.getName());
            addAttribute(Keys.ROTATE);
            addAttribute(Keys.LABEL);
        }

        /**
         * Returns the filename
         * the retuned file is opened if the user wants to modify the element
         *
         * @return the filename
         */
        public File getFile() {
            return file;
        }

        /**
         * @return the elements attributes
         */
        public ElementAttributes getAttributes() {
            return attributes;
        }

        /**
         * Sets a custom description for this field
         *
         * @param description the description
         */
        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String getDescription(ElementAttributes elementAttributes) {
            if (description != null)
                return description;
            else
                return super.getDescription(elementAttributes);
        }
    }


    /**
     * Used to store a elements name and its position in the elements menu.
     */
    public static class ElementContainer {
        private final ElementTypeDescription name;
        private final String treePath;

        /**
         * Creates anew instance
         *
         * @param typeDescription the elements typeDescription
         * @param treePath        the elements menu path
         */
        ElementContainer(ElementTypeDescription typeDescription, String treePath) {
            this.name = typeDescription;
            this.treePath = treePath;
        }

        /**
         * @return the elements name
         */
        public ElementTypeDescription getDescription() {
            return name;
        }

        /**
         * @return Returns the path in the menu
         */
        public String getTreePath() {
            return treePath;
        }
    }

    private static final class PopulateModelVisitor implements Visitor {
        private final HashMap<String, LibraryNode> map;
        private String doubleNode;

        private PopulateModelVisitor(HashMap<String, LibraryNode> map) {
            this.map = map;
        }

        @Override
        public void visit(LibraryNode libraryNode) {
            if (libraryNode.isLeaf()) {
                final String name = libraryNode.getName();

                if (map.containsKey(name))
                    doubleNode = name;

                map.put(name, libraryNode);
            }
        }

        private String getDoubleNode() {
            return doubleNode;
        }
    }
}
