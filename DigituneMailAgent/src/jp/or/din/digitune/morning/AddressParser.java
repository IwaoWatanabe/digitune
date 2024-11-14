/*
 * Address Parser Class
 *		1998/04/21 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

public class AddressParser {
	String address = "", comment = "";
	
	public AddressParser(String src) {
		int state = 0;
		StringBuffer addressbuf = new StringBuffer();
		StringBuffer commentbuf = new StringBuffer();
		for (int i = 0; i < src.length(); i++) {
			switch (state) {
			case 0:
				if (src.charAt(i) == '<') {
					commentbuf.append(addressbuf);
					addressbuf = new StringBuffer();
					state = 1;
					break;
				} else if (src.charAt(i) == '(') {
					state = 2;
					break;
				} else if (src.charAt(i) == '"') {
					state = 3;
					break;
				}
				addressbuf.append(src.charAt(i));
				break;
			case 1:
				if (src.charAt(i) == '>') {
					address = new String(addressbuf).trim();
					state = 0;
					break;
				}
				addressbuf.append(src.charAt(i));
				break;
			case 2:
				if (src.charAt(i) == ')') {
					comment = new String(commentbuf).trim();
					state = 0;
					break;
				}
				commentbuf.append(src.charAt(i));
				break;
			case 3:
				if (src.charAt(i) == '"') {
					comment = new String(commentbuf).trim();
					state = 0;
					break;
				}
				commentbuf.append(src.charAt(i));
				break;
			}
		}
		if (address.equals(""))
			address = new String(addressbuf).trim();
		if (comment.equals(""))
			comment = new String(commentbuf).trim();
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getComment() {
		return comment;
	}
}
