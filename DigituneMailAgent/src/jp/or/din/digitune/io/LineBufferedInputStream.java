/*
 * LineBufferedInputStream class
 *		1998/03/11 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.io;

import jp.or.din.digitune.util.ByteBuffer;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

public class LineBufferedInputStream extends BufferedInputStream {
	byte[] buf = new byte[4096];
	int begin = 0, end = 0;
	
	private void fill() throws IOException {
		end = super.read(buf, begin = 0, buf.length);
	}
	
	public LineBufferedInputStream(InputStream in) {
		super(in);
	}
	
	public LineBufferedInputStream(InputStream in, int size) {
		super(in, size);
	}
	
	public int available() throws IOException {
		if (begin < end)
			return end - begin;
		else
			return super.available();
	}
	
	public int read() throws IOException {
		if (begin < end)
			return buf[begin++];
		else
			return super.read();
	}
	
	public int read(byte[] abuf, int ab, int alen) throws IOException {
		if (begin < end) {
			if (alen < (end - begin)) {
				System.arraycopy(buf, begin, abuf, ab, alen);
				begin += alen;
				return alen;
			} else {
				int tmplen = end - begin;
				System.arraycopy(buf, begin, abuf, ab, tmplen);
				begin = end;
				return tmplen;
			}
		} else
			return super.read(abuf, ab, alen);
	}
	
	public int read(byte[] abuf) throws IOException {
		return read(abuf, 0, abuf.length);
	}
	
	public byte[] readLine() throws IOException {
		ByteBuffer bf = new ByteBuffer();
	outer:
		while (true) {
			if (begin >= end)
				fill();
			if (begin >= end) {
				if (bf.length() > 0)
					return bf.getBytes();
				else
					return null;
			}
			boolean eol = false;
			byte tmpb = 0;
			int index;
		inner:
			for (index = begin; index < end; index++) {
				tmpb = buf[index];
				if (tmpb == '\n' || tmpb == '\r') {
					eol = true;
					break inner;
				}
			}
			bf.append(buf, begin, index - begin);
			begin = index;
			if (eol) {
				bf.append(buf[begin++]);
				if (tmpb == '\r') {
					if (begin >= end)
						fill();
					if (begin < end && buf[begin] == '\n')
						bf.append(buf[begin++]);
					break outer;
				}
			}
		}
		return bf.getBytes();
	}
}
