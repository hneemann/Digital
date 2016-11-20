package de.neemann.digital.docu;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.*;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.language.Language;
import junit.framework.TestCase;
import org.apache.fop.apps.*;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * This is not a real test case.
 * This piece of code reads the elements from the library and creates an XML document containing all descriptive
 * information used to create th tooltips in teh program.  After that Xalan is used to transform the XML document
 * to a XSL-FO document. Then FOP is used to read the XSL-FO file and to compile it to a PDF document.
 * The PDF document is then included in the distribution ZIP.
 *
 * Created by hneemann on 17.11.16.
 */
public class DocuTest extends TestCase {
    private static final int IMAGE_SCALE = 4;

    public void writeXML(Writer w, File images, String language) throws IOException, NodeException, PinException {
        w.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        w.append("<?xml-stylesheet type=\"text/xsl\" href=\"elem2html.xslt\"?>\n");
        w.append("<root titel=\"")
                .append(Lang.get("digital"))
                .append("\" titleImage=\"")
                .append(new File(Resources.getRoot(),"../../../screenshot.png").toString())
                .append("\" inhalt=\"")
                .append(Lang.get("tableOfContent"))
                .append("\">\n");
        ElementLibrary library = new ElementLibrary();
        ShapeFactory shapeFactory = new ShapeFactory(library, language.equals("en"));
        String actPath = null;
        for (ElementLibrary.ElementContainer e : library) {
            String p = e.getTreePath();
            if (!p.equals(actPath)) {
                if (actPath != null)
                    w.append("  </lib>\n");

                actPath = p;
                w.append("  <lib name=\"").append(actPath).append("\">\n");
            }


            final ElementTypeDescription etd = e.getDescription();
            String imageName=etd.getName()+"_"+language;
            w.append("    <element name=\"")
                    .append(escapeHTML(etd.getTranslatedName()))
                    .append("\" img=\"")
                    .append(new File(images,imageName).toString())
                    .append("\">\n");

            BufferedImage bi = new VisualElement(etd.getName()).setShapeFactory(shapeFactory).getBufferedImage(0.75 * IMAGE_SCALE, 250 * IMAGE_SCALE);
            ImageIO.write(bi, "png", new File(images, imageName + ".png"));

            final ElementAttributes attr = new ElementAttributes();
            w.append("      <descr>").append(escapeHTML(etd.getDescription(attr))).append("</descr>\n");
            final PinDescriptions inputDescription = etd.getInputDescription(attr);
            if (!inputDescription.isEmpty()) {
                w.append("      <inputs name=\"").append(Lang.get("elem_Help_inputs")).append("\">\n");
                writePins(w, inputDescription);
                w.append("      </inputs>\n");
            }
            final PinDescriptions outputDescriptions = etd.getOutputDescriptions(attr);
            if (!outputDescriptions.isEmpty()) {
                w.append("      <outputs name=\"").append(Lang.get("elem_Help_outputs")).append("\">\n");
                writePins(w, outputDescriptions);
                w.append("      </outputs>\n");
            }

            if (etd.getAttributeList().size() > 0) {
                w.append("      <attributes name=\"").append(Lang.get("elem_Help_attributes")).append("\">\n");
                for (Key k : etd.getAttributeList()) {
                    w.append("        <attr name=\"").append(escapeHTML(k.getName())).append("\">\n");
                    w.append(escapeHTML(k.getDescription()));
                    w.append("        </attr>\n");
                }
                w.append("      </attributes>\n");
            }

            w.append("    </element>\n");
        }
        w.append("  </lib>\n");
        w.append("</root>");
    }

    private void writePins(Writer w, PinDescriptions pinDescriptions) throws IOException {
        for (PinDescription p : pinDescriptions) {
            w.append("        <pin name=\"").append(escapeHTML(p.getName())).append("\">\n");
            w.append(escapeHTML(p.getDescription()));
            w.append("        </pin>\n");
        }
    }

    private static String escapeHTML(String text) {
        StringBuilder sb = new StringBuilder(text.length() * 2);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
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
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }


    private void startXalan(File xmlIn, File xslt, File xmlOut) throws TransformerException, FileNotFoundException {
        // 1. Instantiate a TransformerFactory.
        javax.xml.transform.TransformerFactory tFactory =
                javax.xml.transform.TransformerFactory.newInstance();

        // 2. Use the TransformerFactory to process the stylesheet Source and
        //    generate a Transformer.
        javax.xml.transform.Transformer transformer = tFactory.newTransformer
                (new javax.xml.transform.stream.StreamSource(xslt));

        // 3. Use the Transformer to transform an XML Source and send the
        //    output to a Result object.
        transformer.transform
                (new javax.xml.transform.stream.StreamSource(xmlIn),
                        new javax.xml.transform.stream.StreamResult( new
                                java.io.FileOutputStream(xmlOut)));
    }

    private void startFOP(FopFactory fopFactory, File fopFile, File outFile) throws IOException, TransformerException, FOPException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));

        try {
            // Step 3: Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

            // Step 4: Setup JAXP using identity transformer
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(); // identity transformer

            // Step 5: Setup input and output for XSLT transformation
            // Setup input stream
            Source src = new StreamSource(fopFile);

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Step 6: Start XSLT transformation and FOP processing
            transformer.transform(src, res);

        } finally {
            //Clean-up
            out.close();
        }
    }

    public void testDocu() throws IOException, NodeException, PinException, TransformerException, SAXException {
        FopFactory fopFactory = FopFactory.newInstance(new File(Resources.getRoot(), "fop.xconf"));

        File maven = Resources.getRoot().getParentFile().getParentFile().getParentFile();
        File target = new File(maven,"target/xslt");
        File images = new File(target, "img");
        images.mkdirs();
        for (Language l : Lang.getBundle().getSupportedLanguages()) {
            // set language
            Lang.setActualRuntimeLanguage(l);
            final String basename = "docu_" + l.getName();
            // write xml
            File xml = new File(target, basename + ".xml");
            try (Writer w = new OutputStreamWriter(new FileOutputStream(xml), "UTF-8")) {
                writeXML(w, images, l.getName());
            }

            // start xslt transformation
            File xslFO = new File(target, basename + ".fo");
            File xslt = new File(Resources.getRoot(), "elem2fo.xslt");
            startXalan(xml, xslt, xslFO);

            // write pdf
            File pdf = new File(target, basename + ".pdf");
            startFOP(fopFactory, xslFO, pdf);
        }
    }
}
