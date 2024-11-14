/*
 * Import Process class
 *		1999/05/09 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.gui.ProgressProcess;
import jp.or.din.digitune.gui.VariableEvent;
import jp.or.din.digitune.gui.VariableListener;
import jp.or.din.digitune.io.LineBufferedInputStream;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class ImportProcess extends ProgressProcess {
	static final int BUFSIZE = 8192;
	LocalResource rc = LocalResource.getResource();
	
	Account account = null;
	File importfile = null;
	String folder = "";
	long offset = 0, max = 0;
	
	public ImportProcess(Account aa, File af, String as) {
		account = aa;
		importfile = af;
		folder = as;
		max = importfile.length();
	}
	
	public String getTitle() {
		return rc.getString("importStr");
	}
	
	stateObject currentstate;
	
	public void setCurrentState(stateObject as) {
		currentstate = as;
	}
	
	private interface stateObject {
		public boolean process(ImportProcess p);
	}
	
	private class importState implements stateObject {
		public boolean process(ImportProcess p) {
			File mboxfile = new File(account.getName() + ".mbx");
			long tmpoffset = 0, tmpsize = 0;
			byte[] buf, tmpbuf = new byte[BUFSIZE];
			String tmpstr;
			AccountManager am = new AccountManager(account);
			am.load();
			VariableEvent ve = new VariableEvent(ImportProcess.this, "", 0, 0);
			try {
				byte[] emark = ".\r\n".getBytes("8859_1");
				offset = 0;
				LineBufferedInputStream bin = new LineBufferedInputStream(
					new FileInputStream(importfile));
				do {
					buf = bin.readLine();
					if (buf == null)
						break;
					offset += buf.length;
				} while (buf.length < 5 || buf[0] != 'F' || buf[1] != 'r'
					|| buf[2] != 'o' || buf[3] != 'm' || buf[4] != ' ');
				bin.close();
				while (buf != null && !isInterrupted()) {
					long size = 0;
					bin = new LineBufferedInputStream(
						new FileInputStream(importfile));
					bin.skip(offset);
					do {
						buf = bin.readLine();
						if (buf == null)
							break;
						size += buf.length;
					} while (buf.length < 5 || buf[0] != 'F' || buf[1] != 'r'
						|| buf[2] != 'o' || buf[3] != 'm' || buf[4] != ' ');
					bin.close();
					bin = new LineBufferedInputStream(
						new FileInputStream(importfile));
					bin.skip(offset);
					offset += size;
					if (buf != null)
						size -= buf.length;
					tmpoffset = mboxfile.length();
					BufferedOutputStream bout = new BufferedOutputStream(
						new FileOutputStream(mboxfile.getAbsolutePath()
						, true));
					int r = 0, allsize = 0;
					while (allsize < size && !isInterrupted()) {
						r = bin.read(tmpbuf, 0
							, (size - allsize < tmpbuf.length)
							? (int) size - allsize : tmpbuf.length);
						bout.write(tmpbuf, 0, r);
						allsize += r;
						ve.setStatus(rc.getString("writingStr"));
						ve.setMaxValue((int) max);
						ve.setCurrentValue((int) (offset - size + allsize));
						invokeListener(ve);
					}
					bin.close();
					bout.write(emark);
					bout.close();
					tmpsize = mboxfile.length() - tmpoffset;
					Hashtable headers;
					BufferedInputStream tmpin = new BufferedInputStream(
						new FileInputStream(mboxfile));
					tmpin.skip(tmpoffset);
					headers = Message.getHeaders(tmpin);
					tmpin.close();
					if (new Boolean(rc.getString("autoFiltering"))
						.booleanValue())
						am.writeInfo(headers, tmpoffset, tmpsize);
					else
						am.writeInfo(folder, headers, tmpoffset, tmpsize);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				ve.setStatus(rc.getString("fileError"));
				ve.setMaxValue(0);
				ve.setCurrentValue(1);
				invokeListener(ve);
				return false;
			}
			ve.setStatus(rc.getString("doneStr"));
			ve.setMaxValue(0);
			ve.setCurrentValue(1);
			invokeListener(ve);
			return false;
		}
	}
	
	public void run() {
		currentstate = new importState();
		boolean f = true;
		while (!isInterrupted() && f)
			f = currentstate.process(this);
		return;
	}
}
