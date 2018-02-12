package de.neemann.digital.gui.components.graphics.svgimport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.GraphicSwing;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.svg.ImportSVG;
import de.neemann.gui.ErrorMessage;

public class PreviewComponent extends JPanel {
	private static final long serialVersionUID = 4412186969854541035L;
	private GraphicSwing graphic;
	private VisualElement element;

	public PreviewComponent(VisualElement element) {
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(300, 200));
		this.element = element;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (element != null) {
			Vector pos=new Vector(element.getPos());
			element.setPos(new Vector(20,20));
			graphic = new GraphicSwing((Graphics2D)g);
			element.drawTo(graphic, null);
			element.setPos(pos);
		}
	}

	public void setInputFile(File svg, VisualElement proto) {
		try {
			element = new ImportSVG(svg).getElement(proto);
		} catch (ParserConfigurationException e) {
			new ErrorMessage("Das Parsen der SVG Datei ist fehlgeschlagen (constStr)").addCause(e).show(this);
			e.printStackTrace();
		} catch (SAXException e) {
			new ErrorMessage("Das Parsen der SVG Datei ist fehlgeschlagen (constStr)").addCause(e).show(this);
			e.printStackTrace();
		} catch (IOException e) {
			new ErrorMessage("Die Datei " + svg.getName() + " konnte nicht gefunden werden (constStr)").addCause(e)
					.show(this);
			e.printStackTrace();
		}
		repaint();
	}

	public VisualElement getElement() {
		return element;
	}
}