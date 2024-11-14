/*
 * Variable Listener class
 *		1998/03/16 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.gui;

import java.util.EventListener;

public interface VariableListener extends EventListener {
	public void valueChanged(VariableEvent ve);
}
