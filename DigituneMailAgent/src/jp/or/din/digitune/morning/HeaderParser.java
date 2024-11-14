/* HeaderParser class */

package jp.or.din.digitune.morning;

import java.util.Hashtable;

public class HeaderParser {
	String field = "";
	Hashtable param = new Hashtable();
	
	public HeaderParser(String src) {
		if (src == null)
			return;
		int state = 0, backupstate = 0;
		StringBuffer strbuf = new StringBuffer();
		String tmpparam = "", tmpvalue = "";
		for (int i = 0; i < src.length(); i++) {
			switch (state) {
			case 0:
				if (src.charAt(i) == '"') {
					state = 1;
					break;
				} else if (src.charAt(i) == ';') {
					field = strbuf.toString().trim();
					strbuf = new StringBuffer();
					state = 2;
					break;
				} else if (src.charAt(i) == '(') {
					backupstate = state;
					state = 5;
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
			case 2:
				if (src.charAt(i) == '=') {
					tmpparam = strbuf.toString().trim().toLowerCase();
					strbuf = new StringBuffer();
					state = 3;
					break;
				} else if (src.charAt(i) == '(') {
					backupstate = state;
					state = 5;
					break;
				}
				strbuf.append(src.charAt(i));
				break;
			case 3:
				if (src.charAt(i) == '"') {
					state = 4;
					break;
				} else if (src.charAt(i) == ';') {
					tmpvalue = strbuf.toString().trim();
					param.put(tmpparam, tmpvalue);
					strbuf = new StringBuffer();
					tmpparam = tmpvalue = "";
					state = 2;
					break;
				} else if (src.charAt(i) == '(') {
					backupstate = state;
					state = 5;
					break;
				}
				strbuf.append(src.charAt(i));
				break;
			case 4:
				if (src.charAt(i) == '"') {
					state = 3;
					break;
				}
				strbuf.append(src.charAt(i));
				break;
			case 5:
				if (src.charAt(i) == ')')
					state = backupstate;
				break;
			}
		}
		if (state == 0 || state == 1) {
			field = strbuf.toString().trim();
		} else if (state == 3 || state == 4) {
			tmpvalue = strbuf.toString().trim();
			param.put(tmpparam, tmpvalue);
		}
	}
	
	public String getField() {
		return field;
	}
	
	public String getParam(String key) {
		return (String) param.get(key);
	}
}
