/*
 * POP3 Client Process class
 *		1998/03/18 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.gui.ProgressProcess;
import jp.or.din.digitune.gui.VariableEvent;
import jp.or.din.digitune.gui.VariableListener;
import jp.or.din.digitune.io.LineBufferedInputStream;
import java.net.Socket;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.ResourceBundle;

public class Pop3Process extends ProgressProcess {
	/* ======== Constant Value ======== */
	static final char OK = '+';
	static final char NG = '-';
	static final String CRLF = "\r\n";
	static final String COM_USER = "USER";
	static final String COM_PASS = "PASS";
	static final String COM_LIST = "LIST";
	static final String COM_UIDL = "UIDL";
	static final String COM_RETR = "RETR";
	static final String COM_DELE = "DELE";
	static final String COM_QUIT = "QUIT";
	static final int POP3PORT = 110;
	static final int BUFSIZE = 8192;
	/* ================================ */
	
	Account ac;
/*	ResourceBundle rc
		= ResourceBundle.getBundle("jp.or.din.digitune.morning.resource");*/
	LocalResource rc = LocalResource.getResource();
	
	public Pop3Process(Account a) {
		ac = a;
	}
	
	public String getTitle() {
		return ac.getName();
	}
	
	private boolean pop3Status(LineBufferedInputStream as) {
		try {
			while (true) {
				String strbuf = new String(as.readLine(), "8859_1");
				if (strbuf.length() == 0)
					continue;
				else if (strbuf.charAt(0) == OK)
					return true;
				else if (strbuf.charAt(0) == NG)
					return false;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	private boolean authorization(LineBufferedInputStream in
		, OutputStreamWriter out) {
		VariableEvent ve = new VariableEvent(this, rc.getString("authStateStr")
			, 1, 0);
		invokeListener(ve);
		String[] comseq = {
			"",
			COM_USER+" "+ac.getID()+CRLF,
			COM_PASS+" "+ac.getPassword()+CRLF,
		};
		try {
			for (int i = 0; i < 3; i++) {
				out.write(comseq[i]);
				out.flush();
				if (!pop3Status(in)) {
					return false;
				}
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	private Vector getMailSizeList(LineBufferedInputStream in
		, OutputStreamWriter out) {
		VariableEvent ve 
			= new VariableEvent(this, rc.getString("sizeListStateStr"), 1, 0);
		invokeListener(ve);
		Vector sizelist = new Vector();
		try {
			out.write(COM_LIST + CRLF);
			out.flush();
			if (!pop3Status(in))
				return null;
			String strbuf;
			for (strbuf = new String(in.readLine(), "8859_1");
				!strbuf.trim().equals(".");
				strbuf = new String(in.readLine(), "8859_1")) {
				strbuf.trim();
				StringTokenizer tokenizer = new StringTokenizer(strbuf);
				if (tokenizer.hasMoreTokens())
					tokenizer.nextToken();
				if (tokenizer.hasMoreTokens())
					sizelist.addElement(tokenizer.nextToken());
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return sizelist;
	}
	
	private Vector getUIDList(LineBufferedInputStream in
		, OutputStreamWriter out) {
		VariableEvent ve 
			= new VariableEvent(this, rc.getString("UIDListStateStr"), 1, 0);
		invokeListener(ve);
		Vector uidlist = new Vector();
		try {
			out.write(COM_UIDL + CRLF);
			out.flush();
			if (!pop3Status(in))
				return null;
			String strbuf;
			for (strbuf = new String(in.readLine(), "8859_1");
				!strbuf.trim().equals(".");
				strbuf = new String(in.readLine(), "8859_1")) {
				strbuf.trim();
				StringTokenizer tokenizer = new StringTokenizer(strbuf);
				if (tokenizer.hasMoreTokens())
					tokenizer.nextToken();
				if (tokenizer.hasMoreTokens())
					uidlist.addElement(tokenizer.nextToken());
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return uidlist;
	}
	
	private interface stateObject {
		public boolean process(Pop3Process p);
	}
	
	private class authState implements stateObject {
		public boolean process(Pop3Process p) {
			if (!authorization(in, out)) {
				VariableEvent ve = new VariableEvent(Pop3Process.this
					, rc.getString("authorizationErrorStr"), 0, 1);
				invokeListener(ve);
				return false;
			} else {
				p.currentstate = new getMailSizeState();
				return true;
			}
		}
	}
	
	private class getMailSizeState implements stateObject {
		public boolean process(Pop3Process p) {
			mailsize = getMailSizeList(in, out);
			if (mailsize == null) {
				VariableEvent ve = new VariableEvent(Pop3Process.this
					, rc.getString("sizeListErrorStr"), 0, 1);
				invokeListener(ve);
				return false;
			} if (mailsize.size() == 0) {
				p.currentstate = new updateState();
				return true;
			} else {
				p.currentstate = new getUIDListState();
				return true;
			}
		}
	}
	
	private class getUIDListState implements stateObject {
		public boolean process(Pop3Process p) {
			uidlist = getUIDList(in, out);
			p.currentstate = new getMailState();
			return true;
		}
	}
	
	private class getMailState implements stateObject {
		private boolean isEOT(byte[] b) {
			if (b[0] == '\r' && b[1] == '\n' && b[2] == '.'
				&& b[3] == '\r' && b[4] == '\n')
				return true;
			return false;
		}
		
		public boolean process(Pop3Process p) {
			String mboxname = ac.getName() + ".mbx";
			int startindex = 0;
			if (uidlist != null) {
				String lastuid = ac.getLastUID();
				for (int i = 0; i < uidlist.size(); i++) {
					if (lastuid.equals((String) uidlist.elementAt(i))) {
						startindex = i + 1;
						break;
					}
				}
				if (ac.getDeleteState()) {
					for (int i = 0; i < startindex; i++) {
						try {
							out.write(COM_DELE+" "+Integer.toString(i+1)+CRLF);
							out.flush();
							if (!pop3Status(in)) {
								VariableEvent ve
									= new VariableEvent(Pop3Process.this
									, rc.getString("retrErrorStr"), 0, 1);
								invokeListener(ve);
								return false;
							}
						} catch (IOException e) {
							VariableEvent ve
								= new VariableEvent(Pop3Process.this
								, rc.getString("retrErrorStr"), 0, 1);
							invokeListener(ve);
							return false;
						}
					}
				}
				if (startindex == uidlist.size()) {
					p.currentstate = new updateState();
					return true;
				}
			}
			int allsize = 0, readsize = 0;
			for (int i = startindex; i < mailsize.size(); i++)
				allsize += Integer.parseInt((String) mailsize.elementAt(i));
			String receivingstr = rc.getString("mailReceivingStr");
			VariableEvent ve = new VariableEvent(Pop3Process.this, "", 0, 0);
			for (int i = startindex; i < mailsize.size() && !isInterrupted()
				; i++) {
				long mboxsize = new File(mboxname).length();
				ve.setStatus(receivingstr + (i + 1) + "/" + mailsize.size());
				ve.setMaxValue(allsize);
				ve.setCurrentValue(readsize);
				invokeListener(ve);
				try {
					out.write(COM_RETR+" "+Integer.toString(i+1)+CRLF);
					out.flush();
					if (!pop3Status(in)) {
						ve = new VariableEvent(Pop3Process.this
							, rc.getString("retrErrorStr"), 0, 1);
						invokeListener(ve);
						return false;
					}
				} catch (IOException e) {
					ve = new VariableEvent(Pop3Process.this
						, rc.getString("retrErrorStr"), 0, 1);
					invokeListener(ve);
					return false;
				}
				try {
					BufferedOutputStream bout = new BufferedOutputStream(
						new FileOutputStream(mboxname, true));
					int tmpsize = 0;
					byte[] tmpbuf = new byte[BUFSIZE], endcheck = new byte[5];
					while (true) {
						ve.setStatus(receivingstr + (i + 1) + "/"
							+ mailsize.size());
						ve.setMaxValue(allsize);
						ve.setCurrentValue(readsize + tmpsize);
						invokeListener(ve);
						int ia = in.available();
						int l = (ia > tmpbuf.length) ? tmpbuf.length : ia;
						int d = in.read(tmpbuf, 0, l);
						if (d > 4)
							System.arraycopy(tmpbuf, d - 5, endcheck, 0, 5);
						else {
							System.arraycopy(endcheck, d, endcheck, 0, 5 - d);
							System.arraycopy(tmpbuf, 0, endcheck, 5 - d, d);
						}
						if (isEOT(endcheck)) {
							bout.write(tmpbuf, 0, d);
							bout.flush();
							allsize += (tmpsize + d - Integer.parseInt(
								(String) mailsize.elementAt(i)));
							ve.setStatus(receivingstr + (i + 1) + "/"
								+ mailsize.size());
							ve.setMaxValue(allsize);
							ve.setCurrentValue(readsize += tmpsize + d);
							invokeListener(ve);
							break;
						}
						bout.write(tmpbuf, 0, d);
						bout.flush();
						tmpsize += d;
					}
					bout.close();
				} catch (IOException ex) {
					ve = new VariableEvent(Pop3Process.this
						, rc.getString("retrErrorStr"), 0, 1);
					invokeListener(ve);
					return false;
				}
				long tmpsize = new File(mboxname).length()
					- mboxsize;
				Hashtable headers = null;
				try {
					BufferedInputStream bin = new BufferedInputStream(
						new FileInputStream(mboxname));
					bin.skip(mboxsize);
					headers = Message.getHeaders(bin);
					bin.close();
					if (new Boolean(rc.getString("autoFiltering"))
						.booleanValue())
						am.writeInfo(headers, mboxsize, tmpsize);
					else
						am.writeInfo(rc.getString("inbox")
							, headers, mboxsize, tmpsize);
				} catch (IOException ex) {
					ve = new VariableEvent(Pop3Process.this
						, rc.getString("retrErrorStr"), 0, 1);
					invokeListener(ve);
					return false;
				}
				ac.setLastUID((String) uidlist.elementAt(i));
				if (ac.getDeleteState()) {
					try {
						out.write(COM_DELE+" "+Integer.toString(i+1)+CRLF);
						out.flush();
						if (!pop3Status(in)) {
							ve = new VariableEvent(Pop3Process.this
								, rc.getString("retrErrorStr"), 0, 1);
							invokeListener(ve);
							return false;
						}
					} catch (IOException e) {
						ve = new VariableEvent(Pop3Process.this
							, rc.getString("retrErrorStr"), 0, 1);
						invokeListener(ve);
						return false;
					}
				}
			}
			p.currentstate = new updateState();
			return true;
		}
	}
	
	private class updateState implements stateObject {
		public boolean process(Pop3Process p) {
			try {
				out.write(COM_QUIT + CRLF);
				out.flush();
				if (!pop3Status(in)) {
					VariableEvent ve = new VariableEvent(Pop3Process.this
						, rc.getString("updateErrorStr"), 0, 1);
					invokeListener(ve);
					return false;
				}
			} catch (IOException ex) {
				VariableEvent ve = new VariableEvent(Pop3Process.this
					, rc.getString("updateErrorStr"), 0, 1);
				invokeListener(ve);
				return false;
			}
			VariableEvent ve = new VariableEvent(Pop3Process.this
				, rc.getString("doneStr"), 0, 1);
			invokeListener(ve);
			return false;
		}
	}
	
	public stateObject currentstate;
	AccountManager am;
	Socket sock;
	LineBufferedInputStream in;
	OutputStreamWriter out;
	Vector uidlist = null, mailsize = null;
	
	public void run() {
		try {
			sock = new Socket(ac.getPOPServer(), POP3PORT);
			in = new LineBufferedInputStream(sock.getInputStream(), BUFSIZE);
			out = new OutputStreamWriter(sock.getOutputStream(), "8859_1");
		} catch (Exception ex) {
			VariableEvent ve = new VariableEvent(this
				, rc.getString("openConnectionErrorStr") + ex, 0, 1);
			invokeListener(ve);
			return;
		}
		am = new AccountManager(ac);
		am.load();
		currentstate = new authState();
		boolean f = true;
		while (!isInterrupted() && f)
			f = currentstate.process(this);
		try {
			in.close();
			out.close();
			sock.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return;
	}
}
