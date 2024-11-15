/*
 *  Address Dialog Class
 *      1998/08/27 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.util.CSV;
import jp.kyasu.awt.Button;
import jp.kyasu.awt.Dialog;
import jp.kyasu.awt.Frame;
import jp.kyasu.awt.Label;
import jp.kyasu.awt.List;
import jp.kyasu.awt.Panel;
import jp.kyasu.awt.TextField;
import org.ingrid.kazama.Cp932;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;

public class AddressDlg extends Dialog {
    LocalResource rc = LocalResource.getResource();
    Vector addrs = new Vector();
    TextField t1, t2, t3;
    String[] selectedaddress = null;

    private void createWindow() {
        setTitle(rc.getString("addressTitle"));
        setLayout(new GridLayout(1, 2));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                setVisible(false);
                dispose();
            }
        });

        try {
            FileInputStream fin
                = new FileInputStream(rc.getString("addressFilename"));
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(fin, "UTF8"));
            String tmpstr;
            while ((tmpstr = reader.readLine()) != null) {
                String[] strs = CSV.cut(tmpstr);
                addrs.addElement(strs);
            }
            reader.close();
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        final List addresses
            = new List(Integer.parseInt(rc.getString("addressLines")));
        addresses.setSelectionMode(List.SHIFT_MULTIPLE_SELECTIONS);
        addresses.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                int idx = addresses.getSelectedIndex();
                if (idx < 0)
                    return;
                String[] strs = (String[]) addrs.elementAt(idx);
                t1.setText(Cp932.toCp932(strs[0]));
                t2.setText(Cp932.toCp932(strs[1]));
                if (strs.length > 2)
                    t3.setText(Cp932.toCp932(strs[2]));
                else
                    t3.setText("");
            }
        });
        addresses.setScrollbarThickness(
            Integer.parseInt(rc.getString("scrollbarThickness")));
        for (Enumeration en = addrs.elements(); en.hasMoreElements(); ) {
            String[] strs = (String[]) en.nextElement();
            String tmpstr = strs[0] + " <" + strs[1] + ">";
            addresses.add(tmpstr);
        }
        add(addresses);

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc;
        Panel p1 = new Panel(gbl);
        add(p1);

        Label l1 = new Label(rc.getString("usernameLabel"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(l1, gbc);
        p1.add(l1);

        t1 = new TextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(t1, gbc);
        p1.add(t1);

        Label l2 = new Label(rc.getString("useraddressLabel"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(l2, gbc);
        p1.add(l2);

        t2 = new TextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(t2, gbc);
        p1.add(t2);

        Label l3 = new Label(rc.getString("commentLabel"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(l3, gbc);
        p1.add(l3);

        t3 = new TextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(t3, gbc);
        p1.add(t3);

        Button addb = new Button(rc.getString("addLabel"));
        addb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String[] strs = new String[3];
                if ((strs[0] = Cp932.toJIS(t1.getText())).equals(""))
                    return;
                if ((strs[1] = Cp932.toJIS(t2.getText())).equals(""))
                    return;
                strs[2] = Cp932.toJIS(t3.getText());
                for (int i = 0; i < addrs.size(); i++)
                    if (strs[1].equals(((String[]) addrs.elementAt(i))[1])) {
                        addrs.removeElementAt(i);
                        addresses.remove(i);
                    }
                addrs.insertElementAt(strs, 0);
                addresses.add(strs[0] + " <" + strs[1] + ">", 0);
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbl.setConstraints(addb, gbc);
        p1.add(addb);

        Button removeb = new Button(rc.getString("removeLabel"));
        removeb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int idx = addresses.getSelectedIndex();
                if (idx < 0)
                    return;
                addrs.removeElementAt(idx);
                addresses.remove(idx);
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbl.setConstraints(removeb, gbc);
        p1.add(removeb);

        Button okb = new Button(rc.getString("okLabel"));
        okb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                selectedaddress = addresses.getSelectedItems();
                try {
                    FileOutputStream fout
                        = new FileOutputStream(rc.getString("addressFilename"));
                    BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(fout, "UTF8"));
                    for (int i = 0; i < addrs.size(); i++) {
                        String[] strs = (String[]) addrs.elementAt(i);
                        String tmpstr = CSV.toStringWithQuote(strs);
                        writer.write(tmpstr, 0, tmpstr.length());
                        writer.newLine();
                    }
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                setVisible(false);
                dispose();
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(2, 10, 2, 2);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbl.setConstraints(okb, gbc);
        p1.add(okb);

        Button cancelb = new Button(rc.getString("cancelLabel"));
        cancelb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
                dispose();
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 6;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbl.setConstraints(cancelb, gbc);
        p1.add(cancelb);

        setSize(480, 320); // xxx
        Dimension scsize = getToolkit().getScreenSize();
        Dimension size = getSize();
        setLocation((scsize.width - size.width) / 2
            , (scsize.height - size.height) / 2);
    }
    
    public AddressDlg(Frame p) {
        super(p, "", true);
        createWindow();
    }

    public String[] getSelectedAddress() {
        return selectedaddress;
    }

    public void setName(String an) {
        t1.setText(Cp932.toCp932(an));
    }

    public void setAddress(String aa) {
        t2.setText(Cp932.toCp932(aa));
    }

    public void setComment(String ac) {
        t3.setText(Cp932.toCp932(ac));
    }

    public static final void main(String[] args) {
        Frame f = new Frame();
        AddressDlg ad = new AddressDlg(f);
        ad.setVisible(true);
    }
}
