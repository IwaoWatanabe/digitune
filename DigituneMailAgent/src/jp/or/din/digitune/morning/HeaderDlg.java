/*
 * Header Dialog Class
 *		1999/06/04 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.gui.SolidPanel;
import jp.kyasu.awt.Button;
import jp.kyasu.awt.Choice;
import jp.kyasu.awt.Frame;
import jp.kyasu.awt.Dialog;
import jp.kyasu.awt.Label;
import jp.kyasu.awt.Panel;
import jp.kyasu.awt.TableList;
import jp.kyasu.awt.TextField;
import org.ingrid.kazama.Cp932;
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
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

public class HeaderDlg extends Dialog {
	ResourceBundle rc
		= ResourceBundle.getBundle("jp.or.din.digitune.morning.resource");
	Hashtable headers = new Hashtable();
	boolean status = false;
	
	SolidPanel p1;
	TableList lists;
	TextField header, content;
	String[] titles = { rc.getString("headerLabel")
		, rc.getString("contentLabel") };
	
	public HeaderDlg(Frame p) {
		super(p, "", true);
		createWindow();
	}
	
	public HeaderDlg(Frame p, Hashtable ah) {
		super(p, "", true);
		headers = ah;
		createWindow();
	}

	public void setHeaders(Hashtable ah) {
		headers = ah;
		setLists();
	}

	public Hashtable getHeaders() {
		return headers;
	}

	public boolean getState() {
		return status;
	}

	private void setLists() {
		lists.removeAll();
		Enumeration keys = headers.keys();
		while (keys.hasMoreElements()) {
			String k = (String) keys.nextElement();
			String[] tmpstrs = { k, (String) headers.get(k) }; 
			lists.addItem(tmpstrs);
		}
	}
	
	private void createWindow() {
		setTitle(rc.getString("headerDlgTitle"));
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				setVisible(false);
				dispose();
			}
		});
		GridBagLayout gblayout = new GridBagLayout();
		setLayout(gblayout);
		p1 = new SolidPanel(new BorderLayout(), 320, 160);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 4;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(p1, gbc);
		add(p1);
		int tmpwidth = 320 - 8
			- Integer.parseInt(rc.getString("scrollbarThickness"));
		int[] widths = { tmpwidth / 2, tmpwidth - (tmpwidth / 2) };
		lists = new TableList(0, titles, widths);
		lists.setScrollbarThickness(
			Integer.parseInt(rc.getString("scrollbarThickness")));
		p1.add("Center", lists);
		Panel bp1 = new Panel(new FlowLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gblayout.setConstraints(bp1, gbc);
		add(bp1);
		Button add = new Button(rc.getString("addLabel"));
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (!header.getText().trim().equals("")
					&& !content.getText().trim().equals("")) {
					String[] tmpstrs = { header.getText().trim().toLowerCase()
						, content.getText().trim() };
					lists.addItem(tmpstrs);
				}
			}
		});
		bp1.add(add);
		Button remove = new Button(rc.getString("removeLabel"));
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int index = lists.getSelectedIndex();
				if (index < 0)
					return;
				String[] tmpstrs = lists.getRowItems(index);
				header.setText(tmpstrs[0]);
				content.setText(tmpstrs[1]);
				lists.remove(index);
				if (index > 0 || lists.getItemCount() > 0)
					lists.select((index < lists.getItemCount())
						? index : index - 1);
			}
		});
		bp1.add(remove);
		header = new TextField(16);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(header, gbc);
		add(header);
		content = new TextField(16);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(content, gbc);
		add(content);
		Panel bp2 = new Panel(new FlowLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gblayout.setConstraints(bp2, gbc);
		add(bp2);
		Button ok = new Button(rc.getString("okLabel"));
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				headers = new Hashtable();
				for (int i = 0; i < lists.getItemCount(); i++) {
					String[] tmpstrs = lists.getRowItems(i);
					System.out.println(tmpstrs[0] + ": " + tmpstrs[1]);
					headers.put(tmpstrs[0], tmpstrs[1]);
				}
				status = true;
				setVisible(false);
				dispose();
			}
		});
		bp2.add(ok);
		Button cancel = new Button(rc.getString("cancelLabel"));
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				status = false;
				setVisible(false);
				dispose();
			}
		});
		bp2.add(cancel);
		pack();
		Dimension scsize = getToolkit().getScreenSize();
		Dimension size = getSize();
		setLocation(scsize.width / 2 - size.width / 2
			, scsize.height / 2 - size.height / 2);
	}
	
	public static final void main(String[] args) {
		Frame p = new Frame();
		HeaderDlg ad = new HeaderDlg(p);
		ad.setVisible(true);
		if (ad.getState()) {
			System.out.println("Passes");
			Hashtable h = ad.getHeaders();
			Enumeration e = h.keys();
			while (e.hasMoreElements()) {
				String s = (String) e.nextElement();
				System.out.println("" + s + ": " + ((String) h.get(s)));
			}
		}
	}
}
