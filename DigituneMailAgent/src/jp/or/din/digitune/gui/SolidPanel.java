/*
 *	Solid Panel Class
 *		1997/06/25 (C) Copyright T.Kazawa
 */

package jp.or.din.digitune.gui;

import jp.kyasu.awt.Panel;
import java.awt.LayoutManager;
import java.awt.Dimension;

public class SolidPanel extends Panel
{
	int width, height;
	
	public SolidPanel(LayoutManager lm, int w, int h) {
		super(lm);
		setPreferredSize(w, h);
	}
	
	public void setPreferredSize(int w, int h) {
		width = w;
		height = h;
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}
	
	public Dimension getMaximumSize() {
		return new Dimension(width, height);
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(width, height);
	}
}
