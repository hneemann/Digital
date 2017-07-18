package de.neemann.digital.draw.library;

import de.neemann.digital.core.arithmetic.*;
import de.neemann.digital.core.arithmetic.Comparator;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.flipflops.*;
import de.neemann.digital.core.io.*;
import de.neemann.digital.core.memory.*;
import de.neemann.digital.core.pld.DiodeBackward;
import de.neemann.digital.core.pld.DiodeForward;
import de.neemann.digital.core.pld.PullDown;
import de.neemann.digital.core.pld.PullUp;
import de.neemann.digital.core.switching.*;
import de.neemann.digital.core.wiring.*;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.Tunnel;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Settings;
import de.neemann.digital.gui.components.data.DummyElement;
import de.neemann.digital.gui.components.graphics.GraphicCard;
import de.neemann.digital.gui.components.graphics.LedMatrix;
import de.neemann.digital.gui.components.terminal.Keyboard;
import de.neemann.digital.gui.components.terminal.Terminal;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The ElementLibrary is responsible for storing all the components which can be used in a circuit.
 * Also the import of nested circuits is handled in this class.
 * This import works in two steps: At first all the files in the same directory as the root circuit are loaded.
 * The file names are shown in the components menu. From there you can pick a file to insert it to the circuit.
 * When a file is selected it is loaded to the library. After that also an icon is available.
 * This is done because the loading of a circuit creation of an icon is very time consuming and should be avoided if
 * not necessary. Its a kind of lazy loading.
 *
 * @author hneemann
 */
