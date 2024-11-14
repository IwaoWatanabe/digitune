/*
 * Preferences Dialog Class
 *      1998/11/13 (C)Copyright T.Kazawa(Digitune)
 *		$Id: PreferenceDlg.java,v 1.2 1998/12/14 12:49:12 kazawa Exp kazawa $
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.gui.FontChooser;
import jp.or.din.digitune.util.CSV;
import org.ingrid.kazama.Cp932;
import jp.kyasu.awt.Button;
import jp.kyasu.awt.Checkbox;
import jp.kyasu.awt.Dialog;
import jp.kyasu.awt.Frame;
import jp.kyasu.awt.Label;
import jp.kyasu.awt.Panel;
import jp.kyasu.awt.TextField;
import jp.kyasu.awt.BorderedPanel;
import jp.kyasu.graphics.TextStyle;
import jp.kyasu.graphics.VPaneBorder;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.util.MissingResourceException;

public class PreferenceDlg extends Dialog {
    Frame parent = null;
    LocalResource rc = LocalResource.getResource();
    TextStyle defaultstyle = new TextStyle("sanserif", Font.PLAIN, 12);
    
    protected TextStyle string2TextStyle(String src) {
        String[] tmpstrs = CSV.cut(src);
        return new TextStyle(tmpstrs[0], Integer.parseInt(tmpstrs[1])
            , Integer.parseInt(tmpstrs[2]));
    }
    
    protected String textStyle2String(TextStyle tmpstyle) {
        StringBuffer strbuf = new StringBuffer();
        strbuf.append(tmpstyle.getFont().getName() + ",");
        strbuf.append(Integer.toString(tmpstyle.getFont().getStyle()) + ",");
        strbuf.append(Integer.toString(tmpstyle.getFont().getSize()));
        return new String(strbuf);
    }
    
    public PreferenceDlg(Frame p) {
        super(p, "", true);
        parent = p;
        createWindow();
    }
    
    private void createWindow() {
        setTitle(rc.getString("preferenceDlgTitle"));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                setVisible(false);
                dispose();
            }
        });
        
        GridBagLayout gblayout = new GridBagLayout();
        GridBagConstraints gbc;
        setLayout(gblayout);
        
        Label l1 = new Label(rc.getString("folderfontLabel"), Label.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(l1, gbc);
        add(l1);
        
        BorderedPanel bp1 = new BorderedPanel(new VPaneBorder());
        bp1.setLayout(new BorderLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(bp1, gbc);
        add(bp1);
        TextStyle tmpstyle = null;
        try {
            tmpstyle = string2TextStyle(rc.getString("folderlistFont"));
        } catch (MissingResourceException ex) {
            tmpstyle = defaultstyle;
        }
        final Label l2 = new Label(textStyle2String(tmpstyle), Label.CENTER);
        l2.setFont(tmpstyle.getFont());
        bp1.add("Center", l2);
        
        Button b1 = new Button(rc.getString("choiseLabel"));
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                FontChooser fc = new FontChooser(parent);
        		TextStyle oldstyle = null;
    		    try {
					oldstyle = string2TextStyle(rc.getString("folderlistFont"));
				} catch (MissingResourceException ex) {
					oldstyle = defaultstyle;
				}
				fc.setFont(oldstyle.getFont());
                fc.setVisible(true);
                if (fc.getTextStyle() != null) {
                    l2.setText(textStyle2String(fc.getTextStyle()));
                    l2.setFont(fc.getTextStyle().getFont());
                }
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(b1, gbc);
        add(b1);
        
        Label l3 = new Label(rc.getString("mailfontLabel"), Label.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(l3, gbc);
        add(l3);
        
        BorderedPanel bp2 = new BorderedPanel(new VPaneBorder());
        bp2.setLayout(new BorderLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(bp2, gbc);
        add(bp2);
        try {
            tmpstyle = string2TextStyle(rc.getString("maillistFont"));
        } catch (MissingResourceException ex) {
            tmpstyle = defaultstyle;
        }
        final Label l4 = new Label(textStyle2String(tmpstyle), Label.CENTER);
        l4.setFont(tmpstyle.getFont());
        bp2.add("Center", l4);
        
        Button b2 = new Button(rc.getString("choiseLabel"));
        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                FontChooser fc = new FontChooser(parent);
        		TextStyle oldstyle = null;
    		    try {
					oldstyle = string2TextStyle(rc.getString("maillistFont"));
				} catch (MissingResourceException ex) {
					oldstyle = defaultstyle;
				}
				fc.setFont(oldstyle.getFont());
                fc.setVisible(true);
                if (fc.getTextStyle() != null) {
                    l4.setText(textStyle2String(fc.getTextStyle()));
                    l4.setFont(fc.getTextStyle().getFont());
                }
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(b2, gbc);
        add(b2);
        
        Label l5 = new Label(rc.getString("bodyfontLabel"), Label.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(l5, gbc);
        add(l5);
        
        BorderedPanel bp3 = new BorderedPanel(new VPaneBorder());
        bp3.setLayout(new BorderLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(bp3, gbc);
        add(bp3);
        try {
            tmpstyle = string2TextStyle(rc.getString("mailbodyFont"));
        } catch (MissingResourceException ex) {
            tmpstyle = defaultstyle;
        }
        final Label l6 = new Label(textStyle2String(tmpstyle), Label.CENTER);
        l6.setFont(tmpstyle.getFont());
        bp3.add("Center", l6);
        
        Button b3 = new Button(rc.getString("choiseLabel"));
        b3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                FontChooser fc = new FontChooser(parent);
        		TextStyle oldstyle = null;
    		    try {
					oldstyle = string2TextStyle(rc.getString("mailbodyFont"));
				} catch (MissingResourceException ex) {
					oldstyle = defaultstyle;
				}
				fc.setFont(oldstyle.getFont());
                fc.setVisible(true);
                if (fc.getTextStyle() != null) {
                    l6.setText(textStyle2String(fc.getTextStyle()));
                    l6.setFont(fc.getTextStyle().getFont());
                }
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(b3, gbc);
        add(b3);
        
        Label l7 = new Label(rc.getString("linespaceLabel"), Label.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(l7, gbc);
        add(l7);
        
        final TextField t1 = new TextField(8);
        t1.setText(rc.getString("lineSpace"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(t1, gbc);
        add(t1);
        
        Label l8 = new Label(rc.getString("scrollbarThicknessLabel")
            , Label.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(l8, gbc);
        add(l8);
        
        final TextField t2 = new TextField(8);
        t2.setText(rc.getString("scrollbarThickness"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(t2, gbc);
        add(t2);

		final Checkbox c1 = new Checkbox(rc.getString("lineWrapLabel"));
		try {
			c1.setState(new Boolean(rc.getString("lineWrap")).booleanValue());
		} catch (MissingResourceException ex) {
			c1.setState(false);
		}
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
		gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(c1, gbc);
        add(c1);
        
        Panel p1 = new Panel(new FlowLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(p1, gbc);
        add(p1);
        
        Button ok = new Button(rc.getString("okLabel"));
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    int i = Integer.parseInt(t1.getText());
                    int j = Integer.parseInt(t2.getText());
                } catch (NumberFormatException ex) {
                    getToolkit().beep();
                    return;
                }
                rc.setString("folderlistFont", l2.getText());
                rc.setString("maillistFont", l4.getText());
                rc.setString("mailbodyFont", l6.getText());
                rc.setString("lineSpace", t1.getText());
                rc.setString("scrollbarThickness", t2.getText());
				rc.setString("lineWrap"
					, new Boolean(c1.getState()).toString());
                setVisible(false);
                dispose();
            }
        });
        p1.add(ok);
        
        Button cancel = new Button(rc.getString("cancelLabel"));
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
                dispose();
            }
        });
        p1.add(cancel);
        
        pack();
        Dimension scsize = getToolkit().getScreenSize();
        Dimension size = getSize();
        setLocation(scsize.width / 2 - size.width / 2
            , scsize.height / 2 - size.height / 2);
    }
    
    public static final void main(String[] args) {
        Frame p = new Frame();
        p.setVisible(true);
        PreferenceDlg pd = new PreferenceDlg(p);
        pd.setVisible(true);
    }
}
