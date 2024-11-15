/*
 * Send Message Class
 *		1998/04/10 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.gui.ProgressBox;
import jp.or.din.digitune.gui.SequenceProgressProcess;
import jp.or.din.digitune.util.CSV;
import jp.or.din.digitune.util.StringReplacer;
import jp.kyasu.awt.Frame;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.IOException;
import java.text.BreakIterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;
import java.util.ResourceBundle;

public class SendMessage extends Message {
	Frame p;
	Account ac = null;
	String body = "";
	Vector attachfile = new Vector();
	MessageInfo msginfo = null;
	ResourceBundle rc
		= ResourceBundle.getBundle("jp.or.din.digitune.morning.resource");
	
	public SendMessage(Frame ap, Account a) {
		p = ap;
		ac = a;
		setHeader("from", 0, ac.getUsername() + " <" + ac.getAddress() + ">");
		setHeader("x-mailer", 0, rc.getString("appName") + " ("
			+ System.getProperty("java.vendor") + " JDK"
			+ System.getProperty("java.version") + "/"
			+ System.getProperty("os.name") + " "
			+ System.getProperty("os.version") + " "
			+ System.getProperty("os.arch") + ")");
		setHeader("mime-version", 0, "1.0");
	}
	
	public void setBody(String ab) {
		body = ab;
	}
	
	public String getBody() {
		return body;
	}
	
	public void addAttachFile(String af) {
		attachfile.addElement(af);
	}
	
	public String[] getAttachFiles() {
		if (attachfile.size() == 0)
			return null;
		String[] tmpstrs = new String[attachfile.size()];
		attachfile.copyInto(tmpstrs);
		return tmpstrs;
	}
	
	public void clearAttachFile() {
		attachfile = new Vector();
	}
	
	static final int MAXLENGTH = 76;

	private void printEachForward(BreakIterator boundary, String src) {
		int start = boundary.first();
		for (int end = boundary.next();
			end != BreakIterator.DONE;
			start = end, end = boundary.next())
			System.out.println(src.substring(start, end));
	}
	
	void writeString (OutputStream out, String argstr) 
		throws IOException {
		String defaultcharset = rc.getString("defaultCharset");
		byte[] tmpbuf = argstr.getBytes(charset2encode("to_"+defaultcharset));
		out.write(tmpbuf, 0, tmpbuf.length);
		out.flush();
	}
	
	void writeHeader(OutputStream out, String header)
		throws IOException {
		String[] tmpstrs = getHeader(header);
		for (int i = 0; i < tmpstrs.length; i++) {
			String tmpstr = toHeaderCase(header) + ": " + tmpstrs[i];
			BreakIterator boundary = BreakIterator.getWordInstance();
			boundary.setText(tmpstr);
			int begin = boundary.first(), end = begin, last = begin;
			StringBuffer tmpbuf = new StringBuffer();
			while (end != BreakIterator.DONE) {
				do {
					last = end;
					end = boundary.next();
				} while (end != BreakIterator.DONE
					&& encodeString(tmpstr.substring(begin, end)).length()
					< MAXLENGTH);
				tmpbuf.append(encodeString(tmpstr.substring(begin, last)));
				writeString(out, new String(tmpbuf) + CRLF);
				tmpbuf = new StringBuffer();
				tmpbuf.append(" ");
				begin = last;
			}
/*			int tmplength = 0, beginindex;
			StringBuffer tmpbuf = new StringBuffer();
			while (tmplength < tmpstr.length()) {
				for (beginindex = tmplength; tmplength < tmpstr.length()
					&& encodeString(tmpstr.substring(beginindex, tmplength+1))
					.length() < MAXLENGTH; tmplength++) ;
				tmpbuf.append(encodeString(
					tmpstr.substring(beginindex, tmplength)));
				writeString(out, tmpbuf.toString()+CRLF);
				tmpbuf = new StringBuffer();
				tmpbuf.append(" ");
			}*/
		}
	}
	
	String makeTimeZone() {
		TimeZone tmptz = DateFormat.getDateInstance().getTimeZone();
		int offset = tmptz.getRawOffset() / 3600 / 10;
		String tmpstr = Integer.toString(offset);
		String tmppm = "";
		if (offset < 0) {
			tmppm = "-";
			tmpstr = tmpstr.substring(1);
		} else
			tmppm = "+";
		if (tmpstr.length() < 4)
			tmpstr = ("0000".substring(tmpstr.length())) + tmpstr;
		if (!tmptz.getID().equals(""))
			tmpstr += " (" + tmptz.getID() + ")";
		return tmppm + tmpstr;
	}
	
	String makeRFC822Date(Date now) {
		SimpleDateFormat dateformatter
			= new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.US);
		dateformatter.setTimeZone(DateFormat.getDateInstance().getTimeZone());
		String datestr = dateformatter.format(now);
		return datestr+" "+makeTimeZone();
	}
	
	String makeBoundaryStr() {
		return "Morning-"+System.currentTimeMillis();
	}
	
	void outputBody(OutputStream out) throws IOException {
		setHeader("content-type", 0
			, "text/plain; Charset=" + rc.getString("defaultCharset"));
		setHeader("content-transfer-encoding", 0, "7bit");
		writeHeader(out, "content-type");
		writeHeader(out, "content-transfer-encoding");
		writeString(out, CRLF);
		BufferedReader reader
			= new BufferedReader(new StringReader(body));
		String tmpstr = "";
		while ((tmpstr = reader.readLine()) != null) {
			if (!tmpstr.equals(""))
				if (tmpstr.charAt(0) == '.')
					tmpstr = "." + tmpstr;
				else if (tmpstr.indexOf("From ") == 0)
					tmpstr = ">" + tmpstr;
			writeString(out, tmpstr + CRLF);
		}
	}
	
	public void writeMessage() {
		setHeader("date", 0, makeRFC822Date(new Date()));
		try {
			String mbox = ac.getName() + ".mbx";
			long offset = new File(mbox).length();
			msginfo = new MessageInfo();
			msginfo.setFlag((byte) 0);
			if (getHeader("subject") != null)
				msginfo.setSubject(getHeader("subject")[0]);
			msginfo.setFrom(getHeader("from")[0]);
			msginfo.setDate(getHeader("date")[0]);
			msginfo.setOffset(offset);
			BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(mbox, true));
			String[] orderedheaders = CSV.cut(rc.getString("orderedHeaders"));
			for (int i = 0; i < orderedheaders.length; i++) {
				if (getHeader(orderedheaders[i]) != null) {
					writeHeader(out, orderedheaders[i]);
					removeHeader(orderedheaders[i]);
				}
			}
			for (Enumeration en = headers.keys(); en.hasMoreElements(); )
				writeHeader(out, (String) en.nextElement());
			body = StringReplacer.replaceString(body, CRLF + ".", CRLF + "..");
			if (body.length() > 0 && body.charAt(0) == '.')
				body = "." + body;
			if (attachfile.size() == 0) {
				outputBody(out);
			} else {
				String boundary = makeBoundaryStr();
				setHeader("content-type", 0
					, "multipart/mixed; Boundary=\"" + boundary + "\"");
				writeHeader(out, "content-type");
				writeString(out, CRLF);
				writeString(out, "--" + boundary + CRLF);
				outputBody(out);
				SequenceProgressProcess spp = new SequenceProgressProcess();
				for (int i = 0; i < attachfile.size(); i++) {
					AttachProcess ap = new AttachProcess(
						new File((String) attachfile.elementAt(i))
						, out, boundary);
					spp.addProgressProcess(ap);
				}
				ProgressBox pb = new ProgressBox(p
					, rc.getString("attachTitle"), true);
				pb.addProgressProcess(spp);
				pb.setVisible(true);
				writeString(out, CRLF + "--" + boundary + "--" + CRLF);
			}
			writeString(out, CRLF + "." + CRLF);
			out.close();
			msginfo.setSize(new File(mbox).length() - offset);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void writeMessageInfo() {
		if (msginfo == null)
			return;
		try {
			String info = ac.getName() + "." + rc.getString("unsend") + ".idx";
			BufferedOutputStream out
				= new BufferedOutputStream(new FileOutputStream(info, true));
			msginfo.writeMessageInfo(out);
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void writeDraftMessageInfo() {
		if (msginfo == null)
			return;
		try {
			String info = ac.getName() + "." + rc.getString("draft") + ".idx";
			BufferedOutputStream out
				= new BufferedOutputStream(new FileOutputStream(info, true));
			msginfo.writeMessageInfo(out);
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
