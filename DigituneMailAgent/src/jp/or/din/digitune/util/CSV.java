/*
 * Comma Separated Value Utils. class
 *		1998/05/29 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.util;

import java.util.StringTokenizer;
import java.util.Vector;

public class CSV {
	private CSV() {
	}
	
	public static String[] cut(String src) {
		if (src == null || src.equals(""))
			return null;
		Vector tmpvec = new Vector();
		StringBuffer strbuf = new StringBuffer();
		int state = 0;
		for (int i = 0; i < src.length(); i++) {
			switch (state) {
			case 0:
				if (src.charAt(i) == '"') {
					state = 1;
					break;
				} else if (src.charAt(i) == ',') {
					tmpvec.addElement(new String(strbuf).trim());
					strbuf = new StringBuffer();
					break;
				}
				strbuf.append(src.charAt(i));
				break;
			case 1:
				if (src.charAt(i) == '"') {
					state = 0;
					break;
				}
				strbuf.append(src.charAt(i));
				break;
			}
		}
		if (strbuf.length() != 0)
			tmpvec.addElement(new String(strbuf).trim());
		String[] tmpstrs = new String[tmpvec.size()];
		tmpvec.copyInto(tmpstrs);
		return tmpstrs;
	}
	
	public static int[] cutInt(String src) {
		if (src == null || src.equals(""))
			return null;
		String[] tmpstrs = cut(src);
		int[] tmpints = new int[tmpstrs.length];
		for (int i = 0; i < tmpints.length; i++) {
			try {
				tmpints[i] = Integer.parseInt(tmpstrs[i]);
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
				tmpints[i] = 0;
			}
		}
		return tmpints;
	}
	
	public static String toString(String[] tmpstrs) {
		if (tmpstrs == null)
			return "";
		StringBuffer strbuf = new StringBuffer();
		strbuf.append(tmpstrs[0]);
		for (int i = 1; i < tmpstrs.length; i++)
			strbuf.append("," + tmpstrs[i]);
		return new String(strbuf);
	}
	
	public static String toStringWithQuote(String[] tmpstrs) {
		if (tmpstrs == null)
			return "";
		StringBuffer strbuf = new StringBuffer();
		strbuf.append('"' + tmpstrs[0]);
		for (int i = 1; i < tmpstrs.length; i++)
			strbuf.append("\",\"" + tmpstrs[i]);
		strbuf.append('"');
		return new String(strbuf);
	}
	
	public static String toString(int[] tmpints) {
		if (tmpints == null)
			return "";
		StringBuffer strbuf = new StringBuffer();
		strbuf.append(Integer.toString(tmpints[0]));
		for (int i = 1; i < tmpints.length; i++)
			strbuf.append("," + Integer.toString(tmpints[i]));
		return new String(strbuf);
	}
}