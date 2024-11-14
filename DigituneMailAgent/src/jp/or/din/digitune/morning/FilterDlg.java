/*
 * Filter Dialog Class
 *		1998/05/28 (C)Copyright T.Kazawa(Digitune)
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
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

public class FilterDlg extends Dialog {
	ResourceBundle rc
		= ResourceBundle.getBundle("jp.or.din.digitune.morning.resource");
	Account ac = new Account();
	AccountManager am;
	
	SolidPanel p1;
	TableList filters;
	TextField target, header;
	Choice folders;
	String[] titles = { rc.getString("targetLabel")
		, rc.getString("headerLabel"), rc.getString("folderLabel") };
	
	public FilterDlg(Frame p) {
		super(p, "", true);
		createWindow();
	}
	
	public FilterDlg(Frame p, Account a) {
		super(p, "", true);
		ac = a;
		createWindow();
		loadFilters();
	}
	
	private void loadFilters() {
		am = new AccountManager(ac);
		am.load();
		String[] tmpstrs = ac.getFolders();
		for (int i = 0; i < tmpstrs.length; i++)
			folders.add(tmpstrs[i]);
		String[][] tmpss = am.getFilters();
		for (int i = 0; i < tmpss[0].length; i++) {
			String[] strs = { tmpss[0][i], tmpss[1][i], tmpss[2][i] };
			filters.addItem(strs);
		}
		if (tmpss[0].length > 0)
			filters.select(0);
	}
	
	private void createWindow() {
		setTitle(rc.getString("filterDlgTitle"));
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
		gbc.gridwidth = 3;
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
		int[] widths = { tmpwidth / 3, tmpwidth / 3
			, tmpwidth - (tmpwidth / 3) * 2 };
		filters = new TableList(0, titles, widths);
		filters.setScrollbarThickness(
			Integer.parseInt(rc.getString("scrollbarThickness")));
		p1.add("Center", filters);
		Panel bp1 = new Panel(new FlowLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gblayout.setConstraints(bp1, gbc);
		add(bp1);
		Button add = new Button(rc.getString("addLabel"));
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (!target.getText().trim().equals("")
					&& !header.getText().trim().equals("")) {
					String[] tmpstrs = { target.getText().trim().toLowerCase()
						, header.getText().trim().toLowerCase()
						, folders.getSelectedItem() };
					filters.addItem(tmpstrs);
				}
			}
		});
		bp1.add(add);
		Button remove = new Button(rc.getString("removeLabel"));
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int index = filters.getSelectedIndex();
				if (index < 0)
					return;
				String[] tmpstrs = filters.getRowItems(index);
				target.setText(tmpstrs[0]);
				header.setText(tmpstrs[1]);
				filters.remove(index);
				if (index > 0 || filters.getItemCount() > 0)
					filters.select((index < filters.getItemCount())
						? index : index - 1);
			}
		});
		bp1.add(remove);
		Button up = new Button(rc.getString("upLabel"));
		up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int index = filters.getSelectedIndex();
				if (index < 0)
					return;
				String[] tmpstrs = filters.getRowItems(index);
				filters.remove(index);
				index = ((index - 1) > 0) ? index - 1:0;
				filters.addItem(tmpstrs, index);
				filters.select(index);
			}
		});
		bp1.add(up);
		Button down = new Button(rc.getString("downLabel"));
		down.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int index = filters.getSelectedIndex();
				if (index < 0)
					return;
				String[] tmpstrs = filters.getRowItems(index);
				filters.remove(index);
				index = ((index + 1) <= filters.getItemCount())
					? index + 1 : index;
				filters.addItem(tmpstrs, index);
				filters.select(index);
			}
		});
		bp1.add(down);
		target = new TextField(16);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(target, gbc);
		add(target);
		header = new TextField(16);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(header, gbc);
		add(header);
		folders = new Choice();
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 5;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(folders, gbc);
		add(folders);
		Panel bp2 = new Panel(new FlowLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gblayout.setConstraints(bp2, gbc);
		add(bp2);
		Button ok = new Button(rc.getString("okLabel"));
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String[][] tmpfilters = new String[3][filters.getItemCount()];
				for (int i = 0; i < filters.getItemCount(); i++) {
					String[] tmpstrs = filters.getRowItems(i);
					for (int j = 0; j < 3; j++)
						tmpfilters[j][i] = tmpstrs[j];
				}
				am.setFilters(tmpfilters);
				am.save();
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
		pack();
		Dimension scsize = getToolkit().getScreenSize();
		Dimension size = getSize();
		setLocation(scsize.width / 2 - size.width / 2
			, scsize.height / 2 - size.height / 2);
	}
	
	public static final void main(String[] args) {
		Frame p = new Frame();
		FilterDlg ad = new FilterDlg(p);
		ad.setVisible(true);
	}
}
