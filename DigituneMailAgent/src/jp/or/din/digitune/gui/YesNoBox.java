/*
 *	Yes/No Dialog Box
 *		1997/10/21 (C) Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.gui;

import jp.kyasu.awt.Frame;
import jp.kyasu.awt.Label;
import jp.kyasu.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class YesNoBox extends MsgBox {
	private Label label1;
	private Button yes, no;
	private boolean state = false;

	public YesNoBox(Frame parent, String title, String msg) {
		super(parent, title, msg, true);
	}

	protected void createButton() {
		yes = new Button(rc.getString("yesLabel"));
		no = new Button(rc.getString("noLabel"));
		yes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				state = true;
				setVisible(false);
				dispose();
			}
		});
		no.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				state = false;
				setVisible(false);
				dispose();
			}
		});
		regbutton.add(yes, "Center");
		regbutton.add(no, "Center");
	}

	public synchronized boolean getState() {
		return state;
	}
}
