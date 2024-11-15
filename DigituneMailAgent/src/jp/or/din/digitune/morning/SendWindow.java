/*
 * Send Message Window Class
 *      1998/04/14 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.gui.InputBox;
import jp.or.din.digitune.util.CSV;
import jp.or.din.digitune.util.ResourceProperties;
import jp.kyasu.awt.Frame;
import jp.kyasu.awt.Button;
import jp.kyasu.awt.Choice;
import jp.kyasu.awt.Panel;
import jp.kyasu.awt.Label;
import jp.kyasu.awt.TextField;
import jp.kyasu.awt.TextArea;
import jp.kyasu.awt.text.Keymap;
import jp.kyasu.graphics.TextStyle;
import org.ingrid.kazama.Cp932;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.util.MissingResourceException;

public class SendWindow extends Frame {
    LocalResource rc = LocalResource.getResource();
	MainWindow mw;
    SendMessage sm;
    String recentheader = "to";
    
    protected TextStyle string2TextStyle(String src) {
        String[] tmpstrs = CSV.cut(src);
        return new TextStyle(tmpstrs[0], Integer.parseInt(tmpstrs[1])
            , Integer.parseInt(tmpstrs[2]));
    }
    
    public SendWindow(MainWindow amw, SendMessage asm) {
		mw = amw;
        sm = asm;
        createWindow();
    }
    
    void saveGUIPref() {
        int[] tmpsize = new int[2];
        tmpsize[0] = this.getLocation().x;
        tmpsize[1] = this.getLocation().y;
        rc.setString("sendwindowLocation", CSV.toString(tmpsize));
        tmpsize[0] = this.getSize().width;
        tmpsize[1] = this.getSize().height;
        rc.setString("sendwindowSize", CSV.toString(tmpsize));
    }
    
    TextField to, subject, attach;
    TextArea body;
    
    void createWindow() {
        setTitle(rc.getString("sendWindowTitle"));
        setLayout(new BorderLayout());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                saveGUIPref();
                setVisible(false);
                dispose();
				mw.refreshMailCount();
            }
            public void windowClosed(WindowEvent we) {
                rc.save();
            }
        });
        Panel np = new Panel(new BorderLayout());
        add("Center", np);
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc;
        Panel p1 = new Panel(gbl);
        np.add("North", p1);

        final Choice headers = new Choice();
        headers.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                if (!to.getText().trim().equals(""))
                    sm.setHeader(recentheader
                        , 0, Cp932.toJIS(to.getText().trim()));
                recentheader = headers.getSelectedItem();
                if (sm.getHeader(recentheader) != null)
                    to.setText(Cp932.toCp932(sm.getHeader(recentheader)[0]));
                else
                    to.setText("");
                sm.removeHeader(recentheader);
            }
        });
        headers.add("to");
        headers.add("cc");
        headers.add("bcc");
        Hashtable tmph = sm.getHeaders();
        for (Enumeration en = tmph.keys(); en.hasMoreElements(); ) {
            String tmpstr = (String) en.nextElement();
            if (tmpstr.equals("to") || tmpstr.equals("cc")
                || tmpstr.equals("bcc"))
                continue;
            headers.add(tmpstr);
        }
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbl.setConstraints(headers, gbc);
        p1.add(headers);

        Label lSubject = new Label("Subject:", Label.LEFT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbl.setConstraints(lSubject, gbc);
        p1.add(lSubject);

        Label lAttach = new Label("Attach:", Label.LEFT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbl.setConstraints(lAttach, gbc);
        p1.add(lAttach);

        to = new TextField();
        if (sm.getHeader("to") != null) {
            to.setText(Cp932.toCp932(sm.getHeader("to")[0]));
            sm.removeHeader("to");
        }
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbl.setConstraints(to, gbc);
        p1.add(to);

        subject = new TextField();
        if (sm.getHeader("subject") != null) {
            subject.setText(Cp932.toCp932(sm.getHeader("subject")[0]));
            sm.removeHeader("subject");
        }
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbl.setConstraints(subject, gbc);
        p1.add(subject);

        attach = new TextField();
        if (sm.getAttachFiles() != null) {
            attach.setText(CSV.toString(sm.getAttachFiles()));
            sm.clearAttachFile();
        }
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbl.setConstraints(attach, gbc);
        p1.add(attach);

        Button bAddress = new Button("Address");
        bAddress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                AddressDlg ad = new AddressDlg(SendWindow.this);
                ad.setVisible(true);
                if (ad.getSelectedAddress() != null) {
                    String[] tmpstrs = ad.getSelectedAddress();
                    for (int i = 0; i < tmpstrs.length; i++)
                        if (!to.getText().trim().equals(""))
                            to.setText(to.getText() + ", "
                                + Cp932.toCp932(tmpstrs[i]));
                        else
                            to.setText(Cp932.toCp932(tmpstrs[i]));
                }
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbl.setConstraints(bAddress, gbc);
        p1.add(bAddress);

        Button bHeader = new Button("Header");
        bHeader.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                InputBox ib = new InputBox(SendWindow.this
                    , rc.getString("addheaderTitle")
                    , rc.getString("inputNewHeaderStr"));
                ib.setVisible(true);
                if (ib.getStatus())
                    headers.add(ib.getString());
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbl.setConstraints(bHeader, gbc);
        p1.add(bHeader);

        Button bAttach = new Button("Browse");
        bAttach.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                FileDialog fd = new FileDialog(SendWindow.this);
                fd.setMode(FileDialog.LOAD);
                fd.setVisible(true);
                if (fd.getFile() != null) {
                    StringBuffer strbuf = new StringBuffer();
                    if (!attach.getText().equals(""))
                        strbuf.append(attach.getText() + ",");
                    strbuf.append(fd.getDirectory() + fd.getFile());
                    attach.setText(new String(strbuf));
                }
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbl.setConstraints(bAttach, gbc);
        p1.add(bAttach);

        body = new TextArea();
//		Keymap tmpmap = body.getKeymap();
//		tmpmap.setKeyCodeMap(KeyEvent.VK_DELETE, "delete-next-character");
//		body.setKeymap(tmpmap);
		try {
        	body.setFont(string2TextStyle(
				rc.getString("mailbodyFont")).getFont());
		} catch (MissingResourceException ex) {}
		try {
        	body.setScrollbarThickness(
            	Integer.parseInt(rc.getString("scrollbarThickness")));
		} catch (MissingResourceException ex) {}
        body.setText(Cp932.toCp932(sm.getBody()));
        np.add("Center", body);
        Panel p2 = new Panel(new FlowLayout(FlowLayout.RIGHT));
        np.add("South", p2);
        jp.kyasu.awt.Button draft
			= new jp.kyasu.awt.Button(rc.getString("draftLabel"));
        draft.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!setSendMessage()) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                sm.writeMessage();
                sm.writeDraftMessageInfo();
                saveGUIPref();
                setVisible(false);
                dispose();
				mw.refreshMailCount();
            }
        });
        p2.add(draft);
        jp.kyasu.awt.Button send
			= new jp.kyasu.awt.Button(rc.getString("sendLabel"));
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!setSendMessage()) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                sm.writeMessage();
                sm.writeMessageInfo();
                saveGUIPref();
                setVisible(false);
                dispose();
				mw.refreshMailCount();
            }
        });
        p2.add(send);
        jp.kyasu.awt.Button cancel
			= new jp.kyasu.awt.Button(rc.getString("cancelLabel"));
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                saveGUIPref();
                setVisible(false);
                dispose();
				mw.refreshMailCount();
            }
        });
        p2.add(cancel);
        try {
            int[] tmpsize;
            tmpsize = CSV.cutInt(rc.getString("sendwindowLocation"));
            this.setLocation(tmpsize[0], tmpsize[1]);
            tmpsize = CSV.cutInt(rc.getString("sendwindowSize"));
            this.setSize(tmpsize[0], tmpsize[1]);
        } catch (MissingResourceException ex) {
            pack();
        }
        setVisible(true);
        toFront();
    }
    
    boolean setSendMessage() {
        if (!to.getText().trim().equals(""))
            sm.setHeader(recentheader
                , 0, Cp932.toJIS(to.getText().trim()));
        if (sm.getHeader("to") == null && sm.getHeader("cc") == null
            && sm.getHeader("bcc") == null)
            return false;
        if (!subject.getText().trim().equals(""))
            sm.setHeader("subject", 0, Cp932.toJIS(subject.getText()).trim());
        if (!attach.getText().trim().equals("")) {
            String[] attfiles = CSV.cut(attach.getText().trim());
            for (int i = 0; i < attfiles.length; i++)
                sm.addAttachFile(attfiles[i]);
        }
        sm.setBody(Cp932.toJIS(body.getText()));
        return true;
    }
    
    //  for test.
    public final static void main(String[] args) {
        new SendWindow(null, new SendMessage(new jp.kyasu.awt.Frame()
            , new Account()));
    }
}
