/*
 * Sequence Progress Process class
 *		1998/05/27 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.gui;

import jp.or.din.digitune.gui.ProgressProcess;
import jp.or.din.digitune.gui.VariableEvent;
import jp.or.din.digitune.gui.VariableListener;
import java.util.Vector;
import java.util.Enumeration;

public class SequenceProgressProcess extends ProgressProcess
	implements VariableListener {
	Vector processes = new Vector();
	String currenttitle = "";
	
	public SequenceProgressProcess() {
	}
	
	public String getTitle() {
		return currenttitle;
	}
	
	public void valueChanged(VariableEvent ve) {
		if (ve.getCurrentValue() >= ve.getMaxValue()) {
			ve.setCurrentValue(-ve.getCurrentValue());
			ve.setMaxValue(-ve.getMaxValue());
		}
		invokeListener(ve);
	}
	
	public void addProgressProcess(ProgressProcess pp) {
		if (currenttitle.equals(""))
			currenttitle = pp.getTitle();
		processes.addElement(pp);
		pp.addVariableListener(this);
	}
	
	ProgressProcess currentproc = null;
	
	public void interrupt() {
		super.interrupt();
		currentproc.interrupt();
	}
	
	public void run() {
		for (int i = 0; i < processes.size() && !isInterrupted(); i++) {
			currentproc = (ProgressProcess) processes.elementAt(i);
			currenttitle = currentproc.getTitle();
			currentproc.start();
			try {
				((Thread) processes.elementAt(i)).join();
			} catch (InterruptedException ex) {}
		}
		return;
	}
}
