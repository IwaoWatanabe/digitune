/*
 * @(#)Codecs.java					0.2-2 23/03/1997
 *
 *	This file is part of the HTTPClient package 
 *	Copyright (C) 1996,1997	 Ronald Tschalaer
 *
 *	This library is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU Library General Public
 *	License as published by the Free Software Foundation; either
 *	version 2 of the License, or (at your option) any later version.
 *
 *	This library is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *	Library General Public License for more details.
 *
 *	You should have received a copy of the GNU Library General Public
 *	License along with this library; if not, write to the Free
 *	Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 *	MA 02111-1307, USA
 *
 *	For questions, suggestions, bug-reports, enhancement-requests etc.
 *	I may be contacted at:
 *
 *	ronald@innovation.ch
 *	Ronald.Tschalaer@psi.ch
 *
 *	Modified by T.Kazawa 1997/04/18
 */

package jp.or.din.digitune.util;

import java.util.BitSet;
import java.io.*;

/**
 * This class collects various encoders and decoders.
 *
 * @version	0.2 (bug fix 2)	 23/03/1997
 * @author	Ronald Tschal&auml;r
 */

public class Codecs
{
	private static BitSet  BoundChar;
	private static byte[]  Base64EncMap, Base64DecMap;

	// Class Initializer

	static
	{	
	// rfc-1521: bcharsnospace - used for multipart codings
	BoundChar = new BitSet(256);
	for (int ch='0'; ch <= '9'; ch++)  BoundChar.set(ch);
	for (int ch='A'; ch <= 'Z'; ch++)  BoundChar.set(ch);
	for (int ch='a'; ch <= 'z'; ch++)  BoundChar.set(ch);
	BoundChar.set('\'');
	BoundChar.set('(');
	BoundChar.set(')');
	BoundChar.set('+');
	BoundChar.set(',');
	BoundChar.set('-');
	BoundChar.set('.');
	BoundChar.set('/');
	BoundChar.set(':');
	BoundChar.set('=');
	BoundChar.set('?');
	BoundChar.set('_');

	// rfc-521: Base64 Alphabet
	byte[] map =
		{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		 '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
	Base64EncMap = map;
	Base64DecMap = new byte[128];
	for (int idx=0; idx<Base64EncMap.length; idx++)
		Base64DecMap[Base64EncMap[idx]] = (byte) idx;
	}


	// Constructors

	/**
	 * This class isn't meant to be instantiated.
	 */
	private Codecs() {}

	// Methods

	/**
	 * This method encodes the given string using the base64-encoding
	 * specified in RFC-1521 (Section 5.2). It's used for example in the
	 * "Basic" authorization scheme.
	 *
	 * @param  str the string
	 * @return the base64-encoded <var>str</var>
	 */
	public final static String base64Encode(String str)
	{
	if (str == null)  return  null;

	byte data[] = str.getBytes();

	return new String(base64Encode(data));
	}

	public final static String base64Encode(String str, String encode)
	{
	if (str == null)  return  null;
	try {
		byte data[] = str.getBytes(encode);
		return new String(base64Encode(data), "8859_1");
	} catch (UnsupportedEncodingException e) {
		return null;
	}
	}


	/**
	 * This method encodes the given byte[] using the base64-encoding
	 * specified in RFC-1521 (Section 5.2).
	 *
	 * @param  data the data
	 * @return the base64-encoded <var>data</var>
	 */
	public final static byte[] base64Encode(byte[] data)
	{
	if (data == null)  return  null;

	int sidx, didx;
	byte dest[] = new byte[((data.length+2)/3)*4];


	// 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
	for (sidx=0, didx=0; sidx < data.length-2; sidx += 3)
	{
		dest[didx++] = Base64EncMap[(data[sidx] >>> 2) & 077];
		dest[didx++] = Base64EncMap[(data[sidx+1] >>> 4) & 017 |
					(data[sidx] << 4) & 077];
		dest[didx++] = Base64EncMap[(data[sidx+2] >>> 6) & 003 |
					(data[sidx+1] << 2) & 077];
		dest[didx++] = Base64EncMap[data[sidx+2] & 077];
	}
	if (sidx < data.length)
	{
		dest[didx++] = Base64EncMap[(data[sidx] >>> 2) & 077];
		if (sidx < data.length-1)
		{
		dest[didx++] = Base64EncMap[(data[sidx+1] >>> 4) & 017 |
						(data[sidx] << 4) & 077];
		dest[didx++] = Base64EncMap[(data[sidx+1] << 2) & 077];
		}
		else
		dest[didx++] = Base64EncMap[(data[sidx] << 4) & 077];
	}

	// add padding
	for ( ; didx < dest.length; didx++)
		dest[didx] = '=';

	return dest;
	}


	/**
	 * This method decodes the given string using the base64-encoding
	 * specified in RFC-1521 (Section 5.2).
	 *
	 * @param  str the base64-encoded string.
	 * @return the decoded <var>str</var>.
	 */
	public final static String base64Decode(String str)
	{
	if (str == null)  return  null;

	byte data[] = str.getBytes();

	return new String(base64Decode(data));
	}

	public final static String base64Decode(String str, String encode)
	{
	if (str == null)  return  null;
	try {
		byte data[] = str.getBytes("8859_1");
		return new String(base64Decode(data), encode);
	} catch (UnsupportedEncodingException e) {
		return null;
	}
	}

	final static boolean isBase64Byte(byte b) {
		if (b >= 'a' && b <= 'z')
			return true;
		if (b >= 'A' && b <= 'Z')
			return true;
		if (b >= '0' && b <= '9')
			return true;
		if (b == '=' || b == '/' || b == '+')
			return true;
		return false;
	}

	/**
	 * This method decodes the given byte[] using the base64-encoding
	 * specified in RFC-1521 (Section 5.2).
	 *
	 * @param  data the base64-encoded data.
	 * @return the decoded <var>data</var>.
	 */
	public final static byte[] base64Decode(byte[] src)
	{
	if (src == null)  return  null;
	
	int i = 0;
	for (int j = 0; j < src.length; j++) {
		if (!isBase64Byte(src[j]))
			continue;
		src[i++] = src[j];
	}
	byte[] data = new byte[i];
	System.arraycopy(src, 0, data, 0, i);
	
	int tail = data.length;
	if (tail > 0)
		while (data[tail-1] == '=')	 tail--;

	byte dest[] = new byte[tail - data.length/4];


	// ascii printable to 0-63 conversion
	for (int idx = 0; idx <data.length; idx++)
		data[idx] = Base64DecMap[data[idx]];

	// 4-byte to 3-byte conversion
	int sidx, didx;
	for (sidx = 0, didx=0; didx < dest.length-2; sidx += 4, didx += 3)
	{
		dest[didx]	 = (byte) ( ((data[sidx] << 2) & 255) |
				((data[sidx+1] >>> 4) & 003) );
		dest[didx+1] = (byte) ( ((data[sidx+1] << 4) & 255) |
				((data[sidx+2] >>> 2) & 017) );
		dest[didx+2] = (byte) ( ((data[sidx+2] << 6) & 255) |
				(data[sidx+3] & 077) );
	}
	if (didx < dest.length)
		dest[didx]	 = (byte) ( ((data[sidx] << 2) & 255) |
				((data[sidx+1] >>> 4) & 003) );
	if (++didx < dest.length)
		dest[didx]	 = (byte) ( ((data[sidx+1] << 4) & 255) |
				((data[sidx+2] >>> 2) & 017) );

	return dest;
	}
	
	public final static String quotedpEncode(String src) {
		byte[] data = src.getBytes();
		return new String(quotedpEncode(data));
	}
	
	public final static String quotedpEncode(String src, String encode) {
		try {
			byte[] data = src.getBytes(encode);
			return new String(quotedpEncode(data), "8859_1");
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}
	
	public final static byte[] quotedpEncode(byte[] src) {
		String CRLF = "\n\r";
		StringBuffer dstbuf = new StringBuffer();
		int ccounter = 0;
		
		for (int i = 0; i < src.length; i++) {
			if (ccounter > 75) {
				dstbuf.append("="+CRLF);
				ccounter = 0;
			}
			if (src[i] == 0x0a || src[i] == 0x0d) {
				byte[] tmpbytes = { src[i] };
				try {
					dstbuf.append(new String(tmpbytes, "8859_1"));
				} catch (UnsupportedEncodingException e) {}
				ccounter = 0;
			} else if ((src[i] > 32 && src[i] < 61)
				||	   (src[i] > 61 && src[i] < 127)
				|| src[i] == 9 || src[i] == 32) {
				byte[] tmpbytes = { src[i] };
				try {
					dstbuf.append(new String(tmpbytes, "8859_1"));
				} catch (UnsupportedEncodingException e) {}
				ccounter++;
			} else {
				String tmpstr = "00"+Integer.toHexString((int) src[i]);
				dstbuf.append(("=" + tmpstr.substring(tmpstr.length()-2
					, tmpstr.length())).toUpperCase());
				ccounter += 3;
			}
		}
		try {
			return dstbuf.toString().getBytes("8859_1");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public final static String quotedpDecode(String src) {
		byte[] data = src.getBytes();
		return new String(quotedpDecode(data));
	}
	
	public final static String quotedpDecode(String src, String encode) {
		try {
			byte[] data = src.getBytes("8859_1");
			return new String(quotedpDecode(data), encode);
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}
	
	public final static byte[] quotedpDecode(byte[] src) {
		int loop = 0, counter = 0;
		byte[] dst = new byte[src.length];
		
		try {
			while (loop < src.length) {
				if (src[loop] == 61) {
					if (src[loop+1] == 0x0a || src[loop+1] == 0x0d) {
						int j = 1;
						while (src[loop+j] == 0x0a || src[loop+j] == 0x0d)
							j++;
						loop += j;
					} else {
						byte[] tmpbytes = { src[loop+1], src[loop+2] };
						try {
							dst[counter++] = Integer.decode("0x"+(
								new String(tmpbytes, "8859_1"))).byteValue();
						} catch (UnsupportedEncodingException e) {}
						loop += 3;
					}
				} else {
					dst[counter++] = src[loop++];
				}
			}
		} catch (ArrayIndexOutOfBoundsException ex) {}
		byte[] returnbuf = new byte[counter];
		System.arraycopy(dst, 0, returnbuf, 0, counter);
		return returnbuf;
	}
}