/*
 * Variable Event class
 *		1998/03/16 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.gui;

import java.util.EventObject;

public class VariableEvent extends EventObject {
	String status = "";
	int max = 0, current = 0;
	
	public VariableEvent(Object asrc, String astr, int amax, int acurrent) {
		super(asrc);
		status = astr;
		max = amax;
		current = acurrent;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String as) {
		status = as;
	}
	
	public int getMaxValue() {
		return max;
	}
	
	public void setMaxValue(int m) {
		max = m;
	}
	
	public int getCurrentValue() {
		return current;
	}
	
	public void setCurrentValue(int c) {
		current = c;
	}
}
