package de.neemann.digital.gui.components.graphics.svgimport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.neemann.digital.draw.graphics.GraphicSwing;
import de.neemann.digital.draw.graphics.svg.Drawable;
import de.neemann.digital.draw.graphics.svg.ImportSVG;
import de.neemann.digital.draw.graphics.svg.SVGFragment;

public class PreviewComponent extends JPanel {
	private static final long serialVersionUID = 4412186969854541035L;
	private GraphicSwing graphic;
	private ArrayList<SVGFragment> fragments;

	public PreviewComponent() {
		setBackground(Color.WHITE);
		setBorder(new EmptyBorder(20, 20, 20, 20));
		setPreferredSize(new Dimension(300, 300));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		graphic = new GraphicSwing((Graphics2D) g);
		if (fragments != null) {
			for (SVGFragment f : fragments) {
				if (f != null && f.getDrawables() != null) {
					for (Drawable d : f.getDrawables()) {
						if (d != null) {
							d.draw(graphic);
						}
					}
				}
			}
		}
	}

	public void getInputFile(File svg) {
		try {
			fragments = new ImportSVG(svg).getFragments();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		repaint();
	}
}