/*
 * Attach Process class
 *		1998/05/25 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.io.GetLine;
import jp.or.din.digitune.gui.ProgressProcess;
import jp.or.din.digitune.gui.VariableEvent;
import jp.or.din.digitune.gui.VariableListener;
import jp.or.din.digitune.util.Codecs;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.ResourceBundle;

public class AttachProcess extends ProgressProcess {
	static final String CRLF = "\r\n";
	static final int BUFSIZE = 8192;
	ResourceBundle rc
		= ResourceBundle.getBundle("jp.or.din.digitune.morning.resource");
	
	File dst = null, src = null;
	long offset = -1, size = -1;
	OutputStream out = null;
	String contenttype = "", name = "", encoding = "", boundary = "";
	
	public AttachProcess(File af1, File af2, long aoff, long asize) {
		src = af1;
		dst = af2;
		offset = aoff;
		size = asize;
		try {
			BufferedInputStream bin = new BufferedInputStream(
				new FileInputStream(src));
			bin.skip(offset);
			Hashtable headers = Message.getHeaders(bin);
			bin.close();
			Vector tmpvec = (Vector) headers.get("content-type");
			if (tmpvec != null) {
				String tmpstr = (String) tmpvec.elementAt(0);
				HeaderParser parser = new HeaderParser(tmpstr);
				contenttype = parser.getField();
				name = parser.getParam("name");
			}
			tmpvec = (Vector) headers.get("content-transfer-encoding");
			if (tmpvec != null)
				encoding = (String) tmpvec.elementAt(0);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public AttachProcess(File af1, OutputStream aout, String ab) {
		src = af1;
		out = aout;
		boundary = ab;
		contenttype = null;
		try {
			BufferedInputStream in = new BufferedInputStream(
				new FileInputStream(src));
			contenttype = URLConnection.guessContentTypeFromStream(in);
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		if (contenttype == null)
			contenttype = "application/octet-stream";
		name = src.getName();
	}
	
	public String getTitle() {
		if (out == null)
			return rc.getString("detachStr");
		else
			return rc.getString("attachStr");
	}
	
	public String getContentType() {
		return contenttype;
	}
	
	public String getFileName() {
		return name;
	}
	
	stateObject currentstate;
	File result = null;
	
	public void setCurrentState(stateObject as) {
		currentstate = as;
	}
	
	private interface stateObject {
		public boolean process(AttachProcess p);
	}
	
	private class attachState implements stateObject {
		public boolean process(AttachProcess p) {
			int max = (int) src.length(), current = 0;
			try {
				byte[] strbuf = (CRLF + "--" + boundary + CRLF)
					.getBytes("8859_1");
				out.write(strbuf, 0, strbuf.length);
				strbuf = ("content-type: " + contenttype
					+ "; name=" + src.getName() + CRLF).getBytes("8859_1");
				out.write(strbuf, 0, strbuf.length);
				strbuf = ("content-transfer-encoding: base64" 
					+ CRLF + CRLF).getBytes("8859_1");
				out.write(strbuf, 0, strbuf.length);
				FileInputStream fin = new FileInputStream(src);
				byte[] readbuf = new byte[57], crlfb = CRLF.getBytes("8859_1");
				int c = 0;
				String writingstr = rc.getString("writingStr");
				VariableEvent ve = new VariableEvent(AttachProcess.this
					, "", 0, 0);
				while ((c = fin.read(readbuf, 0, readbuf.length)) > 0
					&& !isInterrupted()) {
					ve.setStatus(writingstr + getFileName());
					ve.setMaxValue(max);
					ve.setCurrentValue(current += c);
					invokeListener(ve);
					if (c < readbuf.length) {
						byte[] tmpbuf = new byte[c];
						System.arraycopy(readbuf, 0, tmpbuf, 0, c);
						readbuf = tmpbuf;
					}
					byte[] tmpstr = Codecs.base64Encode(readbuf);
					out.write(tmpstr);
					out.write(crlfb);
				}
				fin.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				VariableEvent ve = new VariableEvent(AttachProcess.this
					, rc.getString("fileError"), 0, 1);
				invokeListener(ve);
				return false;
			}
			VariableEvent ve = new VariableEvent(AttachProcess.this
				, rc.getString("doneStr"), 0, 1);
			invokeListener(ve);
			return false;
		}
	}
	
	private class detachState implements stateObject {
		public boolean process(AttachProcess p) {
			int max = (int) size, current = 0;
			try {
				GetLine gl = new GetLine(new BufferedInputStream(
					new FileInputStream(src)), offset, (int) size);
				int tmp = 0;
				while ((tmp = gl.getLine().length) != 2)
					current += tmp;
				int encode = 0;
				if (encoding.toLowerCase().trim().equals("quoted-printable"))
					encode = 2;
				else if (encoding.toLowerCase().trim().equals("base64"))
					encode = 1;
				else
					encode = 0;
				byte[] tmpbuf;
				BufferedOutputStream bout = new BufferedOutputStream(
					new FileOutputStream(dst));
				String writingstr = rc.getString("writingStr");
				VariableEvent ve = new VariableEvent(AttachProcess.this
					, "", 0, 0);
				while ((tmpbuf = gl.getLine()) != null && !isInterrupted()) {
					ve.setStatus(writingstr);
					ve.setMaxValue(max);
					ve.setCurrentValue(current += tmpbuf.length);
					invokeListener(ve);
					switch (encode) {
					case 0:
						bout.write(tmpbuf);
						break;
					case 1:
						bout.write(Codecs.base64Decode(tmpbuf));
						break;
					case 2:
						bout.write(Codecs.quotedpDecode(tmpbuf));
						break;
					}
				}
				gl.close();
				bout.flush();
				bout.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				VariableEvent ve = new VariableEvent(AttachProcess.this
					, rc.getString("fileError"), 0, 1);
				invokeListener(ve);
				return false;
			}
			VariableEvent ve = new VariableEvent(AttachProcess.this
				, rc.getString("doneStr"), 0, 1);
			invokeListener(ve);
			return false;
		}
	}
	
	public void run() {
		if (out == null)
			currentstate = new detachState();
		else
			currentstate = new attachState();
		boolean f = true;
		while (!isInterrupted() && f)
			f = currentstate.process(this);
		return;
	}
}
