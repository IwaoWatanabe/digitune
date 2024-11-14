/*
 * Folder Management class
 *		1998/03/15 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import java.io.RandomAccessFile;
import java.io.IOException;

public class FolderManager {
	RandomAccessFile db = null;
	
	public FolderManager(Account a, String af) {
		try {
			db = new RandomAccessFile(a.getName() + "." + af + ".idx", "rw");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void close() {
		try {
			db.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public int getMessageCount() {
		try {
			return (int) (db.length() / MessageInfo.length());
		} catch (IOException ex) {
			ex.printStackTrace();
			return 0;
		}
	}
	
	public MessageInfo getMessageInfo(int index) {
		return new MessageInfo(db, index);
	}
	
	public void writeMessageInfo(MessageInfo ami, int index) {
		try {
			ami.writeMessageInfo(db, index);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
