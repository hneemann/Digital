package de.neemann.digital.gui.components;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.*;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.ToolTipAction;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Simple Dialog to show an elements help text.
 * <p/>
 * Created by hneemann on 25.10.16.
 */
public class ElementHelpDialog extends JDialog {

    private static final int MAX_WIDTH = 600;
    private static final int MAX_HEIGHT = 800;

    private JPanel buttons;

    /**
     * Creates a new instance
     *
     * @param parent            the parents dialog
     * @param elementType       the type of the element
     * @param elementAttributes the attributes of this element
     */
    public ElementHelpDialog(JDialog parent, ElementTypeDescription elementType, ElementAttributes elementAttributes) {
        super(parent, Lang.get("attr_help"), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        StringWriter w = new StringWriter();
        try {
            writeDetailedDescription(w, elementType, elementAttributes);
        } catch (IOException e) {
            // can not happen because of writing to memory
        }
        init(parent, w.toString());
    }

    /**
     * Creates a new instance
     *
     * @param parent  the parents dialog
     * @param library the elements library
     */
    public ElementHelpDialog(JFrame parent, ElementLibrary library, ShapeFactory shapeFactory) {
        super(parent, Lang.get("attr_help"), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        MyURLStreamHandlerFactory.shapeFactory = shapeFactory;
        StringWriter w = new StringWriter();
        try {
            w.write("<html><body>");
            writeFullHTMLDocumentation(w, library, description -> "image:" + description.getName() + ".png");
            w.write("</body></html>");
        } catch (IOException e) {
            // can not happen because of writing to memory
        }
        init(parent, w.toString());

        buttons.add(
                new ToolTipAction(Lang.get("btn_openInBrowser")) {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        try {
                            File tmp = Files.createTempDirectory("digital").toFile();
                            exportHTMLDocumentation(tmp, library, shapeFactory);
                            File index = new File(tmp, "index.html");
                            openWebpage(index.toURI());
                        } catch (IOException e) {
                            new ErrorMessage(Lang.get("err_openingDocumentation")).addCause(e).show(ElementHelpDialog.this);
                        }
                    }
                }.setToolTip(Lang.get("btn_openInBrowser_tt")).createJButton(), 0);
    }

    /**
     * Creates a full HTML documentation of all elements
     *
     * @param library      the library which parts are documented
     * @param imageHandler the imageHandler creates the url to get the image representing a concrete part
     * @throws IOException IOException
     */
    public static void writeFullHTMLDocumentation(Writer w, ElementLibrary library, ImageHandler imageHandler) throws IOException {
        ArrayList<String> chapter = new ArrayList<>();

        String actPath = null;
        StringWriter content = new StringWriter();
        for (ElementLibrary.ElementContainer e : library) {
            String p = e.getTreePath();
            if (!p.equals(actPath)) {
                actPath = p;
                chapter.add(actPath);
                content.append("<h2><a name=\"").append(actPath).append("\">").append(actPath).append("</a></h2>\n");
                content.append("<hr/>\n");
            }
            String url = imageHandler.getUrl(e.getDescription());
            content.append("<center><img src=\"").append(url).append("\"/></center>\n");
            writeHTMLDescription(content, e.getDescription(), new ElementAttributes());
            content.append("<hr/>\n");
        }
        content.flush();


        w.append("<h1>").append(Lang.get("digital")).append("</h1>\n");
        for (String chap : chapter) {
            w.append("<a href=\"#").append(chap).append("\">").append(chap).append("</a><br/>\n");
        }

        w.write(content.toString());
    }

    private void init(Component parent, String description) {
        JEditorPane editorPane = new JEditorPane("text/html", description);
        editorPane.setEditable(false);
        editorPane.setCaretPosition(0);

        editorPane.addHyperlinkListener(hyperlinkEvent -> {
            if (HyperlinkEvent.EventType.ACTIVATED == hyperlinkEvent.getEventType()) {
                String desc = hyperlinkEvent.getDescription();
                if (desc == null || !desc.startsWith("#")) return;
                desc = desc.substring(1);
                editorPane.scrollToReference(desc);
            }
        });

        getContentPane().add(new JScrollPane(editorPane));

        buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JButton(new AbstractAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
            }
        }));
        getContentPane().add(buttons, BorderLayout.SOUTH);

        pack();
        Dimension r = getSize();
        if (r.width > MAX_WIDTH) r.width = MAX_WIDTH;
        if (r.height > MAX_HEIGHT) r.height = MAX_HEIGHT;
        setSize(r);
        setLocationRelativeTo(parent);
    }

    /**
     * Creates a detailed human readable description of this element
     *
     * @param et                the element to describe
     * @param elementAttributes the actual attributes of the element to describe
     */
    private static void writeDetailedDescription(Writer w, ElementTypeDescription et, ElementAttributes elementAttributes) throws IOException {
        w.write("<html><body>");
        writeHTMLDescription(w, et, elementAttributes);
        w.write("</body></html>");
    }

    /**
     * Adds the description of the given element to the given StringBuilder.
     *
     * @param w                 the StringBuilder to use
     * @param et                the element to describe
     * @param elementAttributes the actual attributes of the element to describe
     * @throws IOException IOException
     */
    public static void writeHTMLDescription(Writer w, ElementTypeDescription et, ElementAttributes elementAttributes) throws IOException {
        String translatedName = et.getTranslatedName();
        if (translatedName.endsWith(".dig"))
            translatedName = new File(translatedName).getName();
        w.append("<h3>").append(escapeHTML(translatedName)).append("</h3>\n");
        String descr = et.getDescription(elementAttributes);
        if (!descr.equals(translatedName))
            w.append("<p>").append(escapeHTML(et.getDescription(elementAttributes))).append("</p>\n");

        try {
            PinDescriptions inputs = et.getInputDescription(elementAttributes);
            if (inputs != null && inputs.size() > 0) {
                w.append("<h4>").append(Lang.get("elem_Help_inputs")).append(":</h4>\n<dl>\n");
                for (PinDescription i : inputs)
                    writeEntry(w, ElementAttributes.cleanLabel(i.getName()), i.getDescription());
                w.append("</dl>\n");
            }
        } catch (NodeException e) {
            e.printStackTrace();
        }

        PinDescriptions outputs = et.getOutputDescriptions(elementAttributes);
        if (outputs != null && outputs.size() > 0) {
            w.append("<h4>").append(Lang.get("elem_Help_outputs")).append(":</h4>\n<dl>\n");
            for (PinDescription i : outputs)
                writeEntry(w, ElementAttributes.cleanLabel(i.getName()), i.getDescription());
            w.append("</dl>\n");
        }

        if (et.getAttributeList().size() > 0) {
            w.append("<h4>").append(Lang.get("elem_Help_attributes")).append(":</h4>\n<dl>\n");
            for (Key k : et.getAttributeList())
                writeEntry(w, k.getName(), k.getDescription());
            w.append("</dl>\n");
        }
    }

    private static void writeEntry(Writer w, String name, String description) throws IOException {
        w.append("<dt><i>").append(escapeHTML(name)).append("</i></dt>\n");
        if (description != null && description.length() > 0 && !name.equals(description))
            w.append("<dd>").append(escapeHTML(description)).append("</dd>\n");
    }

    /**
     * @return factory which catches 'image' protocol requests to deliver images via an URL.
     */
    public static URLStreamHandlerFactory createURLStreamHandlerFactory() {
        return new MyURLStreamHandlerFactory();

    }

    private static class MyURLStreamHandlerFactory implements URLStreamHandlerFactory {

        private static ShapeFactory shapeFactory;

        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if (protocol.equals("image"))
                return new URLStreamHandler() {
                    @Override
                    protected URLConnection openConnection(URL u) throws IOException {
                        return new ImageConnection(shapeFactory, u);
                    }
                };
            else
                return null;
        }
    }

    private static class ImageConnection extends URLConnection {

        private final ShapeFactory shapeFactory;

        ImageConnection(ShapeFactory shapeFactory, URL url) {
            super(url);
            this.shapeFactory = shapeFactory;
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            String path = url.getPath();
            if (path.endsWith(".png"))
                path = path.substring(0, path.length() - 4);
            BufferedImage bi = new VisualElement(path).setShapeFactory(shapeFactory).getBufferedImage(0.75, 150);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", baos);
            return new ByteArrayInputStream(baos.toByteArray());
        }
    }

    private interface ImageHandler {
        String getUrl(ElementTypeDescription description) throws IOException;
    }

    /**
     * Writes the html documentation to a file
     *
     * @param targetPath   the target folder to store the documentation
     * @param library      the library to use
     * @param shapeFactory the shapeFactory to export the shapes
     * @throws IOException IOException
     */
    public static void exportHTMLDocumentation(File targetPath, ElementLibrary library, ShapeFactory shapeFactory) throws IOException {
        File images = new File(targetPath, "img");
        if (!images.mkdir())
            throw new IOException("could not create image folder " + images);
        try (BufferedWriter w =
                     new BufferedWriter(
                             new OutputStreamWriter(
                                     new FileOutputStream(
                                             new File(targetPath, "index.html")), "UTF-8"))) {
            w.write("<!DOCTYPE html>\n"
                    + "<html>\n"
                    + "<head>\n"
                    + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n"
                    + "</head>\n<body>\n");

            writeFullHTMLDocumentation(w, library, description -> {
                final String name = description.getName();
                BufferedImage bi = new VisualElement(name).setShapeFactory(shapeFactory).getBufferedImage(0.75, 150);
                ImageIO.write(bi, "png", new File(images, name + ".png"));
                return "img/" + name + ".png";
            });
            w.write("</body>\n</html>");
        }
    }

    private static void openWebpage(URI uri) throws IOException {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE))
            desktop.browse(uri);
        else
            throw new IOException("could not open browser");
    }

    private static String escapeHTML(String text) {
        StringBuilder sb = new StringBuilder(text.length() * 2);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case 'ä':
                    sb.append("&auml;");
                    break;
                case 'ö':
                    sb.append("&ouml;");
                    break;
                case 'ü':
                    sb.append("&uuml;");
                    break;
                case 'Ä':
                    sb.append("&Auml;");
                    break;
                case 'Ö':
                    sb.append("&Ouml;");
                    break;
                case 'Ü':
                    sb.append("&Uuml;");
                    break;
                case 'ß':
                    sb.append("&szlig;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
