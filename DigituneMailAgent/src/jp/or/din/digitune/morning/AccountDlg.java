/*
 * Account Dialog Class
 *		1998/04/30 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import org.ingrid.kazama.Cp932;
import jp.kyasu.awt.Checkbox;
import jp.kyasu.awt.Button;
import jp.kyasu.awt.Dialog;
import jp.kyasu.awt.Frame;
import jp.kyasu.awt.Label;
import jp.kyasu.awt.Panel;
import jp.kyasu.awt.TextField;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AccountDlg extends Dialog {
	LocalResource rc = LocalResource.getResource();
	Account ac = new Account();
	String oldname = "";
	boolean status = false;
	
	public AccountDlg(Frame p) {
		super(p, "", true);
		createWindow();
	}
	
	public AccountDlg(Frame p, Account a) {
		super(p, "", true);
		ac = a;
		oldname = ac.getName();
		createWindow();
	}
	
	public Account getAccount() {
		return ac;
	}
	
	public void setAccount(Account a) {
		ac = a;
	}
	
	public void setStatus(boolean as) {
		status = as;
	}
	
	public boolean getStatus() {
		return status;
	}

	private String convString(String astr) {
		return astr.replace('"', '\'');
	}
	
	private void createWindow() {
		setTitle(rc.getString("accountDlgTitle"));
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				setVisible(false);
				dispose();
			}
		});
		GridBagLayout gblayout = new GridBagLayout();
		setLayout(gblayout);
		Label lname = new Label(rc.getString("nameLabel"), Label.RIGHT);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(lname, gbc);
		add(lname);
		final TextField tname = new TextField(30);
		tname.setText(Cp932.toCp932(ac.getName()));
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(tname, gbc);
		add(tname);
		Label luser = new Label(rc.getString("userLabel"), Label.RIGHT);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(luser, gbc);
		add(luser);
		final TextField tuser = new TextField(30);
		tuser.setText(Cp932.toCp932(ac.getID()));
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(tuser, gbc);
		add(tuser);
		Label lpass = new Label(rc.getString("passwordLabel"), Label.RIGHT);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(lpass, gbc);
		add(lpass);
		final TextField tpass = new TextField(30);
		tpass.setEchoChar('*');
		tpass.setText(Cp932.toCp932(ac.getPassword()));
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(tpass, gbc);
		add(tpass);
		Label lsmtp = new Label(rc.getString("smtpLabel"), Label.RIGHT);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(lsmtp, gbc);
		add(lsmtp);
		final TextField tsmtp = new TextField(30);
		tsmtp.setText(Cp932.toCp932(ac.getSMTPServer()));
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(tsmtp, gbc);
		add(tsmtp);
		Label lpop3 = new Label(rc.getString("pop3Label"), Label.RIGHT);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(lpop3, gbc);
		add(lpop3);
		final TextField tpop3 = new TextField(30);
		tpop3.setText(Cp932.toCp932(ac.getPOPServer()));
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(tpop3, gbc);
		add(tpop3);
		Label lusername = new Label(rc.getString("usernameLabel"),Label.RIGHT);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(lusername, gbc);
		add(lusername);
		final TextField tusername = new TextField(30);
		tusername.setText(Cp932.toCp932(ac.getUsername()));
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(tusername, gbc);
		add(tusername);
		Label laddress = new Label(rc.getString("addressLabel"),Label.RIGHT);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(laddress, gbc);
		add(laddress);
		final TextField taddress = new TextField(30);
		taddress.setText(Cp932.toCp932(ac.getAddress()));
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 6;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(taddress, gbc);
		add(taddress);
		Label ldelete = new Label(rc.getString("deleteLabel"),Label.RIGHT);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(ldelete, gbc);
		add(ldelete);
		final Checkbox cdelete = new Checkbox(rc.getString("deleteStr"));
		cdelete.setState(ac.getDeleteState());
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 7;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(cdelete, gbc);
		add(cdelete);
		Panel p1 = new Panel(new FlowLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 8;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gblayout.setConstraints(p1, gbc);
		add(p1);
		Button ok = new Button(rc.getString("okLabel"));
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (tname.getText().equals("")) {
					Toolkit.getDefaultToolkit().beep();
					tname.requestFocus();
					return;
				}
				String tmpname = Cp932.toJIS(convString(tname.getText()));
				try {
					if (!tmpname.equals(oldname)
						&& new File(tmpname + ".mbx").exists()) {
						Toolkit.getDefaultToolkit().beep();
						tname.requestFocus();
						return;
					}	
					FileWriter fw = new FileWriter(tmpname + ".xxx.idx");
					fw.write("write test");
					fw.close();
					new File(tmpname + ".xxx.idx").delete();
				} catch (IOException ex) {
					Toolkit.getDefaultToolkit().beep();
					tname.requestFocus();
					return;
				}
				ac.setName(tmpname);
				if (tuser.getText().equals("")) {
					Toolkit.getDefaultToolkit().beep();
					tuser.requestFocus();
					return;
				}
				ac.setID(Cp932.toJIS(tuser.getText()));
				if (tpass.getText().equals("")) {
					Toolkit.getDefaultToolkit().beep();
					tpass.requestFocus();
					return;
				}
				ac.setPassword(tpass.getText());
				if (tsmtp.getText().equals("")) {
					Toolkit.getDefaultToolkit().beep();
					tsmtp.requestFocus();
					return;
				}
				ac.setSMTPServer(Cp932.toJIS(tsmtp.getText()));
				if (tpop3.getText().equals("")) {
					Toolkit.getDefaultToolkit().beep();
					tpop3.requestFocus();
					return;
				}
				ac.setPOPServer(Cp932.toJIS(tpop3.getText()));
				if (tusername.getText().equals("")) {
					Toolkit.getDefaultToolkit().beep();
					tpop3.requestFocus();
					return;
				}
				ac.setUsername(Cp932.toJIS(convString(tusername.getText())));
				if (taddress.getText().equals("")) {
					Toolkit.getDefaultToolkit().beep();
					taddress.requestFocus();
					return;
				}
				ac.setAddress(Cp932.toJIS(convString(taddress.getText())));
				ac.setDeleteState(cdelete.getState());
				/* Ooops! Account name changed! */
				if (!oldname.equals("") && !tmpname.equals(oldname)) {
					new File(oldname + ".mbx")
						.renameTo(new File(tmpname + ".mbx"));
					new File(oldname + ".flt")
						.renameTo(new File(tmpname + ".flt"));
					String[] tmpfolders = ac.getFolders();
					for (int i = 0; i < tmpfolders.length; i++)
						new File(oldname + "." + tmpfolders[i] + ".idx")
							.renameTo(new File(tmpname + "." + tmpfolders[i]
							+ ".idx"));
					String[] tmpstrs = { "New", "Reply", "Forward" };
					for (int i = 0; i < tmpstrs.length; i++)
						new File(oldname + "." + tmpstrs[i] + ".tpl")
							.renameTo(new File(tmpname + "." + tmpstrs[i]
							+ ".tpl"));
				}
				status = true;
				setVisible(false);
			}
		});
		p1.add(ok);
		Button cancel = new Button(rc.getString("cancelLabel"));
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		});
		p1.add(cancel);
		pack();
		Dimension scsize = getToolkit().getScreenSize();
		Dimension size = getSize();
		setLocation(scsize.width / 2 - size.width / 2
			, scsize.height / 2 - size.height / 2);
	}
	
	public static final void main(String[] args) {
		Frame p = new Frame();
		AccountDlg ad = new AccountDlg(p);
		ad.setVisible(true);
		System.out.println(ad.getAccount());
	}
}
