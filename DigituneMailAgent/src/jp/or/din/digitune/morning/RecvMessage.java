/*
 * Receive Message class
 *		1998/03/13 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.gui.ProgressBox;
import jp.or.din.digitune.io.GetLine;
import jp.or.din.digitune.io.LineBufferedInputStream;
import jp.or.din.digitune.util.CSV;
import jp.or.din.digitune.util.Codecs;
import jp.or.din.digitune.util.StringReplacer;
import org.ingrid.kazama.Cp932;
import jp.kyasu.awt.Frame;
import jp.kyasu.graphics.ClickableTextAction;
import jp.kyasu.graphics.ParagraphStyle;
import jp.kyasu.graphics.RichText;
import jp.kyasu.graphics.RichTextStyle;
import jp.kyasu.graphics.Text;
import jp.kyasu.graphics.TextAttachment;
import jp.kyasu.graphics.TextBuffer;
import jp.kyasu.graphics.TextStyle;
import jp.kyasu.graphics.VHRBorder;
import jp.kyasu.graphics.VImage;
import jp.kyasu.graphics.VSpace;
import java.awt.Font;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;

public class RecvMessage extends Message {
	static final char LF = '\n';
	Frame p;
	File f;
	long offset, size;
	String[] visibleheaders = null;
	TextBuffer body
		= new TextBuffer(new TextStyle("monospaced", Font.PLAIN, 12));
	Color bodycolor = Color.black, headercolor = null;
	boolean attachflag = false;
	
	public RecvMessage(Frame ap, File af, long aoff, long asize) {
		p = ap; f = af; offset = aoff; size = asize;
		try {
			BufferedInputStream fin
				= new BufferedInputStream(new FileInputStream(f));
			fin.skip(offset);
			headers = getHeaders(fin);
			fin.close();
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {}
		visibleheaders = CSV.cut(rc.getString("defaultVisibleHeaders"));
	}
	
	public void setVisibleHeaders(String[] aheaders) {
		visibleheaders = aheaders;
	}
	
	public String[] getVisibleHeaders() {
		return visibleheaders;
	}
	
	public boolean getAttachFlag() {
		return attachflag;
	}
	
	public Text getText() {
		return body.toText();
	}
	
	public RichText getRichText() {
		RichTextStyle tmpstyle = RichTextStyle.DEFAULT_DOCUMENT_STYLE;
		ParagraphStyle tmpps
			= RichTextStyle.DEFAULT_DOCUMENT_STYLE.getParagraphStyle();
		ParagraphStyle newps = new ParagraphStyle(tmpps.getAlignment()
			, tmpps.getLeftIndent(), tmpps.getRightIndent()
			, Integer.parseInt(rc.getString("lineSpace")));
		RichTextStyle newstyle = new RichTextStyle(tmpstyle.getLineWrap()
			, tmpstyle.getLineEnd(), tmpstyle.isVariableLineHeight()
			, tmpstyle.getTextStyle(), newps);
		return body.toRichText(newstyle);
	}
	
	public void setTextStyle(TextStyle astyle) {
		body.setTextStyle(astyle);
	}
	
	public TextStyle getTextStyle() {
		return body.getCurrentTextStyle();
	}
	
	public void setColor(Color tmpcolor) {
		bodycolor = tmpcolor;
		body.setColor(tmpcolor);
	}

	public void setHeaderColor(Color ac) {
		headercolor = ac;
	}

	public void process() {
		String[] tmpstrs = getHeader("content-type");
		String contenttype = null;
		if (tmpstrs != null)
			contenttype = tmpstrs[0].trim();
		String type = null, subtype = null;
		HeaderParser hp = null;
		if (contenttype != null) {
			hp = new HeaderParser(contenttype);
			StringTokenizer st
				= new StringTokenizer(hp.getField().toLowerCase(), "/", false);
			try {
				type = st.nextToken().trim();
				subtype = st.nextToken().trim();
			} catch (NoSuchElementException ex) {}
		}
		if (type == null || type.equals("text"))
			defaultProc(subtype, hp);
		else if (type.equals("multipart"))
			multipartProc(subtype, hp);
		else if (type.equals("message"))
			messageProc(subtype, hp);
		else
			unknownProc(subtype, hp);
	}
	
	public void headersProc() {
		if (headercolor != null)
			body.setColor(headercolor);
		if (visibleheaders == null || visibleheaders.length == 0) {
			for (Enumeration en = headers.keys(); en.hasMoreElements(); ) {
				String tmpkey = (String) en.nextElement();
				String[] tmpstrs = getHeader(tmpkey);
				for (int i = 0; i < tmpstrs.length; i++) {
					body.append(toHeaderCase(tmpkey) + ": ");
					body.append(Cp932.toCp932(tmpstrs[i]) + LF);
				}
			}
		} else {
			for (int i = 0; i < visibleheaders.length; i++) {
				if (getHeader(visibleheaders[i]) != null) {
					String[] tmpstrs = getHeader(visibleheaders[i]);
					for (int j = 0; j < tmpstrs.length; j++) {
						body.append(toHeaderCase(visibleheaders[i]) + ": ");
						body.append(Cp932.toCp932(tmpstrs[j]) + LF);
					}
				}
			}
		}
		body.setColor(bodycolor);
		body.append(LF);
	}
	
	void defaultProc(String subtype, HeaderParser hp) {
		String charset = null;
		if (hp != null)
			charset = hp.getParam("charset");
		if (charset == null)
			charset = rc.getString("defaultCharset");
		else
			charset = charset.toLowerCase();
		long tmpsize = size;
		byte[] tmpbuf;
		try {
			LineBufferedInputStream lbin
				= new LineBufferedInputStream(new FileInputStream(f));
			lbin.skip(offset);
			long allsize = 0;
			while ((tmpbuf = lbin.readLine()).length > 2 && allsize < size) {
				if (tmpbuf.length == 0) {
					return;
				} else {
					tmpsize -= tmpbuf.length;
					allsize += tmpbuf.length;
				}
			}
			tmpsize -= tmpbuf.length;
			tmpbuf = new byte[((int) tmpsize < 0) ? 0 : (int) tmpsize];
			int alreadyread = 0;
			while (alreadyread < tmpsize) {
				int readsize = lbin.read(tmpbuf, alreadyread
					, (int) (tmpsize - alreadyread));
				alreadyread += readsize;
			}
			lbin.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
		String[] tmpstrs = getHeader("content-transfer-encoding");
		if (tmpstrs != null) {
			String encoding = tmpstrs[0].toLowerCase();
			if (encoding.equals("base64")) {
				tmpbuf = Codecs.base64Decode(tmpbuf);
			} else if (encoding.equals("quoted-printable")) {
				tmpbuf = Codecs.quotedpDecode(tmpbuf);
			}
		}
		try {
			String tmpstr
				= new String(tmpbuf, charset2encode("from_" + charset));
			if (tmpstr.length() > 0 && tmpstr.charAt(0) == '.')
				tmpstr = tmpstr.substring(1);
			tmpstr = StringReplacer.replaceString(tmpstr, CRLF+".", CRLF);
			body.append(Cp932.toCp932(Text.getJavaString(tmpstr)));
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
	}
	
	class MessagePosition {
		long offset = 0, size = 0;
		
		public MessagePosition(long aoff, long asize) {
			offset = aoff;
			size = asize;
		}
		
		public MessagePosition() {
		}
		
		public void setOffset(long aoff) {
			offset = aoff;
		}
		
		public void setSize(long asize) {
			size = asize;
		}
		
		public long getOffset() {
			return offset;
		}
		
		public long getSize() {
			return size;
		}
	}
	
	boolean isBoundaryLine(byte[] src, byte[] b) {
		if (src == null)
			return true;
		try {
			if (src[0] != '-' || src[1] != '-')
				return false;
			if (src.length != b.length + 4 && src.length != b.length + 6)
				return false;
			for (int i = 0; i < b.length; i++)
				if (src[i+2] != b[i])
					return false;
		} catch (ArrayIndexOutOfBoundsException ex) {
			return false;
		}
		return true;
	}
	
	MessagePosition[] getMultipartPosition(byte[] boundary) {
		Vector positions = new Vector();
		long tmpoffset = offset, tmpsize = 0;
		try {
			GetLine gl = new GetLine(new BufferedInputStream(
				new FileInputStream(f)), offset, (int) size);
			byte[] tmpbuf;
			while (!isBoundaryLine((tmpbuf = gl.getLine()), boundary)
				&& tmpbuf != null)
				tmpoffset += tmpbuf.length;
			if (tmpbuf != null)
				tmpoffset += tmpbuf.length;
			MessagePosition msgpos;
			while (tmpbuf != null) {
				tmpbuf = gl.getLine();
				if (isBoundaryLine(tmpbuf, boundary)) {
					if (tmpbuf == null
						|| (tmpbuf[boundary.length + 2] == '-'
						&& tmpbuf[boundary.length + 3] == '-'))
						break;
					msgpos = new MessagePosition(tmpoffset, tmpsize);
					positions.addElement(msgpos);
					tmpoffset += tmpsize + tmpbuf.length;
					tmpsize = 0;
				} else {
					tmpsize += tmpbuf.length;
				}
			}
			msgpos = new MessagePosition(tmpoffset, tmpsize);
			positions.addElement(msgpos);
			gl.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		MessagePosition[] mps = new MessagePosition[positions.size()];
		positions.copyInto(mps);
		return mps;
	}
	
	void multipartProc(String subtype, HeaderParser hp) {
		byte[] boundary;
		try {
			String tmpstr = hp.getParam("boundary");
			if (tmpstr == null) {
				System.out.println("Multipart, but no boundary");
				return;
			}
			boundary = tmpstr.getBytes("8859_1");
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			return;
		}
		MessagePosition[] mps = getMultipartPosition(boundary);
		for (int i = 0; i < mps.length; i++) {
			RecvMessage rm
				= new RecvMessage(p, f, mps[i].getOffset(), mps[i].getSize());
			rm.setVisibleHeaders(visibleheaders);
			rm.setColor(bodycolor);
			rm.setHeaderColor(headercolor);
			rm.setTextStyle(getTextStyle());
			rm.process();
			attachflag |= rm.getAttachFlag();
			int scrollbarthickness = 0;
			int[] listsizes = null;
			try {
				scrollbarthickness
					= Integer.parseInt(rc.getString("scrollbarThickness"));
				listsizes = CSV.cutInt(rc.getString("maillistSize"));
			} catch (MissingResourceException ex) {}
			int[] tmpsizes = CSV.cutInt(rc.getString("separatorSize"));
			if (listsizes != null)
				tmpsizes[0] = listsizes[0] - scrollbarthickness - 8;
			TextAttachment ta
				= new TextAttachment(new VHRBorder(tmpsizes[0], tmpsizes[1]));
			body.append(ta);
			body.append(Text.LINE_SEPARATOR_CHAR);
			body.append(rm.getText());
		}
	}
	
	void messageProc(String subtype, HeaderParser hp) {
		if (subtype.equals("rfc822")) {
			long tmpoffset = offset, tmpsize = size;
			try {
				LineBufferedInputStream lbin = new LineBufferedInputStream(
					new FileInputStream(f));
				lbin.skip(offset);
				byte[] tmpbuf;
				while ((tmpbuf = lbin.readLine()).length > 2) {
					if (tmpbuf.length == 0) {
						System.out.println("EOF!");
						return;
					} else {
						tmpoffset += tmpbuf.length;
						tmpsize -= tmpbuf.length;
					}
				}
				tmpoffset += tmpbuf.length;
				tmpsize -= tmpbuf.length;
				lbin.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				return;
			}
			RecvMessage rm = new RecvMessage(p, f, tmpoffset, tmpsize);
			rm.setVisibleHeaders(visibleheaders);
			rm.setColor(bodycolor);
			rm.setHeaderColor(headercolor);
			rm.setTextStyle(getTextStyle());
			rm.headersProc();
			rm.process();
			attachflag |= rm.getAttachFlag();
			body.append(rm.getText());
		} else {
			defaultProc(subtype, hp);
		}
	}
	
	protected VImage createVImage(String aname) {
		return new VImage(getClass().getResource(aname));
//		return new VImage(aname);
	}
	
	void unknownProc(String subtype, final HeaderParser hp) {
		TextStyle tmpstyle = body.getCurrentTextStyle();
		ClickableTextAction action = new ClickableTextAction("Attach File");
		action.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				FileDialog fd = new FileDialog(p);
				fd.setMode(FileDialog.SAVE);
				if (hp.getParam("name") != null)
					fd.setFile((String) hp.getParam("name"));
				fd.setVisible(true);
				if (fd.getFile() == null)
					return;
				AttachProcess ap = new AttachProcess(f, 
					new File(fd.getDirectory(), fd.getFile()), offset, size);
				ProgressBox pb
					= new ProgressBox(p, rc.getString("attachTitle"), true);
				pb.addProgressProcess(ap);
				pb.setVisible(true);
			}
		});
		tmpstyle = tmpstyle.deriveStyle(action);
		body.setTextStyle(tmpstyle);
		body.setColor(Color.blue);
		body.setUnderline(true);
		body.append(new VSpace(16, 16));
		body.append(createVImage("images/Morning.gif"));
		body.append((String) hp.getParam("name") + "(" + hp.getField() + ")");
		body.append("\n");
		body.setUnderline(false);
		attachflag = true;
	}
}