public class ElementLibrary implements Iterable<ElementLibrary.ElementContainer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElementLibrary.class);

    /**
     * @return the additional library path
     */
    public static File getLibPath() {
        String path = ElementLibrary.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace('\\', '/');
        if (path.endsWith("/target/classes/"))
            return new File(path.substring(0, path.length() - 16) + "/src/main/dig/lib");
        if (path.endsWith("/target/Digital.jar"))
            return new File(path.substring(0, path.length() - 19) + "/src/main/dig/lib");
        if (path.endsWith("Digital.jar"))
            return new File(path.substring(0, path.length() - 12) + "/examples/lib");

        return new File("noLibFound");
    }

    private final HashMap<String, LibraryNode> map = new HashMap<>();
    private final HashSet<String> isProgrammable = new HashSet<>();
    private final ArrayList<LibraryListener> listeners = new ArrayList<>();
    private final LibraryNode root;
    private ShapeFactory shapeFactory;
    private ElementLibraryFolder custom;
    private File rootLibraryPath;

    /**
     * Creates a new instance.
     */
    public ElementLibrary() {
        root = new LibraryNode(Lang.get("menu_elements"))
                .setLibrary(this)
                .add(new LibraryNode(Lang.get("lib_Logic"))
                        .add(And.DESCRIPTION)
                        .add(NAnd.DESCRIPTION)
                        .add(Or.DESCRIPTION)
                        .add(NOr.DESCRIPTION)
                        .add(XOr.DESCRIPTION)
                        .add(XNOr.DESCRIPTION)
                        .add(Not.DESCRIPTION)
                        .add(LookUpTable.DESCRIPTION)
                        .add(Delay.DESCRIPTION))
                .add(new LibraryNode(Lang.get("lib_io"))
                        .add(Out.DESCRIPTION)
                        .add(Out.LEDDESCRIPTION)
                        .add(In.DESCRIPTION)
                        .add(Clock.DESCRIPTION)
                        .add(Button.DESCRIPTION)
                        .add(DummyElement.TEXTDESCRIPTION)
                        .add(Probe.DESCRIPTION)
                        .add(new LibraryNode(Lang.get("lib_more"))
                                .add(LightBulb.DESCRIPTION)
                                .add(Out.SEVENDESCRIPTION)
                                .add(Out.SEVENHEXDESCRIPTION)
                                .add(LedMatrix.DESCRIPTION)
                                .add(DummyElement.DATADESCRIPTION)
                                .add(RotEncoder.DESCRIPTION)
                                .add(Keyboard.DESCRIPTION)
                                .add(Terminal.DESCRIPTION)))
                .add(new LibraryNode(Lang.get("lib_wires"))
                        .add(Ground.DESCRIPTION)
                        .add(VDD.DESCRIPTION)
                        .add(Const.DESCRIPTION)
                        .add(Tunnel.DESCRIPTION)
                        .add(Splitter.DESCRIPTION)
                        .add(PullUp.DESCRIPTION)
                        .add(PullDown.DESCRIPTION)
                        .add(Driver.DESCRIPTION)
                        .add(DriverInvSel.DESCRIPTION))
                .add(new LibraryNode(Lang.get("lib_mux"))
                        .add(Multiplexer.DESCRIPTION)
                        .add(Demultiplexer.DESCRIPTION)
                        .add(Decoder.DESCRIPTION))
                .add(new LibraryNode(Lang.get("lib_flipFlops"))
                        .add(FlipflopRS.DESCRIPTION)
                        .add(FlipflopJK.DESCRIPTION)
                        .add(FlipflopD.DESCRIPTION)
                        .add(FlipflopT.DESCRIPTION)
                        .add(FlipflopJKAsync.DESCRIPTION)
                        .add(FlipflopDAsync.DESCRIPTION))
                .add(new LibraryNode(Lang.get("lib_memory"))
                        .add(Register.DESCRIPTION)
                        .add(ROM.DESCRIPTION)
                        .add(RAMDualPort.DESCRIPTION)
                        .add(RAMSinglePort.DESCRIPTION)
                        .add(RAMSinglePortSel.DESCRIPTION)
                        .add(GraphicCard.DESCRIPTION)
                        .add(Counter.DESCRIPTION))
                .add(new LibraryNode(Lang.get("lib_arithmetic"))
                        .add(Add.DESCRIPTION)
                        .add(Sub.DESCRIPTION)
                        .add(Mul.DESCRIPTION)
                        .add(BarrelShifter.DESCRIPTION)
                        .add(Comparator.DESCRIPTION)
                        .add(Neg.DESCRIPTION)
                        .add(BitCount.DESCRIPTION))
                .add(new LibraryNode(Lang.get("lib_switching"))
                        //.add(Diode.DESCRIPTION) // see class DiodeTest for further information
                        .add(DiodeForward.DESCRIPTION)
                        .add(DiodeBackward.DESCRIPTION)
                        .add(Switch.DESCRIPTION)
                        .add(Fuse.DESCRIPTION)
                        .add(Relay.DESCRIPTION)
                        .add(PFET.DESCRIPTION)
                        .add(NFET.DESCRIPTION)
                        .add(FGPFET.DESCRIPTION)
                        .add(FGNFET.DESCRIPTION)
                        .add(TransGate.DESCRIPTION))
                .add(new LibraryNode(Lang.get("lib_misc"))
                        .add(TestCaseElement.TESTCASEDESCRIPTION)
                        .add(PowerSupply.DESCRIPTION)
                        .add(Reset.DESCRIPTION)
                        .add(Break.DESCRIPTION));

        populateNodeMap();

        custom = new ElementLibraryFolder(root, Lang.get("menu_custom"));

        File libPath = Settings.getInstance().get(Keys.SETTINGS_LIBRARY_PATH);
        if (libPath != null && libPath.exists())
            new ElementLibraryFolder(root, Lang.get("menu_library")).scanFolder(libPath);

        isProgrammable.clear();
        root.traverse(libraryNode -> {
            ElementTypeDescription d = libraryNode.getDescriptionOrNull();
            if (d != null && d.hasAttribute(Keys.BLOWN))
                isProgrammable.add(d.getName());
        });
    }

    /**
     * Returns true if element is programmable
     *
     * @param name the name
     * @return true if it is programmable
     */
    public boolean isProgrammable(String name) {
        return isProgrammable.contains(name);
    }

    /**
     * Sets the shape factory used to import sub circuits
     *
     * @param shapeFactory the shape factory
     */
    public void setShapeFactory(ShapeFactory shapeFactory) {
        this.shapeFactory = shapeFactory;
    }

    /**
     * @return the node with the custom elements
     */
    public LibraryNode getCustomNode() {
        return custom.getNode();
    }

    private void populateNodeMap() {
        map.clear();
        root.traverse(new PopulateMapVisitor(map));
    }

    /**
     * sets the root library path
     *
     * @param rootLibraryPath the path
     * @throws IOException IOException
     */
    public void setRootFilePath(File rootLibraryPath) throws IOException {
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
     * @return the actual root file path
     */
    public File getRootFilePath() {
        return rootLibraryPath;
    }

    /**
     * Checks if the given file is accessible from the actual library.
     *
     * @param file the file to check
     * @return true if given file is importable
     */
    public boolean isFileAccessible(File file) {
        if (rootLibraryPath == null) return true;

        try {
            String root = rootLibraryPath.getCanonicalPath();
            String path = file.getParentFile().getCanonicalPath();
            return path.startsWith(root);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns the node or null if node not present.
     *
     * @param elementName the name
     * @return the node or null
     */
    public LibraryNode getElementNodeOrNull(String elementName) {
        return map.get(elementName);
    }

    /**
     * Returns a {@link ElementTypeDescription} by a given name.
     * If not found its tried to load it.
     *
     * @param elementName the elements name
     * @return the {@link ElementTypeDescription}
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public ElementTypeDescription getElementType(String elementName) throws ElementNotFoundException {
        try {
            LibraryNode node = map.get(elementName);
            if (node != null)
                return node.getDescription();

            // effects only some old files!
            elementName = elementName.replace("\\", "/");
            if (elementName.contains("/")) {
                elementName = new File(elementName).getName();
            }

            node = map.get(elementName);
            if (node != null)
                return node.getDescription();

            if (rootLibraryPath == null)
                throw new ElementNotFoundException(Lang.get("err_fileNeedsToBeSaved"));

            rescanFolder();

            node = map.get(elementName);
            if (node != null)
                return node.getDescription();
        } catch (IOException e) {
            throw new ElementNotFoundException(Lang.get("msg_errorImportingModel_N0", elementName), e);
        }

        throw new ElementNotFoundException(Lang.get("err_element_N_notFound", elementName));
    }

    private void rescanFolder() throws IOException {
        LOGGER.debug("rescan folder");
        LibraryNode cn = custom.scanFolder(rootLibraryPath);

        populateNodeMap();

        if (cn != null)
            fireLibraryChanged(cn);
    }

    /**
     * Fires a library event
     *
     * @param node the node changed
     */
    void fireLibraryChanged(LibraryNode node) {
        for (LibraryListener l : listeners)
            l.libraryChanged(node);
    }

    /**
     * Adds a listener to this library
     *
     * @param listener the listener to add
     */
    public void addListener(LibraryListener listener) {
        listeners.add(listener);
        LOGGER.debug("added library listener " + listener.getClass().getSimpleName() + ", listeners: " + listeners.size());
    }

    /**
     * Removes a listener from this library
     *
     * @param listener the listener to remove
     */
    public void removeListener(LibraryListener listener) {
        listeners.remove(listener);
        LOGGER.debug("removed library listener " + listener.getClass().getSimpleName() + ", listeners: " + listeners.size());
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
     * Removes an element from the library to enforce a reload
     *
     * @param name the elements name
     * @throws IOException IOException
     */
    public void invalidateElement(File name) throws IOException {
        LibraryNode n = map.get(name.getName());
        if (n != null)
            n.invalidate();
        else {
            if (rootLibraryPath != null && isFileAccessible(name))
                rescanFolder();
        }
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

    /**
     * Imports the given file
     *
     * @param file the file to load
     * @return the description
     * @throws IOException IOException
     */
    ElementTypeDescription importElement(File file) throws IOException {
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
            throw new IOException(Lang.get("msg_errorImportingModel_N0", file), e);
        }
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
         * The returned file is opened if the user wants to modify the element
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

    private static final class PopulateMapVisitor implements Visitor {
        private final HashMap<String, LibraryNode> map;

        private PopulateMapVisitor(HashMap<String, LibraryNode> map) {
            this.map = map;
        }

        @Override
        public void visit(LibraryNode libraryNode) {
            if (libraryNode.isLeaf()) {
                final String name = libraryNode.getName();

                LibraryNode presentNode = map.get(name);
                if (presentNode == null) {
                    map.put(name, libraryNode);
                    libraryNode.setUnique(true);
                } else {
                    if (presentNode.getFile().equals(libraryNode.getFile()))
                        libraryNode.setUnique(true);
                    else {
                        presentNode.setUnique(false); // ToDo does not work if there are more than two duplicates and
                        libraryNode.setUnique(false); // some of the duplicates point to the same file
                    }
                }
            }
        }
    }
}
