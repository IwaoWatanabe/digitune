/*
 * Defrag Process class
 *		1998/05/20 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.gui.ProgressProcess;
import jp.or.din.digitune.gui.VariableEvent;
import jp.or.din.digitune.gui.VariableListener;
import java.io.File;
import java.util.ResourceBundle;

public class DefragFolderProcess extends ProgressProcess {
	Account ac;
	String folder;
	ResourceBundle rc
		= ResourceBundle.getBundle("jp.or.din.digitune.morning.resource");
	
	public DefragFolderProcess(Account a, String af) {
		ac = a;
		folder = af;
	}
	
	public String getTitle() {
		return ac.getName();
	}
	
	public void run() {
		FolderManager fm
			= new FolderManager(ac, folder);
		FolderManager newfm
			= new FolderManager(ac, folder + ".new");
		int tmpcount = fm.getMessageCount();
		for (int i = 0; i < tmpcount && !isInterrupted(); i++) {
			VariableEvent ve = new VariableEvent(this,
				rc.getString("processStateStr"), tmpcount, i + 1);
			invokeListener(ve);
			MessageInfo tmpmi = fm.getMessageInfo(i);
			if (tmpmi.getDeleteFlag())
				continue;
			newfm.writeMessageInfo(tmpmi, newfm.getMessageCount());
		}
		fm.close();
		newfm.close();
		if (isInterrupted()) {
			new File(ac.getName() + "." + folder + ".new.idx").delete();
		} else {
			new File(ac.getName() + "." + folder + ".idx").delete();
			new File(ac.getName() + "." + folder + ".new.idx")
				.renameTo(new File(ac.getName() + "." + folder + ".idx"));
		}
	}
}
