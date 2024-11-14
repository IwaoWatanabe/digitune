/*
 * Message Information class
 *		1998/03/15 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.kyasu.util.VArray;
import java.io.File;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;

public class MessageInfo {
	static final byte DELETEFLAG = 0x01;
	static final byte SEENFLAG = 0x02;
	static final byte ATTACHFLAG = 0x04;
	
	static final int SUBJECTLENGTH = 64;
	static final int FROMLENGTH = 64;
	static final int DATELENGTH = 32;
	static final int OFFSETLENGTH = 8;	/* sizeof (long); */
	static final int SIZELENGTH = 8;	/* sizeof (long); */
	static final int MSGID = 32;	/* message-id */
	static final int PARENTID = 32;	/* in-reply-to or reference */
	static final int PADDING = 256 - (SUBJECTLENGTH + FROMLENGTH + DATELENGTH
		+ OFFSETLENGTH + SIZELENGTH + MSGID + PARENTID + 1);
													/* +1 is flag byte. */
	static final int INDEXLENGTH = 256;
	
	byte flag = 0;
	String subject = "", from = "", date = "", parentid = "", msgid = "";
	long offset = 0, size = 0;
	
	private void readMessageInfo(InputStream in) {
		try {
			DataInputStream din = new DataInputStream(in);
			flag = (byte) din.readByte();
			byte[] tmpbuf = new byte[SUBJECTLENGTH];
			din.readFully(tmpbuf);
			subject = new String(tmpbuf, "UTF8").trim();
			tmpbuf = new byte[FROMLENGTH];
			din.readFully(tmpbuf);
			from = new String(tmpbuf, "UTF8").trim();
			tmpbuf = new byte[DATELENGTH];
			din.readFully(tmpbuf);
			date = new String(tmpbuf, "UTF8").trim();
			offset = din.readLong();
			size = din.readLong();
			tmpbuf = new byte[MSGID];
			din.readFully(tmpbuf);
			msgid = new String(tmpbuf, "UTF8").trim();
			tmpbuf = new byte[PARENTID];
			din.readFully(tmpbuf);
			parentid = new String(tmpbuf, "UTF8").trim();
			din.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public MessageInfo(InputStream in) {
		readMessageInfo(in);
	}
	
	public MessageInfo(InputStream in, int index) {
		try {
			in.skip((long) index * INDEXLENGTH);
			readMessageInfo(in);
		} catch (IOException ex) {}
	}
	
	public MessageInfo(RandomAccessFile din, int index) {
		try {
			long pos = INDEXLENGTH * index;
			if (pos < 0)
				pos = 0;
			else if (pos > din.length())
				pos = din.length() - INDEXLENGTH;
			din.seek(pos);
			flag = (byte) din.readByte();
			byte[] tmpbuf = new byte[SUBJECTLENGTH];
			din.readFully(tmpbuf);
			int i;
			for (i = 0; i < SUBJECTLENGTH; i++)
				if (tmpbuf[i] == 0)
					break;
			byte[] srcbuf = new byte[i];
			System.arraycopy(tmpbuf, 0, srcbuf, 0, i);
			subject = new String(srcbuf, "UTF8");
			tmpbuf = new byte[FROMLENGTH];
			din.readFully(tmpbuf);
			for (i = 0; i < FROMLENGTH; i++)
				if (tmpbuf[i] == 0)
					break;
			srcbuf = new byte[i];
			System.arraycopy(tmpbuf, 0, srcbuf, 0, i);
			from = new String(srcbuf, "UTF8");
			tmpbuf = new byte[DATELENGTH];
			din.readFully(tmpbuf);
			for (i = 0; i < DATELENGTH; i++)
				if (tmpbuf[i] == 0)
					break;
			srcbuf = new byte[i];
			System.arraycopy(tmpbuf, 0, srcbuf, 0, i);
			date = new String(srcbuf, "UTF8");
			offset = din.readLong();
			size = din.readLong();
			tmpbuf = new byte[MSGID];
			din.readFully(tmpbuf);
			for (i = 0; i < MSGID; i++)
				if (tmpbuf[i] == 0)
					break;
			srcbuf = new byte[i];
			System.arraycopy(tmpbuf, 0, srcbuf, 0, i);
			msgid = new String(srcbuf, "UTF8");
			tmpbuf = new byte[PARENTID];
			din.readFully(tmpbuf);
			for (i = 0; i < PARENTID; i++)
				if (tmpbuf[i] == 0)
					break;
			srcbuf = new byte[i];
			System.arraycopy(tmpbuf, 0, srcbuf, 0, i);
			parentid = new String(srcbuf, "UTF8");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public MessageInfo(byte af, String asub, String afrom, String adate
		, long aoff, long asize) {
		flag = af;
		subject = asub;
		from = afrom;
		date = adate;
		offset = aoff;
		size = asize;
	}

	public MessageInfo(byte af, String asub, String afrom, String adate
		, long aoff, long asize, String amsgid, String aparent) {
		flag = af;
		subject = asub;
		from = afrom;
		date = adate;
		offset = aoff;
		size = asize;
		msgid = amsgid;
		parentid = aparent;
	}
	
	public MessageInfo() {
	}
	
	public void writeMessageInfo(OutputStream out) throws IOException {
		DataOutputStream dout = new DataOutputStream(out);
		dout.writeByte(flag);
		byte[] tmpbuf = new byte[SUBJECTLENGTH], srcbuf;
		srcbuf = subject.getBytes("UTF8");
		System.arraycopy(srcbuf, 0, tmpbuf, 0
			, (srcbuf.length < tmpbuf.length) ? srcbuf.length : tmpbuf.length);
		dout.write(tmpbuf, 0, tmpbuf.length);
		tmpbuf = new byte[FROMLENGTH];
		srcbuf = from.getBytes("UTF8");
		System.arraycopy(srcbuf, 0, tmpbuf, 0
			, (srcbuf.length < tmpbuf.length) ? srcbuf.length : tmpbuf.length);
		dout.write(tmpbuf, 0, tmpbuf.length);
		tmpbuf = new byte[DATELENGTH];
		srcbuf = date.getBytes("UTF8");
		System.arraycopy(srcbuf, 0, tmpbuf, 0
			, (srcbuf.length < tmpbuf.length) ? srcbuf.length : tmpbuf.length);
		dout.write(tmpbuf, 0, tmpbuf.length);
		dout.writeLong(offset);
		dout.writeLong(size);
		tmpbuf = new byte[MSGID];
		srcbuf = msgid.getBytes("UTF8");
		System.arraycopy(srcbuf, 0, tmpbuf, 0
			, (srcbuf.length < tmpbuf.length) ? srcbuf.length : tmpbuf.length);
		dout.write(tmpbuf, 0, tmpbuf.length);
		tmpbuf = new byte[PARENTID];
		srcbuf = parentid.getBytes("UTF8");
		System.arraycopy(srcbuf, 0, tmpbuf, 0
			, (srcbuf.length < tmpbuf.length) ? srcbuf.length : tmpbuf.length);
		dout.write(tmpbuf, 0, tmpbuf.length);
		tmpbuf = new byte[PADDING];
		dout.write(tmpbuf, 0, tmpbuf.length);
		dout.close();
	}
	
	public void writeMessageInfo(File file, int index)
		throws IOException {
		RandomAccessFile dout = new RandomAccessFile(file, "rw");
		writeMessageInfo(dout, index);
		dout.close();
	}
	
	public void writeMessageInfo(RandomAccessFile dout, int index)
		throws IOException {
		long pos = INDEXLENGTH * index;
		if (pos < 0)
			pos = 0;
		else if (pos > dout.length())
			pos = dout.length();
		dout.seek(pos);
		dout.writeByte(flag);
		byte[] tmpbuf = new byte[SUBJECTLENGTH], srcbuf;
		srcbuf = subject.getBytes("UTF8");
		System.arraycopy(srcbuf, 0, tmpbuf, 0
			, (srcbuf.length < tmpbuf.length) ? srcbuf.length : tmpbuf.length);
		dout.write(tmpbuf, 0, tmpbuf.length);
		tmpbuf = new byte[FROMLENGTH];
		srcbuf = from.getBytes("UTF8");
		System.arraycopy(srcbuf, 0, tmpbuf, 0
			, (srcbuf.length < tmpbuf.length) ? srcbuf.length : tmpbuf.length);
		dout.write(tmpbuf, 0, tmpbuf.length);
		tmpbuf = new byte[DATELENGTH];
		srcbuf = date.getBytes("UTF8");
		System.arraycopy(srcbuf, 0, tmpbuf, 0
			, (srcbuf.length < tmpbuf.length) ? srcbuf.length : tmpbuf.length);
		dout.write(tmpbuf, 0, tmpbuf.length);
		dout.writeLong(offset);
		dout.writeLong(size);
		tmpbuf = new byte[MSGID];
		srcbuf = msgid.getBytes("UTF8");
		System.arraycopy(srcbuf, 0, tmpbuf, 0
			, (srcbuf.length < tmpbuf.length) ? srcbuf.length : tmpbuf.length);
		dout.write(tmpbuf, 0, tmpbuf.length);
		tmpbuf = new byte[PARENTID];
		srcbuf = parentid.getBytes("UTF8");
		System.arraycopy(srcbuf, 0, tmpbuf, 0
			, (srcbuf.length < tmpbuf.length) ? srcbuf.length : tmpbuf.length);
		dout.write(tmpbuf, 0, tmpbuf.length);
		tmpbuf = new byte[PADDING];
		dout.write(tmpbuf, 0, tmpbuf.length);
	}
	
	public static int length() {
		return INDEXLENGTH;
	}
	
	public void setDeleteFlag() {
		flag |= DELETEFLAG;
	}
	
	public void clearDeleteFlag() {
		if ((flag & DELETEFLAG) != 0)
			flag ^= DELETEFLAG;
	}
	
	public boolean getDeleteFlag() {
		if ((flag & DELETEFLAG) != 0)
			return true;
		else
			return false;
	}
	
	public void setSeenFlag() {
		flag |= SEENFLAG;
	}
	
	public void clearSeenFlag() {
		if ((flag & SEENFLAG) != 0)
			flag ^= SEENFLAG;
	}
	
	public boolean getSeenFlag() {
		if ((flag & SEENFLAG) != 0)
			return true;
		else
			return false;
	}
	
	public void setAttachFlag() {
		flag |= ATTACHFLAG;
	}
	
	public void clearAttachFlag() {
		if ((flag & ATTACHFLAG) != 0)
			flag ^= ATTACHFLAG;
	}
	
	public boolean getAttachFlag() {
		if ((flag & ATTACHFLAG) != 0)
			return true;
		else
			return false;
	}
	
	public void setFlag(byte a) {
		flag = a;
	}
	
	public byte getFlag() {
		return flag;
	}
	
	public void setSubject(String a) {
		subject = a;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setFrom(String a) {
		from = a;
	}
	
	public String getFrom() {
		return from;
	}
	
	public void setDate(String a) {
		date = a;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setOffset(long a) {
		offset = a;
	}
	
	public long getOffset() {
		return offset;
	}
	
	public void setSize(long a) {
		size = a;
	}
	
	public long getSize() {
		return size;
	}

	public void setMessageId(String a) {
		msgid = a;
	}

	public String getMessageId() {
		return msgid;
	}

	public void setParentId(String a) {
		parentid = a;
	}

	public String getParentId() {
		return parentid;
	}
}
