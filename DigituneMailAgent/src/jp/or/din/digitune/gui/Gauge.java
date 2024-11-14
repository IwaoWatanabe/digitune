/*
 *	Gauge Component
 *		1997/04/06 (C) Copyright T.Kazawa
 */

package jp.or.din.digitune.gui;

import java.awt.Component;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Dimension;

public class Gauge extends Component
{
	int max = 100, current = 0;
	int width = 256, height = 16;
	
	public Gauge() {
	}
	
	public Gauge(int argfull) {
		max = argfull;
	}
	
	public Gauge(int argfull, int argwidth, int argheight) {
		this(argfull);
		width = argwidth;
		height = argheight;
	}
	
	public void setCurrentValue(int value) {
		current = value;
		updateValue();
	}
	
	public int getCurrentValue() {
		return current;
	}
	
	public void setMaxValue(int argfull) {
		max = argfull;
	}
	
	public int getMaxValue() {
		return max;
	}
	
	public void updateValue() {
		Graphics g = Gauge.this.getGraphics();
		if (g != null) {
			paint(g);
			g.dispose();
		}
	}
	
	public void paint(Graphics g) {
		if (max != 0) {
			int tmpwidth = (int) (((float) current / (float) max) * width);
			g.setColor(getBackground());
			g.fill3DRect(tmpwidth, 0, width - tmpwidth, height - 2, false);
			g.setColor(Color.red.darker());
			g.fill3DRect(0, 0, tmpwidth, height - 2, true);
		}
	}
	
	public void update(Graphics g) {
		paint(g);
	}
	
	public void setPreferredSize(Dimension d) {
		width = d.width;
		height = d.height;
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(width, height);
	}
}
