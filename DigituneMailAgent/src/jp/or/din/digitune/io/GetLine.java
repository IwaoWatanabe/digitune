/*	
 *	Get a Line from InputStream class
 *		1997/06/15 Copyright (C) T.Kazawa
 */

package jp.or.din.digitune.io;

import java.io.*;

public class GetLine
{
	private static final int BUFSIZE = 8192;
	int currentread = 0, size;
	InputStream in;
	int bufindex, bufend;
	byte[] readbuf = new byte[BUFSIZE];
	
	public GetLine(InputStream argin, long argoffset, int argsize) {
		size = argsize;
		in = argin;
		try {
			in.skip(argoffset);
		} catch (IOException ex) {}
		bufindex = readbuf.length;
		putBuffer();
	}
	
	boolean putBuffer() {
		if (bufindex == 0)
			return false;
		int newindex = readbuf.length - bufindex;
		System.arraycopy(readbuf, bufindex, readbuf, 0, newindex);
		int readcount = (size - currentread > BUFSIZE - newindex)
			? BUFSIZE - newindex : size - currentread;
		for (int alreadyread = 0; alreadyread < readcount;) {
			try {
				alreadyread += in.read(readbuf, newindex + alreadyread
					, readcount - alreadyread);
			} catch (IOException ex) {}
		}
		currentread += readcount;
		bufindex = 0;
		bufend = newindex + readcount;
		return true;
	}
	
	public byte[] getLine() {
		if (currentread == size && bufindex == bufend)
			return null;
		int beginindex = bufindex;
		for (; bufindex < bufend; bufindex++){
			if (readbuf[bufindex] == 0x0d) {
				try {
					if (readbuf[bufindex+1] == 0x0a) {
						byte[] returnbuf = new byte[bufindex-beginindex+2];
						System.arraycopy(readbuf, beginindex
							, returnbuf, 0, bufindex - beginindex + 2);
						bufindex += 2;
						return returnbuf;
					} else {
						byte[] returnbuf = new byte[bufindex-beginindex+1];
						System.arraycopy(readbuf, beginindex
							, returnbuf, 0, bufindex - beginindex + 1);
						bufindex += 1;
						return returnbuf;
					}
				} catch (ArrayIndexOutOfBoundsException ex) {
					byte[] returnbuf = new byte[bufindex-beginindex+1];
					System.arraycopy(readbuf, beginindex
						, returnbuf, 0, bufindex - beginindex + 1);
					bufindex += 1;
					return returnbuf;
				}
			} else if (readbuf[bufindex] == 0x0a) {
				byte[] returnbuf = new byte[bufindex-beginindex+1];
				System.arraycopy(readbuf, beginindex
					, returnbuf, 0, bufindex - beginindex + 1);
				bufindex += 1;
				return returnbuf;
			}
		}
		bufindex = beginindex;
		if (size != currentread) {
			if (putBuffer()) {
				return getLine();
			} else {
				byte[] returnbuf = new byte[bufend - bufindex];
				System.arraycopy(returnbuf, 0, readbuf, bufindex
					, bufend - bufindex);
				bufindex = bufend;
				putBuffer();
				return returnbuf;
			}
		}
		byte[] returnbuf = new byte[bufend - bufindex];
		System.arraycopy(returnbuf, 0, readbuf, bufindex, bufend - bufindex);
		bufindex = bufend;
		return returnbuf;
	}
	
	public void close() {
		try {
			in.close();
		} catch (IOException ex) {}
	}
}
