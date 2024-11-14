/*
 * Templete Dialog Class
 *		1998/05/13 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.kyasu.awt.Button;
import jp.kyasu.awt.Dialog;
import jp.kyasu.awt.Frame;
import jp.kyasu.awt.Choice;
import jp.kyasu.awt.Checkbox;
import jp.kyasu.awt.Label;
import jp.kyasu.awt.Panel;
import jp.kyasu.awt.TextField;
import jp.kyasu.awt.TextArea;
import org.ingrid.kazama.Cp932;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

public class TempleteDlg extends Dialog {
	LocalResource rc = LocalResource.getResource();
	String[] categories = { "New", "Reply", "Forward" };
	String[] templetes = new String[3];
	int oldindex = 0;
	Account ac = new Account();
	
	public TempleteDlg(Frame p) {
		super(p, "", true);
		setDefaultTemplete();
		createWindow();
	}
	
	public TempleteDlg(Frame p, Account a) {
		super(p, "", true);
		ac = a;
		setDefaultTemplete();
		createWindow();
	}
	
	public Account getAccount() {
		return ac;
	}
	
	public void setAccount(Account a) {
		ac = a;
	}
	
	private void setDefaultTemplete() {
		for (int i = 0; i < categories.length; i++) {
			StringBuffer strbuf = new StringBuffer();
			try {
				String tmpname = ac.getName()+"."+categories[i]+".tpl";
				InputStream tmpin;
				if (new File(tmpname).exists())
					tmpin = new FileInputStream(tmpname);
				else
					tmpin = getClass().getResourceAsStream(
						"text/" + categories[i] + ".tpl");
//					tmpin = new FileInputStream(
//						"text/" + categories[i] + ".tpl");
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(tmpin, "UTF8"));
				String tmpstr;
				while ((tmpstr = reader.readLine()) != null)
					strbuf.append(tmpstr + "\n");
				reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			templetes[i] = new String(strbuf);
		}
	}
	
	private void createWindow() {
		setTitle(rc.getString("templeteDlgTitle"));
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				setVisible(false);
				dispose();
			}
		});
		setLayout(new BorderLayout());
		Panel p1 = new Panel(new FlowLayout());
		add("North", p1);
		Label lcate = new Label(rc.getString("categoryLabel"), Label.RIGHT);
		p1.add("Center", lcate);
		final TextArea body = new TextArea();
		body.setText(Cp932.toCp932(templetes[0]));
		add("Center", body);
		final Choice cate = new Choice();
		cate.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				templetes[oldindex] = Cp932.toJIS(body.getText());
				body.setText(Cp932.toJIS(templetes[cate.getSelectedIndex()]));
				oldindex = cate.getSelectedIndex();
			}
		});
		cate.add(rc.getString("cateNew"));
		cate.add(rc.getString("cateReply"));
		cate.add(rc.getString("cateForward"));
		p1.add("Center", cate);
		Panel p3 = new Panel(new FlowLayout());
		add("South", p3);
		Label lquoter = new Label(rc.getString("quoterLabel"), Label.RIGHT);
		p3.add("Center", lquoter);
		final TextField tquoter
			= new TextField(Cp932.toCp932(rc.getString("quoter")));
		p3.add("Center", tquoter);
		Button ok = new Button(rc.getString("okLabel"));
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				rc.setString("quoter", Cp932.toJIS(tquoter.getText()));
				templetes[oldindex] = Cp932.toJIS(body.getText());
				String[] tmpstrs = new String[categories.length];
				System.arraycopy(templetes, 0, tmpstrs, 0, categories.length);
				setDefaultTemplete();
				for (int i = 0; i < categories.length; i++) {
					if (!tmpstrs[i].equals(templetes[i])) {
						try {
							BufferedWriter writer = new BufferedWriter(
								new OutputStreamWriter(new FileOutputStream(
								ac.getName() + "." + categories[i] + ".tpl")
								, "UTF8"));
							writer.write(tmpstrs[i]);
							writer.close();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				}
				setVisible(false);
				dispose();
			}
		});
		p3.add("Center", ok);
		Button cancel = new Button(rc.getString("cancelLabel"));
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
				dispose();
			}
		});
		p3.add("Center", cancel);
		pack();
		Dimension scsize = getToolkit().getScreenSize();
		Dimension size = getSize();
		setLocation(scsize.width / 2 - size.width / 2
			, scsize.height / 2 - size.height / 2);
	}
	
	public static final void main(String[] args) {
		Frame p = new Frame();
		TempleteDlg td = new TempleteDlg(p);
		td.setVisible(true);
	}
}
