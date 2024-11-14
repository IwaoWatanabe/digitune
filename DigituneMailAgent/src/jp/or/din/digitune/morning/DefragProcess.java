/*
 * Defrag Process class
 *		1998/05/20 (C)Copyright T.Kazawa(Digitune)
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
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.ResourceBundle;

public class DefragProcess extends ProgressProcess {
	static final int BUFSIZE = 8192;
	
	Account ac;
	ResourceBundle rc
		= ResourceBundle.getBundle("jp.or.din.digitune.morning.resource");
	
	public DefragProcess(Account a) {
		ac = a;
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
	
	private interface stateObject {
		public boolean process(DefragProcess p);
	}
	
	private class setupState implements stateObject {
		public boolean process(DefragProcess p) {
			VariableEvent ve = new VariableEvent(DefragProcess.this
				, rc.getString("startStateStr"), 1, 0);
			invokeListener(ve);
			folders = ac.getFolders();
			for (int i = 0; i < folders.length; i++) {
				if (folders[i].equals(rc.getString("trash")))
					continue;
				FolderManager fm = new FolderManager(ac, folders[i]);
				max += fm.getMessageCount();
				fm.close();
			}
			p.setCurrentState(new processState());
			return true;
		}
	}
	
	private class processState implements stateObject {
		public boolean process(DefragProcess p) {
			int i, j;
			for (i = 0; i < folders.length && !isInterrupted(); i++) {
				if (folders[i].equals(rc.getString("trash")))
					continue;
				FolderManager fm = new FolderManager(ac, folders[i]);
				FolderManager newfm
					= new FolderManager(ac, folders[i] + ".new");
				int tmpcount = fm.getMessageCount();
				String processstr = rc.getString("processStateStr");
				VariableEvent ve = new VariableEvent(DefragProcess.this
					, "", 0, 0);
				for (j = 0; j < tmpcount && !isInterrupted(); j++) {
					ve.setStatus(processstr + (current + j + 1) + "/" + max);
					ve.setMaxValue(max);
					ve.setCurrentValue(current + j + 1);
					invokeListener(ve);
					MessageInfo mi = fm.getMessageInfo(j);
					if (mi.getDeleteFlag())
						continue;
					byte[] tmpbuf = new byte[BUFSIZE];
					try {
						long tmpoffset
							= new File(ac.getName() + ".new.mbx").length();
						BufferedOutputStream bout = new BufferedOutputStream(
							new FileOutputStream(ac.getName() + ".new.mbx"
							, true));
						BufferedInputStream bin = new BufferedInputStream(
							new FileInputStream(ac.getName() + ".mbx"));
						bin.skip(mi.getOffset());
						int tmpsize = 0, mailsize = (int) mi.getSize();
						while (tmpsize < mailsize) {
							int s = bin.read(tmpbuf, 0
								, ((mailsize - tmpsize) > BUFSIZE)
								? BUFSIZE : mailsize - tmpsize);
							bout.write(tmpbuf, 0, s);
							tmpsize += s;
						}
						bin.close();
						bout.flush();
						bout.close();
						mi.setOffset(tmpoffset);
						mi.setSize(mailsize);
						newfm.writeMessageInfo(mi, newfm.getMessageCount());
					} catch (IOException ex) {
						ex.printStackTrace();
						ve = new VariableEvent(DefragProcess.this
							, rc.getString("writeErrorStr") + current + j + 1
							, 0, 1);
						invokeListener(ve);
						return false;
					}
				}
				current += tmpcount;
				fm.close();
				newfm.close();
			}
			if (isInterrupted()) {
				for (int s = 0; s < i; s++)
					new File(ac.getName()+"."+folders[s]+".new.idx").delete();
				new File(ac.getName() + ".new.mbx").delete();
				VariableEvent ve = new VariableEvent(DefragProcess.this
					, rc.getString("doneStr"), 0, 1);
				invokeListener(ve);
				return false;
			} else {
				p.setCurrentState(new quitState());
				return true;
			}
		}
	}
	
	private class quitState implements stateObject {
		public boolean process(DefragProcess p) {
			Hashtable hfolder = new Hashtable();
			for (int i = 0; i < folders.length; i++) {
				String tmpfilename = ac.getName() + "." + folders[i] + ".idx";
				new File(tmpfilename).delete();
				if (folders[i].equals(rc.getString("trash")))
					continue;
				new File(ac.getName() + "." + folders[i] + ".new.idx")
					.renameTo(new File(tmpfilename));
				hfolder.put(tmpfilename,hfolder);
			}
			new File(ac.getName() + ".mbx").delete();
			new File(ac.getName() + ".new.mbx")
				.renameTo(new File(ac.getName() + ".mbx"));
			String[] files = new File(
				new File(
				new File("dummy").getAbsolutePath()).getParent()).list();
			for (int i = 0; i < files.length; i++) {
				if (hfolder.get(files[i]) != null)
					continue;
				if (files[i].endsWith("eml"))
					new File(files[i]).delete();
				else if (files[i].startsWith(ac.getName())
					&& files[i].endsWith("idx"))
					new File(files[i]).delete();
			}
			VariableEvent ve = new VariableEvent(DefragProcess.this
				, rc.getString("doneStr"), 0, 1);
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
