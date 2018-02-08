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

public class PreviewComponent extends JPanel {
	private static final long serialVersionUID = 4412186969854541035L;
	private GraphicSwing graphic;
	private VisualElement element;

	public PreviewComponent(VisualElement element) {
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(300, 300));
		this.element = new VisualElement(element);
		this.element.setPos(new Vector(20, 20));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		graphic = new GraphicSwing((Graphics2D) g);
		if (element != null) {
			element.drawTo(graphic, null);
		}
	}

	public void setInputFile(File svg, VisualElement proto) {
		try {
			element = new ImportSVG(svg).getElement(proto);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		repaint();
	}

	public VisualElement getElement() {
		return element;
	}
}