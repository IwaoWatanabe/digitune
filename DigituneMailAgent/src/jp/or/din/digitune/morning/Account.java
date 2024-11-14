/*
 * Account class
 *		1998/03/15 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.util.Codecs;
import jp.or.din.digitune.util.CSV;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.MissingResourceException;

public class Account {
	String name = "";
	String user = "", address = "";
	String id = "", pass = "", smtpserver = "", popserver = "";
	boolean deletestate = false;
	String lastUID = "xxx";
	Vector folders = new Vector();
	LocalResource rc = LocalResource.getResource();
	
	public Account(String aname, String aid, String apass, String asmtp
		, String apop, boolean adel, String auid, String aaddr, String auser) {
		name = aname;
		id = aid;
		pass = apass;
		smtpserver = asmtp;
		popserver = apop;
		deletestate = adel;
		lastUID = auid;
		address = aaddr;
		user = auser;

		String[] tmpstrs = CSV.cut(rc.getString("defaultFolders"));
		for (int i = 0; i < tmpstrs.length; i++)
			folders.addElement(tmpstrs[i]);
	}
	
	public Account(String aline) {
		String[] strs = CSV.cut(aline);
		name = strs[0];
		id = strs[1];
		pass = Codecs.base64Decode(strs[2]);
		smtpserver = strs[3];
		popserver = strs[4];
		deletestate = new Boolean(strs[5]).booleanValue();
		if (strs[6].startsWith("base64:"))
			lastUID = Codecs.base64Decode(strs[6].substring(7));
		else
			lastUID = strs[6];
		address = strs[7];
		user = strs[8];

		for (int i = 9; i < strs.length; i++)
			folders.addElement(strs[i]);

		if (folders.size() == 0) { // temp
			String[] tmpstrs;
			try {
				tmpstrs = CSV.cut(rc.getString(name + "Folders"));
			} catch (MissingResourceException ex) {
				tmpstrs = CSV.cut(rc.getString("defaultFolders"));
			}
			for (int i = 0; i < tmpstrs.length; i++)
				folders.addElement(tmpstrs[i]);
		}
	}
	
	public Account() {
		String[] tmpstrs = CSV.cut(rc.getString("defaultFolders"));
		for (int i = 0; i < tmpstrs.length; i++)
			folders.addElement(tmpstrs[i]);
	}
	
	public void setName(String aname) {
		name = aname;
	}
	
	public String getName() {
		return name;
	}
	
	public void setID(String aid) {
		id = aid;
	}
	
	public String getID() {
		return id;
	}
	
	public void setPassword(String apass) {
		pass = apass;
	}
	
	public String getPassword() {
		return pass;
	}
	
	public void setSMTPServer(String asmtp) {
		smtpserver = asmtp;
	}
	
	public String getSMTPServer() {
		return smtpserver;
	}
	
	public void setPOPServer(String apop) {
		popserver = apop;
	}
	
	public String getPOPServer() {
		return popserver;
	}
	
	public void setDeleteState(boolean a) {
		deletestate = a;
	}
	
	public boolean getDeleteState() {
		return deletestate;
	}
	
	public void setLastUID(String a) {
		lastUID = a;
	}
	
	public String getLastUID() {
		return lastUID;
	}
	
	public void setUsername(String auser) {
		user = auser;
	}
	
	public String getUsername() {
		return user;
	}
	
	public void setAddress(String aaddr) {
		address = aaddr;
	}
	
	public String getAddress() {
		return address;
	}

	public void addFolder(String a) {
		folders.addElement(a.trim());
	}

	public void removeFolder(String a) {
		folders.removeElement(a);
	}

	public void upFolder(String a) {
		int i = folders.indexOf(a);
		if (i > 0) {
			folders.removeElementAt(i);
			folders.insertElementAt(a, i - 1);
		}
	}

	public void downFolder(String a) {
		int i = folders.indexOf(a);
		if (i < folders.size() - 1) {
			folders.removeElementAt(i);
			folders.insertElementAt(a, i + 1);
		}
	}

	public void setFolders(String[] as) {
		folders = new Vector();
		for (int i = 0; i < as.length; i++)
			folders.addElement(as[i].trim());
	}

	public String[] getFolders() {
		String[] tmpstrs = new String[folders.size()];
		folders.copyInto(tmpstrs);
		return tmpstrs;
	}
	
	public String toString() {
		String[] strs = new String[9 + folders.size()];
		strs[0] = name;
		strs[1] = id;
		strs[2] = Codecs.base64Encode(pass);
		strs[3] = smtpserver;
		strs[4] = popserver;
		strs[5] = new Boolean(deletestate).toString();
		strs[6] = "base64:" + Codecs.base64Encode(lastUID);
		strs[7] = address;
		strs[8] = user;
		for (int i = 9; i < strs.length; i++)
			strs[i] = (String) folders.elementAt(i - 9);

		return CSV.toStringWithQuote(strs);
	}
}
