/*
 *  Font Chooser Class
 *      1997/11/13 (C) Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.gui;

import jp.kyasu.awt.Frame;
import jp.kyasu.awt.Dialog;
import jp.kyasu.awt.Label;
import jp.kyasu.awt.Choice;
import jp.kyasu.awt.Button;
import jp.kyasu.awt.Panel;
import jp.kyasu.awt.TextField;
import jp.kyasu.awt.BorderedPanel;
import jp.kyasu.graphics.TextStyle;
import jp.kyasu.graphics.VTitledPaneBorder;
import java.awt.Font;
import java.awt.Color;
import java.awt.Canvas;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.FontMetrics;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.StringTokenizer;
import java.util.ResourceBundle;

public class FontChooser extends Dialog {
    protected ResourceBundle rc
        = ResourceBundle.getBundle("jp.or.din.digitune.gui.resources");
    int fstyle = Font.PLAIN, fsize = 12;
    String fname = "SanSerif";
	Choice c1 = null, c2 = null;
	TextField t1 = null;
    
    public FontChooser(Frame parent) {
        super(parent, "Font Chooser", true);
        
        final Canvas sample = new Canvas() {
            public void paint(Graphics g) {
                g.setFont(new Font(fname, fstyle, fsize));
                FontMetrics fm = g.getFontMetrics();
                Rectangle r = getBounds();
                Color fg = getForeground();
                g.clearRect(r.x, r.y, r.width, r.height);
                g.drawString(rc.getString("fontsampleString1"), 2
					, fm.getHeight());
                g.drawString(rc.getString("fontsampleString2"), 2
                    , fm.getHeight() * 2);
            }
        };
        
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc;
        setLayout(gbl);
        
        Label l1 = new Label(rc.getString("fontnameLabel"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(l1, gbc);
        add(l1);
        
        Label l2 = new Label(rc.getString("fontsizeLabel"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(l2, gbc);
        add(l2);
        
        Label l3 = new Label(rc.getString("fontstyleLabel"));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(l3, gbc);
        add(l3);
        
        c1 = new Choice();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(c1, gbc);
        add(c1);
        String[] fonts = getToolkit().getFontList();
        for (int i = 0; i < fonts.length; i++)
            c1.addItem(fonts[i]);
        c1.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                fname = c1.getSelectedItem();
                sample.repaint();
            }
        });
        
        t1 = new TextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(t1, gbc);
        add(t1);
        t1.setText("12");
        t1.addTextListener(new TextListener() {
            public void textValueChanged(TextEvent te) {
                try {
                    fsize = Integer.parseInt(t1.getText());
                } catch (NumberFormatException ex) {}
                sample.repaint();
            }
        });
        
        c2 = new Choice();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(c2, gbc);
        add(c2);
        c2.addItem("PLAIN");
        c2.addItem("BOLD");
        c2.addItem("ITALIC");
        c2.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                if (c2.getSelectedItem().equals("PLAIN"))
                    fstyle = Font.PLAIN;
                else if (c2.getSelectedItem().equals("BOLD"))
                    fstyle = Font.BOLD;
                else
                    fstyle = Font.ITALIC;
                sample.repaint();
            }
        });
        
        BorderedPanel p1 = new BorderedPanel(
            new VTitledPaneBorder(rc.getString("fontsampleLabel")));
        p1.setLayout(new BorderLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(p1, gbc);
        add(p1);
        
        p1.add("Center", sample);
        
        Panel p2 = new Panel(new FlowLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(p2, gbc);
        add(p2);
        
        Button b1 = new Button(rc.getString("okLabel"));
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
                dispose();
            }
        });
        p2.add(b1);
        
        Button b2 = new Button(rc.getString("cancelLabel"));
        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                fname = null;
                fstyle = fsize = 0;
                setVisible(false);
                dispose();
            }
        });
        p2.add(b2);
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                setVisible(false);
                dispose();
            }
        });
        
        setSize(300, 256);
        Dimension scsize = getToolkit().getScreenSize();
        Dimension size = getSize();
        setLocation((scsize.width - size.width) / 2
            , (scsize.height - size.height) / 2);
    }

    public Font getFont() {
        if (fname != null)
            return new Font(fname, fstyle, fsize);
        else
            return null;
    }

	public void setFont(Font af) {
		fname = af.getName();
		c1.select(fname);
		fsize = af.getSize();
		t1.setText(Integer.toString(fsize));
		fstyle = af.getStyle();
		if ((fstyle & Font.ITALIC) != 0)
			c2.select("ITALIC");
		else if ((fstyle & Font.BOLD) != 0)
			c2.select("BOLD");
		else
			c2.select("PLAIN");
	}
    
    public TextStyle getTextStyle() {
        if (fname != null)
            return new TextStyle(fname, fstyle, fsize);
        else
            return null;
    }
    
    public static final void main(String[] args) {
        Frame p = new Frame();
        p.setVisible(true);
        FontChooser f = new FontChooser(p);
        f.setVisible(true);
    }
}
