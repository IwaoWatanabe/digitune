/*
 * Message Class
 *		1998/03/10 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.io.LineBufferedInputStream;
import jp.or.din.digitune.util.StringReplacer;
import jp.or.din.digitune.util.Codecs;
import java.io.File;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import java.util.Hashtable;
import java.util.ResourceBundle;

public class Message {
	protected static final String CRLF = "\r\n";
	protected static LocalResource rc = LocalResource.getResource();
	protected Hashtable headers = new Hashtable();
	
	protected Message() {
	}
	
	protected final static String charset2encode(String charset) {
		try {
			return rc.getString(charset);
		} catch (Exception ex) {
			return "8859_1";
		}
	}
	
	protected final static String encodeString(String src) {
		String defaultcharset = rc.getString("defaultCharset");
		try {
			byte[] tmpbuf = src.getBytes(charset2encode("to_"+defaultcharset));
			for (int i = 0; i < tmpbuf.length; i++) {
				if (tmpbuf[i] < 0x20 || tmpbuf[i] > 0x7a) {
					int j;
					for (j = i; j < tmpbuf.length; j++)
						if (tmpbuf[j] == 0x20)
							break;
					byte[] encodedst, encodesrc = new byte[j - i];
					System.arraycopy(tmpbuf, i, encodesrc, 0, j - i);
					encodedst = Codecs.base64Encode(encodesrc);
					byte[] encodeprefix = ("=?"+defaultcharset+"?B?")
						.getBytes("8859_1");
					byte[] encodesuffix = "?=".getBytes("8859_1");
					int endindex = i + encodeprefix.length
						+ encodedst.length + encodesuffix.length;
					byte[] tmpbuf2 = new byte[endindex+(tmpbuf.length-j)];
					System.arraycopy(tmpbuf, 0, tmpbuf2, 0, i);
					System.arraycopy(encodeprefix, 0, tmpbuf2, i
						, encodeprefix.length);
					System.arraycopy(encodedst,0,tmpbuf2,i+encodeprefix.length
						, encodedst.length);
					System.arraycopy(encodesuffix, 0, tmpbuf2
						, i+encodeprefix.length+encodedst.length
						, encodesuffix.length);
					System.arraycopy(tmpbuf, j, tmpbuf2, endindex
						, tmpbuf.length - j);
					tmpbuf = tmpbuf2;
					i = endindex;
				}
			}
			return new String(tmpbuf, "8859_1");
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			return src;
		}
	}
	
	protected final static String decodeString(String src) {
		int index = 0, oldindex = 0;
		String charset = "", encode = "";
		StringBuffer dst = new StringBuffer();
		while ((index = src.indexOf("=?", oldindex)) != -1) {
			if ((index - oldindex) != 0)
				dst.append(src.substring(oldindex, index));
			try {
				charset = src.substring(index+2
					, src.indexOf("?", index+2)).toLowerCase();
				index = src.indexOf("?", index+2)+1;
				encode = src.substring(index
					, src.indexOf("?", index)).toLowerCase();
				index = src.indexOf("?", index)+1;
				if (encode.equals("b")) {
					dst.append(Codecs.base64Decode(src.substring(index
						, src.indexOf("?=", index))
						, charset2encode("from_" + charset)));
				} else if (encode.equals("q")) {
					dst.append(Codecs.quotedpDecode(StringReplacer
						.replaceString(src.substring(index
						, src.indexOf("?=", index)), "_", " ")
						, charset2encode("from_" + charset)));
				} else
					dst.append(src.substring(index, src.indexOf("?=", index)));
				index = src.indexOf("?=", index)+2;
			} catch (StringIndexOutOfBoundsException ex) {
				oldindex = index;
				break;
			}
			oldindex = index;
		}
		dst.append(src.substring(oldindex, src.length()));
		return dst.toString();
	}
	
	protected String toHeaderCase(String header) {
		boolean notalpha = true;
		StringBuffer strbuf = new StringBuffer();
		for (int i = 0; i < header.length(); i++) {
			if (Character.isLetter(header.charAt(i))) {
				if (notalpha)
					strbuf.append(Character.toUpperCase(header.charAt(i)));
				else
					strbuf.append(header.charAt(i));
				notalpha = false;
			} else {
				strbuf.append(header.charAt(i));
				notalpha = true;
			}
		}
		return new String(strbuf);
	}
	
	protected final static boolean isLWSPchar(char src) {
		if (src == ' ' || src == '\t')
			return true;
		return false;
	}
	
	public final static Hashtable getHeaders(InputStream in) {
		Hashtable hs = new Hashtable();
		try {
			String encode
				= charset2encode("from_" + rc.getString("defaultCharset"));
			LineBufferedInputStream bin = new LineBufferedInputStream(in);
			byte[] tmpbuf = bin.readLine();
			String tmpstr = new String(tmpbuf, encode);
			while (tmpbuf != null && tmpbuf.length > 2) {
				int colon = tmpstr.indexOf(':');
				if (colon == -1)
				/*	if (tmpstr.toLowerCase().indexOf("from ") < 0)
						break;
					else */{
						tmpbuf = bin.readLine();
						continue;
					}
				String header = tmpstr.substring(0, colon).toLowerCase();
				StringBuffer content
					= new StringBuffer(tmpstr.substring(colon+1).trim());
				tmpbuf = bin.readLine();
				tmpstr = new String(tmpbuf, encode);
				while (tmpbuf != null && tmpbuf.length > 2
					&& isLWSPchar(tmpstr.charAt(0))) {
					tmpstr = tmpstr.trim();
					if (!tmpstr.equals(""))
						content.append(tmpstr);
					tmpbuf = bin.readLine();
					tmpstr = new String(tmpbuf, encode);
				}
				String tmpheader = decodeString(content.toString());
				Vector tmpvec;
				if (hs.get(header) != null)
					tmpvec = (Vector) hs.get(header);
				else
					tmpvec = new Vector();
				tmpvec.addElement(tmpheader);
				hs.put(header, tmpvec);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return hs;
	}
	
	public final static Hashtable getHeadersOld(InputStream in) {
		Hashtable hs = new Hashtable();
		try {
			String encode
				= charset2encode("from_" + rc.getString("defaultCharset"));
			BufferedReader bin = new BufferedReader(new InputStreamReader(in
				, encode));
			String tmpstr = bin.readLine();
			while (tmpstr != null && !tmpstr.equals("")) {
				int colon = tmpstr.indexOf(':');
				if (colon == -1) {
					tmpstr = bin.readLine();
					continue;
				}
				String header = tmpstr.substring(0, colon).toLowerCase();
				StringBuffer content
					= new StringBuffer(tmpstr.substring(colon+1).trim());
				tmpstr = bin.readLine();
				while (tmpstr != null && !tmpstr.equals("")
					&& isLWSPchar(tmpstr.charAt(0))) {
					tmpstr = tmpstr.trim();
					if (!tmpstr.equals(""))
						content.append(tmpstr);
					tmpstr = bin.readLine();
				}
				String tmpheader = decodeString(content.toString());
				Vector tmpvec;
				if (hs.get(header) != null)
					tmpvec = (Vector) hs.get(header);
				else
					tmpvec = new Vector();
				tmpvec.addElement(tmpheader);
				hs.put(header, tmpvec);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return hs;
	}
	
	public void setHeader(String ah, String af) {
		Vector tmpvec = (Vector) headers.get(ah);
		if (tmpvec == null)
			tmpvec = new Vector();
		tmpvec.addElement(af);
		headers.put(ah, tmpvec);
	}
	
	public void setHeader(String ah, int index, String af) {
		Vector tmpvec = (Vector) headers.get(ah);
		if (tmpvec == null)
			tmpvec = new Vector();
		index = (index < 0) ? 0 : index;
		if (tmpvec.size() == 0 || tmpvec.size() <= index)
			tmpvec.addElement(af);
		else 
			tmpvec.setElementAt(af, index);
		headers.put(ah, tmpvec);
	}
	
	public String[] getHeader(String ah) {
		if (headers.get(ah) == null)
			return null;
		Vector tmpvec = (Vector) headers.get(ah);
		String[] tmpstrs = new String[tmpvec.size()];
		tmpvec.copyInto(tmpstrs);
		return tmpstrs;
	}
	
	public void removeHeader(String ah) {
		headers.remove(ah);
	}
	
	public void setHeaders(Hashtable aheaders) {
		headers = aheaders;
	}
	
	public Hashtable getHeaders() {
		return headers;
	}
}
