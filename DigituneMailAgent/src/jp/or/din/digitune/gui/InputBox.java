/*
 * Input Box Class
 *		1998/04/30 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.gui;

import org.ingrid.kazama.Cp932;
import jp.kyasu.awt.Button;
import jp.kyasu.awt.Checkbox;
import jp.kyasu.awt.Dialog;
import jp.kyasu.awt.Frame;
import jp.kyasu.awt.Label;
import jp.kyasu.awt.Panel;
import jp.kyasu.awt.TextField;
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

public class InputBox extends Dialog {
	ResourceBundle rc
		= ResourceBundle.getBundle("jp.or.din.digitune.gui.resources");
	String msg = "", value = "";
	boolean status = false;
	
	public InputBox(Frame p, String title, String amsg) {
		super(p, title, true);
		msg = amsg;
		createWindow();
	}
	
	public InputBox(Frame p, String title, String amsg, String a) {
		super(p, title, true);
		msg = amsg;
		value = a;
		createWindow();
	}
	
	public String getString() {
		return value;
	}
	
	public void setStatus(boolean as) {
		status = as;
	}
	
	public boolean getStatus() {
		return status;
	}
	
	private void createWindow() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				setVisible(false);
				dispose();
			}
		});
		GridBagLayout gblayout = new GridBagLayout();
		setLayout(gblayout);
		Label lmsg = new Label(msg, Label.LEFT);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 2, 0, 2);
		gblayout.setConstraints(lmsg, gbc);
		add(lmsg);
		final TextField tvalue = new TextField(30);
		tvalue.setText(Cp932.toCp932(value));
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gblayout.setConstraints(tvalue, gbc);
		add(tvalue);
		Panel p1 = new Panel(new FlowLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gblayout.setConstraints(p1, gbc);
		add(p1);
		Button ok = new Button(rc.getString("okLabel"));
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (tvalue.getText().trim().equals("")) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				value = Cp932.toJIS(tvalue.getText());
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
	
	public static final void main(String[] args) {
		Frame p = new Frame();
		InputBox ib = new InputBox(p, "test", "test");
		ib.setVisible(true);
		System.out.println(ib.getString());
	}
}
