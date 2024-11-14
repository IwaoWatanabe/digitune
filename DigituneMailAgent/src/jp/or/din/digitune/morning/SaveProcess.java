/*
 * Save Process class
 *		1998/05/25 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.gui.ProgressProcess;
import jp.or.din.digitune.gui.VariableEvent;
import jp.or.din.digitune.gui.VariableListener;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class SaveProcess extends ProgressProcess {
	static final int BUFSIZE = 8192;
	ResourceBundle rc
		= ResourceBundle.getBundle("jp.or.din.digitune.morning.resource");
	
	File dst = null, src = null;
	long offset = -1, size = -1;
	
	public SaveProcess(File af1, File af2, long aoff, long asize) {
		src = af1;
		dst = af2;
		offset = aoff;
		size = asize - 3; // Ignored '.' + CR + LF.
	}
	
	public String getTitle() {
		return rc.getString("saveStr");
	}
	
	stateObject currentstate;
	
	public void setCurrentState(stateObject as) {
		currentstate = as;
	}
	
	private interface stateObject {
		public boolean process(SaveProcess p);
	}
	
	private class saveState implements stateObject {
		public boolean process(SaveProcess p) {
			try {
				BufferedInputStream bin
					= new BufferedInputStream(new FileInputStream(src));
				BufferedOutputStream bout
					= new BufferedOutputStream(new FileOutputStream(
					dst.getAbsolutePath(), true));
				bin.skip(offset);
				int tmpsize = 0, allsize = 0;
				byte[] tmpbuf = new byte[8192];
				while (allsize < size && !isInterrupted()) {
					tmpsize = bin.read(tmpbuf, 0
						, (size - allsize < tmpbuf.length) ? (int) size-allsize
						: tmpbuf.length);
					bout.write(tmpbuf, 0, tmpsize);
					allsize += tmpsize;
					VariableEvent ve = new VariableEvent(SaveProcess.this
						, rc.getString("writingStr"), (int) size, allsize);
					invokeListener(ve);
				}
				bin.close();
				bout.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				VariableEvent ve = new VariableEvent(SaveProcess.this
					, rc.getString("fileError"), 0, 1);
				invokeListener(ve);
				return false;
			}
			VariableEvent ve = new VariableEvent(SaveProcess.this
				, rc.getString("doneStr"), 0, 1);
			invokeListener(ve);
			return false;
		}
	}
	
	public void run() {
		currentstate = new saveState();
		boolean f = true;
		while (!isInterrupted() && f)
			f = currentstate.process(this);
		return;
	}
}
