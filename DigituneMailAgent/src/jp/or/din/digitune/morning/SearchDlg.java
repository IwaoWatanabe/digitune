/*
 * Search Dialog Class
 *      1999/03/04 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import gnu.regexp.RE;
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
import java.util.ResourceBundle;
import java.util.MissingResourceException;

public class SearchDlg extends Dialog {
    Frame parent = null;
    ResourceBundle rc
		= ResourceBundle.getBundle("jp.or.din.digitune.morning.resource");
	String searchstr = "";
	boolean status = false, currentonly = false, ignorecase = false
		, regexp = false;
    
    public SearchDlg(Frame p) {
        super(p, "", true);
        parent = p;
        createWindow();
    }
    
    private void createWindow() {
        setTitle(rc.getString("searchDlgTitle"));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                setVisible(false);
                dispose();
            }
        });
        
        GridBagLayout gblayout = new GridBagLayout();
        GridBagConstraints gbc;
        setLayout(gblayout);
        
        Label l1 = new Label(rc.getString("searchstrinputLabel"), Label.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(l1, gbc);
        add(l1);
        
        final TextField t1 = new TextField(24);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(t1, gbc);
        add(t1);
        
		final Checkbox c1
			= new Checkbox(rc.getString("currentfolderonlyLabel"));
		c1.setState(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(c1, gbc);
        add(c1);
        
		final Checkbox c2
			= new Checkbox(rc.getString("ignorecaseLabel"));
		c2.setState(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(c2, gbc);
        add(c2);
        
		final Checkbox c3
			= new Checkbox(rc.getString("regexpLabel"));
		c3.setState(false);
		try {
			Class.forName("gnu.regexp.RE");
		} catch (Exception ex) {
			c3.setEnabled(false);
		}
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(c3, gbc);
        add(c3);
        
        Panel p1 = new Panel(new FlowLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gblayout.setConstraints(p1, gbc);
        add(p1);
        
        Button ok = new Button(rc.getString("okLabel"));
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
				searchstr = t1.getText();
				currentonly = c1.getState();
				ignorecase = c2.getState();
				regexp = c3.getState();
				status = true;
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

	public String getString() {
		return searchstr;
	}

	public boolean getCurrentFolderOnlyState() {
		return currentonly;
	}

	public boolean getIgnoreCaseState() {
		return ignorecase;
	}

	public boolean getRegexpState() {
		return regexp;
	}

	public boolean getState() {
		return status;
	}
    
    public static final void main(String[] args) {
        Frame p = new Frame();
        p.setVisible(true);
        SearchDlg pd = new SearchDlg(p);
        pd.setVisible(true);
    }
}
