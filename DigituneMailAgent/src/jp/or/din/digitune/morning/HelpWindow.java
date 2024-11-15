/*
 *	Help Window Class
 *		1998/12/25 Copyright (C) T.Kazawa
 */

package jp.or.din.digitune.morning;

import jp.kyasu.awt.Frame;
import jp.kyasu.awt.Button;
import jp.kyasu.editor.HTMLEditor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class HelpWindow extends Frame {
	LocalResource rc = LocalResource.getResource();

	public HelpWindow() {
		super();
		createWindow();
	}

	private void createWindow() {
		setTitle(rc.getString("helpwindowTitle"));
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc;
		setLayout(gbl);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				HelpWindow.this.setVisible(false);
				HelpWindow.this.dispose();
			}
		});

		final HTMLEditor body = new HTMLEditor(false);
		File mf = new File(rc.getString("manualFile"));
		if (mf.exists())
			body.open_file(mf);
		else {
			String url = rc.getString("manualURL");
			System.err.println("Manual URL: " + url);
			body.goto_page(url);
		}
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbl.setConstraints(body, gbc);
		add(body);

		Button quit = new Button(rc.getString("quitLabel"));
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				HelpWindow.this.setVisible(false);
				HelpWindow.this.dispose();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbl.setConstraints(quit, gbc);
		add(quit);

		Button prev = new Button(rc.getString("prevLabel"));
		prev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				body.backward_page();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbl.setConstraints(prev, gbc);
		add(prev);

		Button next = new Button(rc.getString("nextLabel"));
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				body.forward_page();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbl.setConstraints(next, gbc);
		add(next);

		pack();
		Dimension scsize = getToolkit().getScreenSize();
		Dimension size = getSize();
		setLocation(scsize.width / 2 - size.width / 2
			, scsize.height / 2 - size.height / 2);
		body.requestFocus();
	}
}
