/*
 * Progress Process Interface
 *		1998/03/17 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.gui;

import java.util.Vector;

public abstract class ProgressProcess extends Thread {
	protected Vector listeners = new Vector();

	public abstract String getTitle();

	public synchronized void addVariableListener(VariableListener listener) {
		listeners.addElement(listener);
	}

	public synchronized void removeVariableListener(VariableListener listener){
		listeners.removeElement(listener);
	}

    public synchronized void invokeListener(VariableEvent ve) {
		for (int i = 0; i < listeners.size(); i++)
			((VariableListener) listeners.elementAt(i)).valueChanged(ve);
	}
}
