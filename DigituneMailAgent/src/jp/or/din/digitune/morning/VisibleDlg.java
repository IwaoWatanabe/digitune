/*
 * Visible Header Selection Dialog Class
 *      1998/11/13 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.util.CSV;
import jp.kyasu.awt.Button;
import jp.kyasu.awt.Frame;
import jp.kyasu.awt.Dialog;
import jp.kyasu.awt.Label;
import jp.kyasu.awt.Panel;
import jp.kyasu.awt.List;
import jp.kyasu.awt.TextField;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.util.MissingResourceException;

public class VisibleDlg extends Dialog {
    LocalResource rc = LocalResource.getResource();
    
    public VisibleDlg(Frame p) {
        super(p, "", true);
        createWindow();
    }
    
    private void createWindow() {
        setTitle(rc.getString("visibleDlgTitle"));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                setVisible(false);
                dispose();
            }
        });
        
        GridBagLayout gblayout = new GridBagLayout();
        GridBagConstraints gbc;
        setLayout(gblayout);
        
        final TextField t1 = new TextField();
        
        final List l1 = new List();
        String str = null;
        try {
            str = rc.getString("visibleHeader");
        } catch (MissingResourceException ex) {
            str = rc.getString("defaultVisibleHeaders");
        }
        String[] strs = CSV.cut(str);
        for (int i = 0; i < strs.length; i++)
            l1.add(strs[i]);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        gblayout.setConstraints(l1, gbc);
        add(l1);
        
        Panel bp1 = new Panel(new FlowLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        gblayout.setConstraints(bp1, gbc);
        add(bp1);
        
        Button add = new Button(rc.getString("addLabel"));
        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!t1.getText().trim().equals("")) {
                    l1.add(t1.getText().trim().toLowerCase());
                }
            }
        });
        bp1.add(add);
        
        Button remove = new Button(rc.getString("removeLabel"));
        remove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int index = l1.getSelectedIndex();
                if (index < 0)
                    return;
                t1.setText(l1.getSelectedItem());
                l1.remove(index);
                if (index > 0 || l1.getItemCount() > 0)
                    l1.select((index < l1.getItemCount())
                        ? index : index - 1);
            }
        });
        bp1.add(remove);
        
        Button up = new Button(rc.getString("upLabel"));
        up.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int index = l1.getSelectedIndex();
                if (index < 0)
                    return;
                String tmpstr = l1.getSelectedItem();
                l1.remove(index);
                index = ((index - 1) > 0) ? index - 1:0;
                l1.add(tmpstr, index);
                l1.select(index);
            }
        });
        bp1.add(up);
        
        Button down = new Button(rc.getString("downLabel"));
        down.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int index = l1.getSelectedIndex();
                if (index < 0)
                    return;
                String tmpstr = l1.getSelectedItem();
                l1.remove(index);
                index = ((index + 1) <= l1.getItemCount())
                    ? index + 1 : index;
                l1.add(tmpstr, index);
                l1.select(index);
            }
        });
        bp1.add(down);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 2, 0, 2);
        gblayout.setConstraints(t1, gbc);
        add(t1);
        
        Panel bp2 = new Panel(new FlowLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        gblayout.setConstraints(bp2, gbc);
        add(bp2);
        
        Button ok = new Button(rc.getString("okLabel"));
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int c = l1.getItemCount();
                if (c > 0) {
                    String[] tmpstrs = new String[c];
                    for (int i = 0; i < c; i++)
                        tmpstrs[i] = l1.getItem(i);
                    rc.setString("visibleHeader"
                        , CSV.toStringWithQuote(tmpstrs));
                }
                setVisible(false);
                dispose();
            }
        });
        bp2.add(ok);
        
        Button cancel = new Button(rc.getString("cancelLabel"));
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
                dispose();
            }
        });
        bp2.add(cancel);
        
        setSize(256, 256); // xxx
        Dimension scsize = getToolkit().getScreenSize();
        Dimension size = getSize();
        setLocation(scsize.width / 2 - size.width / 2
            , scsize.height / 2 - size.height / 2);
    }
    
    public static final void main(String[] args) {
        Frame p = new Frame();
        p.setVisible(true);
        VisibleDlg ad = new VisibleDlg(p);
        ad.setVisible(true);
    }
}
