/*
 * Progress Box class
 *		1998/03/17 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.gui;

import jp.kyasu.awt.Frame;
import jp.kyasu.awt.Label;
import jp.kyasu.awt.Button;
import jp.kyasu.awt.Panel;
import jp.kyasu.awt.BorderedPanel;
import jp.kyasu.graphics.VTitledPaneBorder;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Enumeration;
import java.util.Vector;

public class ProgressBox extends MsgBox implements Runnable {
	Vector procs = new Vector();
	Panel pbody;
	Thread thread = null;
	
	public ProgressBox(Frame ap, String at, boolean am) {
		super(ap, at, "", am);
	}
	
	protected void createButton() {
		Button cancel = new Button(rc.getString("cancelLabel"));
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				for (Enumeration en = procs.elements()
					; en.hasMoreElements(); )
					((ProgressProcess) en.nextElement()).interrupt();
			}
		});
		regbutton.add(cancel, "Center");
	}
	
	protected void createBody(String dm) {
		pbody = new Panel(new GridLayout(0, 1, 0, 0));
		add(pbody, "Center");
	}
	
	protected void windowClosing(WindowEvent we) {
		for (Enumeration en = procs.elements(); en.hasMoreElements(); )
			((ProgressProcess) en.nextElement()).stop();
		setVisible(false);
		dispose();
	}
	
	public void addProgressProcess(ProgressProcess app) {
		procs.addElement(app);
		BorderedPanel p1
			= new BorderedPanel(new VTitledPaneBorder(app.getTitle()));
		p1.setLayout(new GridLayout(0, 1, 0, 0));
		pbody.add(p1);
		final Label l1 = new Label();
		p1.add(l1);
		final Gauge g1 = new Gauge();
		p1.add(g1);
		app.addVariableListener(new VariableListener() {
			public void valueChanged(VariableEvent ve) {
				l1.setText(ve.getStatus());
				g1.setMaxValue(ve.getMaxValue());
				g1.setCurrentValue(ve.getCurrentValue());
			}
		});
	}
	
	public void setVisible(boolean t) {
		if (t) {
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException ex) {}
			packnmove();
			thread = new Thread(ProgressBox.this);
			thread.start();
		}
		super.setVisible(t);
	}
	
	public void run() {
		try {
			thread.sleep(500);
		} catch (InterruptedException ex) {}
		for (Enumeration en = procs.elements(); en.hasMoreElements(); )
			((ProgressProcess) en.nextElement()).start();
		try {
			for (Enumeration en = procs.elements(); en.hasMoreElements(); )
				((ProgressProcess) en.nextElement()).join();
		} catch (InterruptedException ex) {}
		try {
			thread.sleep(1000);
		} catch (InterruptedException ex) {}
		setVisible(false);
		dispose();
	}
}
