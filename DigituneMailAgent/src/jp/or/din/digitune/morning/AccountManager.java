/*
 * Account Management class
 *		1998/03/15 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.util.CSV;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Hashtable;
import java.util.MissingResourceException;

public class AccountManager {
	LocalResource rc = LocalResource.getResource();
	Account ac;
	Vector folders = new Vector();
	Vector filkeys = new Vector(), filheaders = new Vector()
		, filto = new Vector();
	
	public AccountManager(Account a) {
		ac = a;
	}
	
	public void load() {
		String[] tmpfolders = ac.getFolders();
		for (int i = 0; i < tmpfolders.length; i++)
			folders.addElement(tmpfolders[i]);
		if (folders.size() == 0) //temp
			try {
				tmpfolders = CSV.cut(rc.getString(ac.getName() + "Folders"));
				for (int i = 0; i < tmpfolders.length; i++)
					if (!tmpfolders[i].trim().equals(""))
						folders.addElement(tmpfolders[i].trim());
			} catch (MissingResourceException ex) {}
/* unuse code ...
			try {
				BufferedReader reader
					= new BufferedReader(new FileReader(ac.getName()+".fld"));
				String tmpstr;
				while ((tmpstr = reader.readLine()) != null)
					if (!tmpstr.trim().equals(""))
						folders.addElement(tmpstr.trim());
				reader.close();
			} catch (FileNotFoundException ex) {
				tmpfolders = CSV.cut(rc.getString("defaultFolders"));
				for (int i = 0; i < tmpfolders.length; i++)
					if (!tmpfolders[i].trim().equals(""))
						folders.addElement(tmpfolders[i].trim());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
*/
		try {
			BufferedReader reader
				= new BufferedReader(new FileReader(ac.getName() + ".flt"));
			String tmpstr;
			while ((tmpstr = reader.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(tmpstr, "\t");
				if (st.countTokens() != 3)
					continue;
				filkeys.addElement(st.nextToken());
				filheaders.addElement(st.nextToken());
				filto.addElement(st.nextToken());
			}
			reader.close();
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void save() {
/* move to Account.java
		String[] tmpfolders = new String[folders.size()];
		folders.copyInto(tmpfolders);
		rc.setString(ac.getName() + "Folders"
			, CSV.toStringWithQuote(tmpfolders));
*/
		try {
			PrintWriter writer
				= new PrintWriter(new FileWriter(ac.getName() + ".flt"));
			for (int i = 0; i < filkeys.size(); i++)
				writer.println((String) filkeys.elementAt(i) + "\t"
					+ (String) filheaders.elementAt(i) + "\t"
					+ (String) filto.elementAt(i));
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void addFolder(String a) {
		folders.addElement(a);
	}
	
	public void removeFolder(String a) {
		folders.removeElement(a);
	}
	
	public void setFolders(String[] as) {
		folders = new Vector();
		for (int i = 0; i < as.length; i++)
			folders.addElement(as[i]);
	}
	
	public String[] getFolders() {
		String[] tmpstrs = new String[folders.size()];
		folders.copyInto(tmpstrs);
		return tmpstrs;
	}
	
	public void addFilter(String[] af) {
		if (af.length != 3)
			return;
		filkeys.addElement(af[0].toLowerCase().trim());
		filheaders.addElement(af[1].toLowerCase().trim());
		filto.addElement(af[2].trim());
	}
	
	public void removeFilter(String[] af) {
		if (af.length != 3)
			return;
		filkeys.removeElement(af[0].toLowerCase().trim());
		filheaders.removeElement(af[1].toLowerCase().trim());
		filto.removeElement(af[2].toLowerCase().trim());
	}
	
	public void setFilters(String[][] af) {
		if (af.length != 3)
			return;
		filkeys = new Vector();
		filheaders = new Vector();
		filto = new Vector();
		for (int i = 0; i < af[0].length; i++) {
			filkeys.addElement(af[0][i].toLowerCase().trim());
			filheaders.addElement(af[1][i].toLowerCase().trim());
			filto.addElement(af[2][i].trim());
		}
	}
	
	public String[][] getFilters() {
		String[][] filters = new String[3][filkeys.size()];
		filkeys.copyInto(filters[0]);
		filheaders.copyInto(filters[1]);
		filto.copyInto(filters[2]);
		return filters;
	}

	public String getFilteringFolder(Hashtable headers) {
		String tmpfolder = rc.getString("inbox");
		for (int i = 0; i < filkeys.size(); i++) {
			Vector tmpvec = (Vector) headers.get(filheaders.elementAt(i));
			String tmpstr = null;
			if (tmpvec != null)
				tmpstr = (String) tmpvec.elementAt(0);
			if (tmpstr != null) {
				if (tmpstr.toLowerCase().indexOf((String) filkeys.elementAt(i))
					!= -1) {
					tmpfolder = (String) filto.elementAt(i);
					break;
				}
			}
		}
		return tmpfolder;
	}

	private String getParentId(Hashtable headers) {
		if (headers.get("in-reply-to") != null) {
			String tmpstr =
				((String) ((Vector) headers.get("in-reply-to")).elementAt(0));
			int begin = tmpstr.indexOf("<");
			int end = tmpstr.indexOf(">");
			if (begin >= 0 && end >= 0 && begin < end)
				return tmpstr.substring(begin, end + 1);
		}
		if (headers.get("references") != null) {
			String tmpstr =
				((String) ((Vector) headers.get("references")).elementAt(0));
			int begin = tmpstr.lastIndexOf("<");
			int end = tmpstr.lastIndexOf(">");
			if (begin >= 0 && end >= 0 && begin < end)
				return tmpstr.substring(begin, end + 1);
		}
		return "";
	}
	
	public void writeInfo(String folder, Hashtable headers, long offset
		, long size) throws IOException {
		MessageInfo mi = new MessageInfo();
		if (headers.get("subject") != null)
			mi.setSubject((String) ((Vector) headers.get("subject"))
				.elementAt(0));
		if (headers.get("from") != null)
			mi.setFrom((String) ((Vector) headers.get("from")).elementAt(0));
		if (headers.get("date") != null)
			mi.setDate((String) ((Vector) headers.get("date")).elementAt(0));
		mi.setOffset(offset);
		mi.setSize(size);
		if (headers.get("message-id") != null)
			mi.setMessageId(
				(String) ((Vector) headers.get("message-id")).elementAt(0));
		mi.setParentId(getParentId(headers));
		FileOutputStream fout
			= new FileOutputStream(ac.getName() + "." + folder + ".idx", true);
		mi.writeMessageInfo(fout);
		fout.close();
	}
	
	public void writeInfo(Hashtable headers, long offset, long size) 
		throws IOException {
		writeInfo(getFilteringFolder(headers), headers, offset, size);
	}
}
