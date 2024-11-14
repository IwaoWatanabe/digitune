/*
 * Search Process class
 *		1999/03/03 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import gnu.regexp.RE;
import gnu.regexp.REException;
import jp.or.din.digitune.gui.ProgressProcess;
import jp.or.din.digitune.gui.VariableEvent;
import jp.or.din.digitune.gui.VariableListener;
import jp.or.din.digitune.io.GetLine;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.ResourceBundle;

public class SearchProcess extends ProgressProcess {
	static final int BUFSIZE = 65536;
	
	Account ac;
	String searchstr;
	boolean ignorecase = false, regexp = false;
	ResourceBundle rc
		= ResourceBundle.getBundle("jp.or.din.digitune.morning.resource");
	VariableEvent ve;
	RE re = null;
	
	public SearchProcess(Account a, String as, boolean aig, boolean arg) {
		ac = a;
		searchstr = as;
		ignorecase = aig;
		regexp = arg;
		ve = new VariableEvent(this, "", 0, 0);
	}

	public SearchProcess(Account a, String as, String acf, boolean aig
		, boolean arg) {
		ac = a;
		searchstr = as;
		folders = new String[1];
		folders[0] = acf;
		ignorecase = aig;
		regexp = arg;
		ve = new VariableEvent(this, "", 0, 0);
	}
	
	public String getTitle() {
		return ac.getName();
	}
	
	stateObject currentstate;
	String[] folders = null;
	int current = 0, max = 0;
	
	public void setCurrentState(stateObject as) {
		currentstate = as;
	}

	private interface searchMethod {
		public boolean isMatch(String s);
	}

	private class NullSearch implements searchMethod {
		private String key = "";

		public NullSearch(String a) {
			key = a;
		}

		public boolean isMatch(String astr) {
			return (astr.indexOf(key) >= 0);
		}
	}

	private class NullIgnoreCaseSearch implements searchMethod {
		private String key = "";

		public NullIgnoreCaseSearch(String a) {
			key = a.toLowerCase();
		}

		public boolean isMatch(String astr) {
			return (astr.toLowerCase().indexOf(key) >= 0);
		}
	}

	private class RegexpSearch implements searchMethod {
		private RE re = null;

		public RegexpSearch(String a, boolean ic) throws REException {
			if (ic)
				re = new RE(a, RE.REG_ICASE);
			else
				re = new RE(a);
		}

		public boolean isMatch(String astr) {
			return re.isMatch(astr);
		}
	}
	
	private interface stateObject {
		public boolean process(SearchProcess p);
	}
	
	private class setupState implements stateObject {
		public boolean process(SearchProcess p) {
			ve.setStatus(rc.getString("startStateStr"));
			ve.setMaxValue(1);
			ve.setCurrentValue(0);
			invokeListener(ve);
			if (folders == null)
				folders = ac.getFolders();
			for (int i = 0; i < folders.length; i++) {
				FolderManager fm = new FolderManager(ac, folders[i]);
				max += fm.getMessageCount();
				fm.close();
			}
			p.setCurrentState(new processState());
			return true;
		}
	}
	
	private class processState implements stateObject {
		public boolean process(SearchProcess p) {
			int c = 0;
			FolderManager searchfm
				= new FolderManager(ac, rc.getString("search"));
			searchMethod sm = null;
			if (regexp)
				try {
					sm = new RegexpSearch(searchstr, ignorecase);
				} catch (Exception ex) {}
			if (sm == null) {
				if (ignorecase)
					sm = new NullIgnoreCaseSearch(searchstr);
				else
					sm = new NullSearch(searchstr);
			}
			for (int i = 0; i < folders.length && !isInterrupted(); i++) {
				FolderManager fm = new FolderManager(ac, folders[i]);
				int tmpcount = fm.getMessageCount();
				for (int j = 0; j < tmpcount && !isInterrupted(); j++) {
					ve.setStatus(rc.getString("processStateStr") + c
						+ rc.getString("foundStr"));
					ve.setMaxValue(max);
					ve.setCurrentValue(current + j + 1);
					invokeListener(ve);
					MessageInfo mi = fm.getMessageInfo(j);
					if (mi.getDeleteFlag())
						continue;
					byte[] tmpbuf;
					boolean found = false;
					try {
						BufferedInputStream bin = new BufferedInputStream(
						new FileInputStream(ac.getName() + ".mbx"));
						GetLine gl = new GetLine(bin, mi.getOffset()
							, (int) mi.getSize());
						while ((tmpbuf = gl.getLine()) != null) {
							// Search String
							String tmpstr = new String(tmpbuf, 0
								, (tmpbuf.length > 2) ? tmpbuf.length - 2
								: tmpbuf.length
								, rc.getString("searchEncode"));
							if (sm.isMatch(tmpstr)) {
								c++;
								found = true;
								break;
							}
						}
						bin.close();
						if (found)
							searchfm.writeMessageInfo(mi
							, searchfm.getMessageCount());
					} catch (IOException ex) {
						ex.printStackTrace();
						ve.setStatus(rc.getString("writeErrorStr")
							+ current + j + 1);
						ve.setMaxValue(0);
						ve.setCurrentValue(1);
						invokeListener(ve);
						return false;
					}
				}
				current += tmpcount;
				fm.close();
			}
			searchfm.close();
			ve.setStatus(rc.getString("doneStr"));
			ve.setMaxValue(0);
			ve.setCurrentValue(1);
			invokeListener(ve);
			return false;
		}
	}
	
	public void run() {
		currentstate = new setupState();
		boolean f = true;
		while (!isInterrupted() && f)
			f = currentstate.process(this);
		return;
	}
}
