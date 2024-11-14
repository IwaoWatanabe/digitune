/*
 * SMTP Process class
 *		1998/04/20 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.gui.ProgressProcess;
import jp.or.din.digitune.gui.VariableEvent;
import jp.or.din.digitune.gui.VariableListener;
import jp.or.din.digitune.io.LineBufferedInputStream;
import jp.or.din.digitune.util.CSV;
import jp.kyasu.awt.Frame;
import jp.kyasu.util.VArray;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.ResourceBundle;

public class SmtpProcess extends ProgressProcess {
	/* ======== Constant Value ======== */
	static final char OK = '2';
	static final char SENDOK = '3';
	static final String CRLF = "\r\n";
	static final String COM_HELO = "HELO";
	static final String COM_MAIL = "MAIL FROM:";
	static final String COM_RCPT = "RCPT TO:";
	static final String COM_DATA = "DATA";
	static final String COM_QUIT = "QUIT";
	static final int SMTPPORT = 25;
	static final int BUFSIZE = 8192;
	/* ================================ */
	
	Account ac;
	ResourceBundle rc
		= ResourceBundle.getBundle("jp.or.din.digitune.morning.resource");
	
	public SmtpProcess(Account a) {
		ac = a;
	}
	
	public String getTitle() {
		return ac.getName();
	}
	
	private boolean	smtpStatus(LineBufferedInputStream as) {
		try {
			while (true) {
				String strbuf = new String(as.readLine(), "8859_1");
				lastreply = strbuf;
				if (strbuf.length() == 0)
					continue;
				else if (strbuf.charAt(0) == OK)
					return true;
				else if (strbuf.charAt(0) == SENDOK)
					return true;
				else
					return false;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	stateObject currentstate;
	LineBufferedInputStream in = null;
	OutputStream out = null;
	Vector msginfo = new Vector();
	VArray msgindex = new VArray(int.class);
	int procmsg = 0;
	int allmsgsize = 0, sentmsgsize = 0;
	String lastreply = "";
	
	public void setCurrentState(stateObject as) {
		currentstate = as;
	}
	
	private interface stateObject {
		public boolean process(SmtpProcess p);
	}
	
	private class setupState implements stateObject {
		public boolean process(SmtpProcess p) {
			VariableEvent ve = new VariableEvent(SmtpProcess.this
				, rc.getString("setupStateStr"), 1, 0);
			invokeListener(ve);
			boolean flag = false;
			FolderManager fm = new FolderManager(ac, rc.getString("unsend"));
			for (int i = 0; i < fm.getMessageCount(); i++) {
				MessageInfo tmpinfo = fm.getMessageInfo(i);
				if (!tmpinfo.getDeleteFlag()) {
					flag = true;
					allmsgsize += tmpinfo.getSize();
					msginfo.addElement(tmpinfo);
					msgindex.append(i);
				}
			}
			fm.close();
			if (!flag) {
				ve = new VariableEvent(SmtpProcess.this
				, rc.getString("doneStr"), 0, 1);
				invokeListener(ve);
				return false;
			}
			p.setCurrentState(new initState());
			return true;
		}
	}
	
	private class initState implements stateObject {
		public boolean process(SmtpProcess p) {
			VariableEvent ve = new VariableEvent(SmtpProcess.this
				, rc.getString("initStateStr"), allmsgsize, sentmsgsize);
			invokeListener(ve);
			String localhost;
			try {
				localhost = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException ex) {
				localhost = "localhost";
			}
			String[] comseq = {
				"",
				COM_HELO+" " + localhost + CRLF,
			};
			try {
				OutputStreamWriter outw = new OutputStreamWriter(out,"8859_1");
				for (int i = 0; i < comseq.length; i++) {
					outw.write(comseq[i]);
					outw.flush();
					if (!smtpStatus(in)) {
						ve = new VariableEvent(SmtpProcess.this
							, rc.getString("heloErrorStr") + lastreply, 0, 1);
						invokeListener(ve);
						return false;
					}
				}
			} catch (IOException e) {
				ve = new VariableEvent(SmtpProcess.this
					, rc.getString("heloErrorStr") + lastreply, 0, 1);
				invokeListener(ve);
				return false;
			}
			p.setCurrentState(new envelopeState());
			return true;
		}
	}
	
	private class envelopeState implements stateObject {
		public boolean process(SmtpProcess p) {
			if (procmsg >= msginfo.size()) {
				p.setCurrentState(new quitState());
				return true;
			}
			VariableEvent ve = new VariableEvent(SmtpProcess.this
				, rc.getString("envelopeStateStr")
				+ (procmsg + 1) + "/" + msginfo.size()
				, allmsgsize, sentmsgsize);
			invokeListener(ve);
			MessageInfo tmpinfo = (MessageInfo) msginfo.elementAt(procmsg);
			RecvMessage tmpmsg
				= new RecvMessage(new Frame(), new File(ac.getName() + ".mbx")
				, tmpinfo.getOffset(), tmpinfo.getSize());
			StringBuffer strbuf = new StringBuffer();
			if (tmpmsg.getHeader("to") != null) {
				String[] tmpstrs = tmpmsg.getHeader("to");
				for (int i = 0; i < tmpstrs.length; i++)
					strbuf.append("," + tmpstrs[i]);
			}
			if (tmpmsg.getHeader("cc") != null) {
				String[] tmpstrs = tmpmsg.getHeader("cc");
				for (int i = 0; i < tmpstrs.length; i++)
					strbuf.append("," + tmpstrs[i]);
			}
			if (tmpmsg.getHeader("bcc") != null) {
				String[] tmpstrs = tmpmsg.getHeader("bcc");
				for (int i = 0; i < tmpstrs.length; i++)
					strbuf.append("," + tmpstrs[i]);
			}
			String[] rcpts = CSV.cut(new String(strbuf));
			if (rcpts.length == 0)
				return false;
			for (int i = 0; i < rcpts.length; i++)
				rcpts[i] = new AddressParser(rcpts[i]).getAddress();
			try {
				OutputStreamWriter outw = new OutputStreamWriter(out,"8859_1");
				outw.write(COM_MAIL + "<" + ac.getAddress() + ">" + CRLF);
				outw.flush();
				if (!smtpStatus(in)) {
					ve = new VariableEvent(SmtpProcess.this
						, rc.getString("envelopeErrorStr") + lastreply, 0, 1);
					invokeListener(ve);
					return false;
				}
				for (int i = 0; i < rcpts.length; i++) {
					if (rcpts[i].equals(""))
						continue;
					outw.write(COM_RCPT + "<" + rcpts[i] + ">" + CRLF);
					outw.flush();
					if (!smtpStatus(in)) {
						ve = new VariableEvent(SmtpProcess.this
							, rc.getString("envelopeErrorStr") + lastreply
							, 0, 1);
						invokeListener(ve);
						return false;
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				ve = new VariableEvent(SmtpProcess.this
					, rc.getString("envelopeErrorStr") + lastreply, 0, 1);
				invokeListener(ve);
				return false;
			}
			p.setCurrentState(new transactionState());
			return true;
		}
	}
	
	private class transactionState implements stateObject {
		public boolean process(SmtpProcess p) {
			VariableEvent ve = new VariableEvent(SmtpProcess.this
				, rc.getString("transactionStateStr")
				+ (procmsg+1) + "/" + msginfo.size(), allmsgsize, sentmsgsize);
			invokeListener(ve);
			try {
				OutputStreamWriter outw = new OutputStreamWriter(out,"8859_1");
				outw.write(COM_DATA + CRLF);
				outw.flush();
				if (!smtpStatus(in)) {
					ve = new VariableEvent(SmtpProcess.this
						, rc.getString("dataErrorStr") + lastreply, 0, 1);
					invokeListener(ve);
					return false;
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				ve = new VariableEvent(SmtpProcess.this
					, rc.getString("dataErrorStr") + lastreply, 0, 1);
				invokeListener(ve);
				return false;
			}
			MessageInfo tmpinfo = (MessageInfo) msginfo.elementAt(procmsg);
			try {
				byte[] tmpbuf = new byte[BUFSIZE];
				BufferedInputStream bin = new BufferedInputStream(
					new FileInputStream(ac.getName() + ".mbx"));
				bin.skip(tmpinfo.getOffset());
				int value = 0, mailsize = (int) tmpinfo.getSize();
				String transactionstr = rc.getString("transactionStateStr");
				ve = new VariableEvent(SmtpProcess.this, "", 0, 0);
				while (value < mailsize) {
					int size = bin.read(tmpbuf, 0
						, ((mailsize - value) > BUFSIZE)
						? BUFSIZE : mailsize - value);
					out.write(tmpbuf, 0, size);
					value += size;
					sentmsgsize += size;
					ve.setStatus(transactionstr + (procmsg + 1) + "/"
						+ msginfo.size());
					ve.setMaxValue(allmsgsize);
					ve.setCurrentValue(sentmsgsize);
					invokeListener(ve);
				}
				out.flush();
				if (!smtpStatus(in)) {
					ve = new VariableEvent(SmtpProcess.this
						, rc.getString("dataErrorStr") + lastreply, 0, 1);
					invokeListener(ve);
					return false;
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				ve = new VariableEvent(SmtpProcess.this
					, rc.getString("dataErrorStr") + lastreply, 0, 1);
				invokeListener(ve);
				return false;
			}
			FolderManager fm = new FolderManager(ac, rc.getString("sent"));
			fm.writeMessageInfo(tmpinfo, fm.getMessageCount());
			fm.close();
			tmpinfo.setDeleteFlag();
			fm = new FolderManager(ac, rc.getString("unsend"));
			fm.writeMessageInfo(tmpinfo, msgindex.getInt(procmsg));
			fm.close();
			if (!isInterrupted()) {
				procmsg++;
				p.setCurrentState(new envelopeState());
			} else
				p.setCurrentState(new quitState());
			return true;
		}
	}
	
	private class quitState implements stateObject {
		public boolean process(SmtpProcess p) {
			try {
				OutputStreamWriter outw = new OutputStreamWriter(out,"8859_1");
				outw.write(COM_QUIT + CRLF);
				outw.flush();
				if (!smtpStatus(in)) {
					VariableEvent ve = new VariableEvent(SmtpProcess.this
						, rc.getString("quitErrorStr") + lastreply, 0, 1);
					invokeListener(ve);
					return false;
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				VariableEvent ve = new VariableEvent(SmtpProcess.this
					, rc.getString("quitErrorStr") + lastreply, 0, 1);
				invokeListener(ve);
				return false;
			}
			VariableEvent ve = new VariableEvent(SmtpProcess.this
				, rc.getString("doneStr"), 0, 1);
			invokeListener(ve);
			return false;
		}
	}
	
	public void run() {
		Socket sock;
		try {
			sock = new Socket(ac.getSMTPServer(), SMTPPORT);
			in = new LineBufferedInputStream(sock.getInputStream(), BUFSIZE);
			out = new BufferedOutputStream(sock.getOutputStream());
		} catch (Exception ex) {
			VariableEvent ve = new VariableEvent(SmtpProcess.this
				, rc.getString("openConnectionErrorStr") + ex, 0, 1);
			invokeListener(ve);
			return;
		}
		currentstate = new setupState();
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
