/*
 * Main Window Class
 *      1998/03/10 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import org.ingrid.kazama.Cp932;
import jp.or.din.digitune.gui.MsgBox;
import jp.or.din.digitune.gui.InputBox;
import jp.or.din.digitune.gui.SolidPanel;
import jp.or.din.digitune.gui.ProgressBox;
import jp.or.din.digitune.gui.ProgressProcess;
import jp.or.din.digitune.gui.SequenceProgressProcess;
import jp.or.din.digitune.gui.YesNoBox;
import jp.or.din.digitune.gui.VariableEvent;
import jp.or.din.digitune.gui.VariableListener;
import jp.or.din.digitune.util.CSV;
import jp.or.din.digitune.util.ResourceProperties;
import jp.or.din.digitune.util.StringReplacer;
import jp.kyasu.awt.Button;
import jp.kyasu.awt.Choice;
import jp.kyasu.awt.Frame;
import jp.kyasu.awt.Label;
import jp.kyasu.awt.List;
import jp.kyasu.awt.Panel;
import jp.kyasu.awt.NativePanel;
import jp.kyasu.awt.TableList;
import jp.kyasu.awt.TextArea;
import jp.kyasu.awt.SplitPanel;
import jp.kyasu.awt.PopupWindow;
import jp.kyasu.awt.text.KeyAction;
import jp.kyasu.awt.text.Keymap;
import jp.kyasu.awt.text.TextController;
import jp.kyasu.editor.ColorChooser;
import jp.kyasu.graphics.Text;
import jp.kyasu.graphics.TextBuffer;
import jp.kyasu.graphics.TextStyle;
import jp.kyasu.graphics.RichText;
import jp.kyasu.graphics.VActiveButton;
import jp.kyasu.graphics.VImage;
import jp.kyasu.graphics.VSpace;
import jp.kyasu.util.VArray;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.CheckboxMenuItem;
import java.awt.MenuBar;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.PopupMenu;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.text.DateFormat;

public class MainWindow extends Frame {
    LocalResource rc = LocalResource.getResource();
    Vector accounts = new Vector();
    RefreshMailList refml = null;
    boolean dontchange = false;
    int lastindex = 0;
    String recentfrom = "", currentfolder = "";
    Vector msginfo = new Vector();
    VArray indexes = new VArray(int.class), spacings = new VArray(int.class);
    
    VImage chkimg, attimg, inboximg, draftimg, unsendimg, sentimg, trashimg
		, folderimg;
    VSpace blkimg = new VSpace(12, 12);
    TextStyle defaultstyle = new TextStyle("sanserif", Font.PLAIN, 12);
    
    Choice achoice = null;
    TableList folders = null;
    TableList mails = null;
    SolidPanel p1 = null;
    SolidPanel mailpanel = null;
    Panel folderpanel = null;
    TextArea body = null;
    Label statusline = null;
    
    Menu movemail, copymail, pmovemail, pcopymail;
    
    public MainWindow() {
        super();
        loadProperties();
        createWindow();
    }
    
    private Account currentAccount() {
        int tmpidx = achoice.getSelectedIndex();
        return (Account) accounts.elementAt(tmpidx);
    }

	private String selectedFolder() {
		return folders.getItem(folders.getSelectedIndex(), 0).substring(1);
	}

	private void setAccounts(BufferedReader ar) throws IOException {
		String tmpstr;
		while ((tmpstr = ar.readLine()) != null) {
			Account tmpac = new Account(tmpstr);
			accounts.addElement(tmpac);
		}
	}
    
    private void loadProperties() {
		if (new File(rc.getString("accountsFilename")).exists()) {
			try {
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(
					new FileInputStream(rc.getString("accountsFilename"))
					, "UTF8"));
				setAccounts(reader);
				reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else if (new File(rc.getString("accountFilename")).exists()) {
        	try {
           		BufferedReader reader = new BufferedReader(
                	new FileReader(rc.getString("accountFilename")));
				setAccounts(reader);
            	reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
            AccountDlg ad = new AccountDlg(MainWindow.this);
            while (!ad.getStatus()) {
                Toolkit.getDefaultToolkit().beep();
                ad.setVisible(true);
            }
            accounts.addElement(ad.getAccount());
			ad.dispose();
		}
    }
    
    protected void saveProperties() {
        rc.save();
        try {
            BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(
				new FileOutputStream(rc.getString("accountsFilename"))
				, "UTF8"));
            for (Enumeration enum = accounts.elements()
                ; enum.hasMoreElements(); ) {
				String tmpstr = ((Account) enum.nextElement()).toString();
                writer.write(tmpstr, 0, tmpstr.length());
				writer.newLine();
			}
            writer.close();
        } catch (IOException ex) {
			ex.printStackTrace();
		}
    }
    
    protected void saveGUIPref() {
        int[] widths = new int[2];
        if (folders != null) {
            widths = folders.getModel().getTextList().getColumnWidths();
            rc.setString("folderlistWidths", CSV.toString(widths));
        }
        if (mails != null) {
            widths = mails.getModel().getTextList().getColumnWidths();
            rc.setString("maillistWidths", CSV.toString(widths));
        }
        int[] tmpsize = new int[2];
        tmpsize[0] = p1.getSize().width;
        tmpsize[1] = p1.getSize().height;
        rc.setString("folderlistSize", CSV.toString(tmpsize));
        tmpsize[0] = mailpanel.getSize().width;
        tmpsize[1] = mailpanel.getSize().height;
        rc.setString("maillistSize", CSV.toString(tmpsize));
        tmpsize[0] = this.getLocation().x;
        tmpsize[1] = this.getLocation().y;
        rc.setString("mainwindowLocation", CSV.toString(tmpsize));
        tmpsize[0] = this.getSize().width;
        tmpsize[1] = this.getSize().height;
        rc.setString("mainwindowSize", CSV.toString(tmpsize));
    }
    
    protected void waitCancel() {
        while (refml != null && refml.isAlive()) {
            refml.cancel();
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException ex) {}
        }
    }
    
    protected class ActionEventCatcher implements ActionListener {
        ActionListener al;
        public ActionEventCatcher(ActionListener aal) {
            al = aal;
        }
        
        public void actionPerformed(ActionEvent ae) {
            waitCancel();
            al.actionPerformed(ae);
        }
    }
    
    protected class ItemEventCatcher implements ItemListener {
        ItemListener il;
        public ItemEventCatcher(ItemListener ail) {
            il = ail;
        }
        
        public void itemStateChanged(ItemEvent ie) {
            waitCancel();
            il.itemStateChanged(ie);
        }
    }
    
    protected void allSeen() {
        dontchange = true;
		String allseenstr = rc.getString("setAllSeenFlag");
        for (int i = 0; i < mails.getItemCount(); i++) {
            statusline.setText(allseenstr + (i + 1) + "/"
				+ mails.getItemCount());
            MessageInfo tmpmi = (MessageInfo) msginfo.elementAt(i);
            if (!tmpmi.getSeenFlag()) {
                int findex = indexes.getInt(i);
                tmpmi.setSeenFlag();
                msginfo.setElementAt(tmpmi, i);
                mails.replaceItem(createMailList(tmpmi, spacings.getInt(i)), i);
                FolderManager fm
                    = new FolderManager(currentAccount(), currentfolder);
                fm.writeMessageInfo(tmpmi, findex);
                fm.close();
            }
        }
        dontchange = false;
        statusline.setText(" ");
    }
    
    protected class KeyProcessor extends KeyAdapter {
		public void keyReleased(KeyEvent ke) {
            if (ke.getKeyCode() == KeyEvent.VK_DELETE)
                moveMail(rc.getString("trash"), true);
		}

        public void keyTyped(KeyEvent ke) {
            if (ke.getKeyChar() == ' ') {
//              waitCancel();
                if (lastindex != mails.getSelectedIndex()) {
                    refreshBody();
                    return;
                }
                if (body.getRichText().length() == body.getCaretPosition()) {
                    int tmpidx = mails.getSelectedIndex();
                    if (tmpidx < 0)
                        tmpidx = lastindex;
                    if (tmpidx < mails.getItemCount() - 1) {
                        mails.deselect(tmpidx++);
                        mails.select(tmpidx);
                        mails.makeVisible(tmpidx);
                        refreshBody();
                    } else if (folders.getSelectedIndex()
						< folders.getItemCount() - 1) {
        				if (refml != null && refml.isAlive()) {
            				refml.cancel();
							return;
						}
						tmpidx = folders.getSelectedIndex();
                    	folders.deselect(tmpidx++);
                    	folders.select(tmpidx);
                    	folders.makeVisible(tmpidx);
                    	refml = refreshMailList(
							folders.getItem(tmpidx, 0).substring(1));
                    	refml.start();
					}
                    return;
                }
                body.getController().next_page();
                if (body.getCaretPosition() == 0) {
                    int tmpidx = mails.getSelectedIndex();
                    if (tmpidx < 0)
                        tmpidx = lastindex;
                    if (tmpidx < mails.getItemCount() - 1) {
                        mails.deselect(tmpidx++);
                        mails.select(tmpidx);
                        mails.makeVisible(tmpidx);
                        refreshBody();
                    } else if (folders.getSelectedIndex()
						< folders.getItemCount() - 1) {
        				if (refml != null && refml.isAlive()) {
            				refml.cancel();
							return;
						}
						tmpidx = folders.getSelectedIndex();
                    	folders.deselect(tmpidx++);
                    	folders.select(tmpidx);
                    	folders.makeVisible(tmpidx);
                    	refml = refreshMailList(
                        	folders.getItem(tmpidx, 0).substring(1));
                    	refml.start();
                    }
                    return;
                }
            } else if (ke.getKeyChar() == 'b') {
                if (body.getCaretPosition() == 0) {
                    int tmpidx = mails.getSelectedIndex();
                    if (tmpidx < 0)
                        tmpidx = lastindex;
                    if (tmpidx > 0) {
                        mails.deselect(tmpidx--);
                        mails.select(tmpidx);
                        mails.makeVisible(tmpidx);
                        refreshBody();
                    }
                    return;
                }
                body.getController().previous_page();
            } else if (ke.getKeyChar() == 'C') {
//              waitCancel();
                allSeen();
            } else if (ke.getKeyChar() == 'n' | ke.getKeyChar() == 'j') {
//              waitCancel();
                int tmpidx = mails.getSelectedIndex();
                if (tmpidx < 0)
                    tmpidx = lastindex;
                if (tmpidx < mails.getItemCount() - 1) {
                    mails.deselect(tmpidx++);
                    mails.select(tmpidx);
                    mails.makeVisible(tmpidx);
                    refreshBody();
                }
            } else if (ke.getKeyChar() == 'p' | ke.getKeyChar() == 'k') {
//              waitCancel();
                int tmpidx = mails.getSelectedIndex();
                if (tmpidx < 0)
                    tmpidx = lastindex;
                if (tmpidx > 0) {
                    mails.deselect(tmpidx--);
                    mails.select(tmpidx);
                    mails.makeVisible(tmpidx);
                    refreshBody();
                }
            } else if (ke.getKeyChar() == 'N' | ke.getKeyChar() == 'J') {
                int tmpidx = folders.getSelectedIndex();
                if (tmpidx < folders.getItemCount() - 1) {
                    waitCancel();
                    folders.deselect(tmpidx++);
                    folders.select(tmpidx);
                    folders.makeVisible(tmpidx);
                    refml = refreshMailList(
                        folders.getItem(tmpidx, 0).substring(1));
                    refml.start();
                }
            } else if (ke.getKeyChar() == 'P' | ke.getKeyChar() == 'K') {
                int tmpidx = folders.getSelectedIndex();
                if (tmpidx > 0) {
                    waitCancel();
                    folders.deselect(tmpidx--);
                    folders.select(tmpidx);
                    folders.makeVisible(tmpidx);
                    refml = refreshMailList(
                        folders.getItem(tmpidx, 0).substring(1));
                    refml.start();
                }
            } else {
				waitCancel();
			}
        }
    }
    
	protected Color string2Color(String src) {
		String[] tmpstrs = CSV.cut(src);
		return new Color(Integer.parseInt(tmpstrs[0])
			, Integer.parseInt(tmpstrs[1]), Integer.parseInt(tmpstrs[2]));
	}

    protected String color2String(Color tmpcolor) {
        StringBuffer strbuf = new StringBuffer();
        strbuf.append(Integer.toString(tmpcolor.getRed()) + ",");
        strbuf.append(Integer.toString(tmpcolor.getGreen()) + ",");
        strbuf.append(Integer.toString(tmpcolor.getBlue()));
        return new String(strbuf);
    }

    protected TextStyle string2TextStyle(String src) {
        String[] tmpstrs = CSV.cut(src);
        return new TextStyle(tmpstrs[0], Integer.parseInt(tmpstrs[1])
            , Integer.parseInt(tmpstrs[2]));
    }
    
    protected String textStyle2String(TextStyle tmpstyle) {
        StringBuffer strbuf = new StringBuffer();
        strbuf.append(tmpstyle.getFont().getName() + ",");
        strbuf.append(Integer.toString(tmpstyle.getFont().getStyle()) + ",");
        strbuf.append(Integer.toString(tmpstyle.getFont().getSize()) + ",");
        return new String(strbuf);
    }

	protected Menu createFolderListPrefMenu() {
		Menu menu = new Menu(rc.getString("folderlistprefMenu"));
		MenuItem f = new MenuItem(rc.getString("folderlistforegroundMenu"));
		f.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final PopupWindow pw = new PopupWindow(MainWindow.this);
				ColorChooser cc = new ColorChooser(
					string2Color(rc.getString("folderlistForeground")));
				cc.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ie) {
						Color tmpcolor = (Color) ie.getItem();
						rc.setString("folderlistForeground"
							, color2String(tmpcolor));
						folders.setForeground(tmpcolor);
						folders.repaint();
						achoice.setForeground(tmpcolor);
						achoice.repaint();
						pw.hide();
						pw.dispose();
					}
				});
				pw.add("Center", cc);
				pw.pack();
				pw.show(folders, 0, 0);
			}
		});
		menu.add(f);
		MenuItem b = new MenuItem(rc.getString("folderlistbackgroundMenu"));
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final PopupWindow pw = new PopupWindow(MainWindow.this);
				ColorChooser cc = new ColorChooser(
					string2Color(rc.getString("folderlistBackground")));
				cc.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ie) {
						Color tmpcolor = (Color) ie.getItem();
						rc.setString("folderlistBackground"
							, color2String(tmpcolor));
						folders.setBackground(tmpcolor);
						folders.repaint();
						achoice.setForeground(tmpcolor);
						achoice.repaint();
						pw.hide();
						pw.dispose();
					}
				});
				pw.add("Center", cc);
				pw.pack();
				pw.show(folders, 0, 0);
			}
		});
		menu.add(b);
		MenuItem sf = new MenuItem(
			rc.getString("folderlistselectionforegroundMenu"));
		sf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final PopupWindow pw = new PopupWindow(MainWindow.this);
				ColorChooser cc = new ColorChooser(string2Color(
					rc.getString("folderlistSelectionForeground")));
				cc.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ie) {
						Color tmpcolor = (Color) ie.getItem();
						rc.setString("folderlistSelectionForeground"
							, color2String(tmpcolor));
						folders.setSelectionForeground(tmpcolor);
						folders.repaint();
						achoice.setForeground(tmpcolor);
						achoice.repaint();
						pw.hide();
						pw.dispose();
					}
				});
				pw.add("Center", cc);
				pw.pack();
				pw.show(folders, 0, 0);
			}
		});
		menu.add(sf);
		MenuItem sb = new MenuItem(
			rc.getString("folderlistselectionbackgroundMenu"));
		sb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final PopupWindow pw = new PopupWindow(MainWindow.this);
				ColorChooser cc = new ColorChooser(string2Color(
					rc.getString("folderlistSelectionBackground")));
				cc.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ie) {
						Color tmpcolor = (Color) ie.getItem();
						rc.setString("folderlistSelectionBackground"
							, color2String(tmpcolor));
						folders.setSelectionBackground(tmpcolor);
						folders.repaint();
						achoice.setForeground(tmpcolor);
						achoice.repaint();
						pw.hide();
						pw.dispose();
					}
				});
				pw.add("Center", cc);
				pw.pack();
				pw.show(folders, 0, 0);
			}
		});
		menu.add(sb);
		return menu;
	}

	protected Menu createMailListPrefMenu() {
		Menu menu = new Menu(rc.getString("maillistprefMenu"));
		MenuItem f = new MenuItem(rc.getString("maillistforegroundMenu"));
		f.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final PopupWindow pw = new PopupWindow(MainWindow.this);
				ColorChooser cc = new ColorChooser(
					string2Color(rc.getString("maillistForeground")));
				cc.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ie) {
						Color tmpcolor = (Color) ie.getItem();
						rc.setString("maillistForeground"
							, color2String(tmpcolor));
						mails.setForeground(tmpcolor);
						mails.repaint();
						pw.hide();
						pw.dispose();
					}
				});
				pw.add("Center", cc);
				pw.pack();
				pw.show(mails, 0, 0);
			}
		});
		menu.add(f);
		MenuItem b = new MenuItem(rc.getString("maillistbackgroundMenu"));
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final PopupWindow pw = new PopupWindow(MainWindow.this);
				ColorChooser cc = new ColorChooser(
					string2Color(rc.getString("maillistBackground")));
				cc.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ie) {
						Color tmpcolor = (Color) ie.getItem();
						rc.setString("maillistBackground"
							, color2String(tmpcolor));
						mails.setBackground(tmpcolor);
						mails.repaint();
						pw.hide();
						pw.dispose();
					}
				});
				pw.add("Center", cc);
				pw.pack();
				pw.show(mails, 0, 0);
			}
		});
		menu.add(b);
		MenuItem sf = new MenuItem(
			rc.getString("maillistselectionforegroundMenu"));
		sf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final PopupWindow pw = new PopupWindow(MainWindow.this);
				ColorChooser cc = new ColorChooser(string2Color(
					rc.getString("maillistSelectionForeground")));
				cc.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ie) {
						Color tmpcolor = (Color) ie.getItem();
						rc.setString("maillistSelectionForeground"
							, color2String(tmpcolor));
						mails.setSelectionForeground(tmpcolor);
						mails.repaint();
						pw.hide();
						pw.dispose();
					}
				});
				pw.add("Center", cc);
				pw.pack();
				pw.show(mails, 0, 0);
			}
		});
		menu.add(sf);
		MenuItem sb = new MenuItem(
			rc.getString("maillistselectionbackgroundMenu"));
		sb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final PopupWindow pw = new PopupWindow(MainWindow.this);
				ColorChooser cc = new ColorChooser(string2Color(
					rc.getString("maillistSelectionBackground")));
				cc.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ie) {
						Color tmpcolor = (Color) ie.getItem();
						rc.setString("maillistSelectionBackground"
							, color2String(tmpcolor));
						mails.setSelectionBackground(tmpcolor);
						mails.repaint();
						pw.hide();
						pw.dispose();
					}
				});
				pw.add("Center", cc);
				pw.pack();
				pw.show(mails, 0, 0);
			}
		});
		menu.add(sb);
		return menu;
	}

	protected Menu createMailBodyPrefMenu() {
		Menu menu = new Menu(rc.getString("mailbodyprefMenu"));
		MenuItem f = new MenuItem(rc.getString("mailbodyforegroundMenu"));
		f.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final PopupWindow pw = new PopupWindow(MainWindow.this);
				ColorChooser cc = new ColorChooser(
					string2Color(rc.getString("mailbodyForeground")));
				cc.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ie) {
						Color tmpcolor = (Color) ie.getItem();
						rc.setString("mailbodyForeground"
							, color2String(tmpcolor));
						pw.hide();
						pw.dispose();
					}
				});
				pw.add("Center", cc);
				pw.pack();
				pw.show(body, 0, 0);
			}
		});
		menu.add(f);
		MenuItem b = new MenuItem(rc.getString("mailbodybackgroundMenu"));
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final PopupWindow pw = new PopupWindow(MainWindow.this);
				ColorChooser cc = new ColorChooser(
					string2Color(rc.getString("mailbodyBackground")));
				cc.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ie) {
						Color tmpcolor = (Color) ie.getItem();
						rc.setString("mailbodyBackground"
							, color2String(tmpcolor));
						body.setBackground(tmpcolor);
						body.repaint();
						pw.hide();
						pw.dispose();
					}
				});
				pw.add("Center", cc);
				pw.pack();
				pw.show(body, 0, 0);
			}
		});
		menu.add(b);
		MenuItem sf = new MenuItem(
			rc.getString("mailbodyheaderforegroundMenu"));
		sf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final PopupWindow pw = new PopupWindow(MainWindow.this);
				ColorChooser cc = new ColorChooser(string2Color(
					rc.getString("mailbodyheaderForeground")));
				cc.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ie) {
						Color tmpcolor = (Color) ie.getItem();
						rc.setString("mailbodyheaderForeground"
							, color2String(tmpcolor));
						pw.hide();
						pw.dispose();
					}
				});
				pw.add("Center", cc);
				pw.pack();
				pw.show(body, 0, 0);
			}
		});
		menu.add(sf);
		return menu;
	}
    
    protected Text[] createMailList(MessageInfo ami, int as) {
        TextStyle tmpstyle = null;
        try {
            tmpstyle = string2TextStyle(rc.getString("maillistFont"));
        } catch (MissingResourceException ex) {
            tmpstyle = defaultstyle;
        }
        Text[] tmptexts = new Text[4];
        TextBuffer txtbuf = new TextBuffer();
        txtbuf.setTextStyle(tmpstyle);
        if (ami.getSeenFlag())
            txtbuf.append(chkimg);
        else
            txtbuf.append(blkimg);
        if (ami.getAttachFlag())
            txtbuf.append(attimg);
        else
            txtbuf.append(blkimg);
		VSpace spacing = new VSpace(6 * as, 12);
		txtbuf.append(spacing);
        txtbuf.append(Cp932.toCp932(ami.getSubject()));
        tmptexts[0] = txtbuf.toText();
        tmptexts[1] = new Text(Cp932.toCp932(ami.getFrom()), tmpstyle);
        tmptexts[2] = new Text(Cp932.toCp932(ami.getDate()), tmpstyle);
        tmptexts[3] = new Text(Integer.toString(((int) ami.getSize()) >> 10)
            + "KB", tmpstyle);
        return tmptexts;
    }
    
    protected void refreshBody() {
        dontchange = true;
        int index = mails.getSelectedIndex();
        if (index < 0) {
            dontchange = false;
            return;
        }
        saveGUIPref();
        statusline.setText(rc.getString("readingMails"));
        MessageInfo tmpmi = (MessageInfo) msginfo.elementAt(index);
        RecvMessage rm = new RecvMessage(MainWindow.this
            , new File(currentAccount().getName() + ".mbx")
            , tmpmi.getOffset(), tmpmi.getSize());
        TextStyle tmpstyle = null;
        try {
            tmpstyle = string2TextStyle(rc.getString("mailbodyFont"));
        } catch (MissingResourceException ex) {
            tmpstyle = defaultstyle;
        }
        rm.setTextStyle(tmpstyle);
        try {
            rm.setVisibleHeaders(CSV.cut(rc.getString("visibleHeader")));
        } catch (MissingResourceException ex) {}
		rm.setColor(string2Color(rc.getString("mailbodyForeground")));
		rm.setHeaderColor(
			string2Color(rc.getString("mailbodyheaderForeground")));
        rm.headersProc();
        rm.process();
        body.setRichText(rm.getRichText());
        recentfrom = rm.getHeader("from")[0];
        int findex = indexes.getInt(index);
        tmpmi.setSeenFlag();
        if (rm.getAttachFlag())
            tmpmi.setAttachFlag();
        msginfo.setElementAt(tmpmi, index);
        mails.replaceItem(createMailList(tmpmi, spacings.getInt(index)), index);
        FolderManager fm
            = new FolderManager(currentAccount(), currentfolder);
        fm.writeMessageInfo(tmpmi, findex);
        fm.close();
        lastindex = index;
        dontchange = false;
        statusline.setText(" ");
    }
    
    protected void refreshFolders() {
        String[] titles = CSV.cut(rc.getString("folderlistTitles"));
        int[] widths = null;
        if (folders != null) {
            folderpanel.remove(folders);
            widths = folders.getModel().getTextList().getColumnWidths();
            rc.setString("folderlistWidths", CSV.toString(widths));
        }
        TextStyle tmpstyle = null;
        try {
            tmpstyle = string2TextStyle(rc.getString("folderlistFont"));
        } catch (MissingResourceException ex) {
            tmpstyle = defaultstyle;
        }
        try {
            widths = CSV.cutInt(rc.getString("folderlistWidths"));
        } catch (MissingResourceException ex) {
            int tmpwidth = folderpanel.getSize().width;
            widths = new int[2];
            widths[0] = tmpwidth / 3 * 2;
            widths[1] = tmpwidth - widths[0] - 4;
        }
        folders = new TableList(0, titles, widths);
//		folders.setDeselectionEnabled(true);
//		folders.setFocusTraversable(false);
        folders.setForeground(
			string2Color(rc.getString("folderlistForeground")));
        folders.setBackground(
			string2Color(rc.getString("folderlistBackground")));
        folders.setSelectionForeground(
			string2Color(rc.getString("folderlistSelectionForeground")));
        folders.setSelectionBackground(
			string2Color(rc.getString("folderlistSelectionBackground")));
        folders.setScrollbarThickness(
            Integer.parseInt(rc.getString("scrollbarThickness")));
        folders.addItemListener(new ItemEventCatcher(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                refml = refreshMailList(selectedFolder());
                refml.start();
            }
        }));
        folders.addKeyListener(new KeyProcessor());
		PopupMenu tmpmenu = folders.getPopupMenu();
		tmpmenu.addSeparator();
		tmpmenu.add(createFolderListPrefMenu());
		folders.setPopupMenu(tmpmenu);
        String[] tmpflds = currentAccount().getFolders();
        for (int i = 0; i < tmpflds.length; i++) {
			VImage tmpimg;
			if (tmpflds[i].equals(rc.getString("inbox")))
				tmpimg = inboximg;
			else if (tmpflds[i].equals(rc.getString("draft")))
				tmpimg = draftimg;
			else if (tmpflds[i].equals(rc.getString("unsend")))
				tmpimg = unsendimg;
			else if (tmpflds[i].equals(rc.getString("sent")))
				tmpimg = sentimg;
			else if (tmpflds[i].equals(rc.getString("trash")))
				tmpimg = trashimg;
			else
				tmpimg = folderimg;
            Text[] tmptxts = new Text[2];
            TextBuffer txtbuf = new TextBuffer();
            txtbuf.setTextStyle(tmpstyle);
            txtbuf.append(tmpimg);
            txtbuf.append(tmpflds[i]);
            tmptxts[0] = txtbuf.toText();
            FolderManager tmpfm = new FolderManager(currentAccount()
                , tmpflds[i]);
            tmptxts[1] 
                = new Text(Integer.toString(tmpfm.getMessageCount()),tmpstyle);
            tmpfm.close();
            folders.addItem(tmptxts);
        }
        folderpanel.add("Center", folders);
        MainWindow.this.validate();
        folders.select(0);
        folders.makeVisible(0);
        createMoveMailMenu();
        createCopyMailMenu();
        refml = refreshMailList(selectedFolder());
        refml.start();
    }

	public void refreshMailCount() {
        TextStyle tmpstyle = null;
        try {
            tmpstyle = string2TextStyle(rc.getString("folderlistFont"));
        } catch (MissingResourceException ex) {
            tmpstyle = defaultstyle;
        }
		String[] tmpflds = currentAccount().getFolders();
		for (int i = 0; i < tmpflds.length; i++) {
			FolderManager tmpfm = new FolderManager(currentAccount()
				, tmpflds[i]);
			TextBuffer txtbuf = new TextBuffer();
			txtbuf.setTextStyle(tmpstyle);
			txtbuf.append(Integer.toString(tmpfm.getMessageCount()));
			folders.setItem(i, 1, txtbuf.toText());
			tmpfm.close();
		}
	}

	private TableList createTableList() {
        int scrollbarthickness
            = Integer.parseInt(rc.getString("scrollbarThickness"));
        String[] titles = CSV.cut(rc.getString("maillistTitles"));
        int[] widths = null;
        if (mails != null) {
            widths = mails.getModel().getTextList().getColumnWidths();
            rc.setString("maillistWidths", CSV.toString(widths));
        }
        try {
            widths = CSV.cutInt(rc.getString("maillistWidths"));
        } catch (MissingResourceException ex) {
            int tmpwidth = mailpanel.getSize().width - scrollbarthickness;
            widths = new int[4];
            int allwidth = 0;
            allwidth += widths[0] = tmpwidth / 3;
            allwidth += widths[1] = tmpwidth / 3;
            allwidth += widths[2] = tmpwidth / 4;
            widths[3] = tmpwidth - allwidth - 4;
        }
        TableList tmpmails = new TableList(0, titles, widths);
//		tmpmails.setFocusTraversable(false);
        tmpmails.setForeground(
 			string2Color(rc.getString("maillistForeground")));
        tmpmails.setBackground(
			string2Color(rc.getString("maillistBackground")));
        tmpmails.setSelectionForeground(
			string2Color(rc.getString("maillistSelectionForeground")));
        tmpmails.setSelectionBackground(
			string2Color(rc.getString("maillistSelectionBackground")));
        tmpmails.setScrollbarThickness(scrollbarthickness);
        tmpmails.setSelectionMode(TableList.SHIFT_MULTIPLE_SELECTIONS);
        tmpmails.setDeselectionEnabled(false);
		PopupMenu pmenu = tmpmails.getPopupMenu();
		pmenu.addSeparator();
		pmenu.add(pmovemail);
		pmenu.add(pcopymail);
		pmenu.addSeparator();
		pmenu.add(createMailListPrefMenu());
		tmpmails.setPopupMenu(pmenu);
/*      mails.addItemListener(new ItemEventCatcher(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                refreshBody();
            }
        }));*/
        tmpmails.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                refreshBody();
            }
        });
        tmpmails.addKeyListener(new KeyProcessor());
		return tmpmails;
	}

	protected RefreshMailList refreshMailList(String af) {
		if (new Boolean(rc.getString("threadView")).booleanValue())
			return new ThreadedRefreshMailList(af);
		else
			return new DefaultRefreshMailList(af);
	}

	protected class RefreshMailList extends Thread {
		boolean cancel = false;
		String folder;

		public void cancel() {
			cancel = true;
		}
	}
    
    protected class DefaultRefreshMailList extends RefreshMailList {
        public DefaultRefreshMailList(String af) {
            folder = af;
        }
        
        public void run() {
            FolderManager currentfm
                = new FolderManager(currentAccount(), folder);
            int newermail = 0;
			TableList tmpmails = createTableList();
            int count = currentfm.getMessageCount();
            Vector tmpmsginfo = new Vector(count);
            VArray tmpindexes = new VArray(int.class, count);
			VArray tmpspacings = new VArray(int.class, count);
			String readingstr = rc.getString("readingList"), mark = "";
            for (int i = count - 1; i >= 0 && !cancel; i--) {
                MessageInfo tmpinfo = currentfm.getMessageInfo(i);
                if (!tmpinfo.getDeleteFlag()) {
                    tmpmails.addItem(createMailList(tmpinfo, 0), 0);
                    tmpmsginfo.insertElementAt(tmpinfo, 0);
                    VArray tmpva = new VArray(int.class, 1);
                    tmpva.append(i);
                    tmpindexes.insert(0, tmpva);
					tmpspacings.append(0);
                    if (tmpinfo.getSeenFlag()) {
						mark = "*";
                        newermail++;
                    } else {
						mark = "";
                        newermail = 0;
					}
                	statusline.setText(readingstr + i + "/" + count + mark);
                }
				try {
					sleep(1);
				} catch (InterruptedException ex) {}
            }
            currentfm.close();
			currentfolder = folder;
            while (dontchange) {
                try {
                    sleep(100);
                } catch (InterruptedException ex) {}
            }
            if (mails != null)
                mailpanel.remove(mails);
            mails = tmpmails;
            msginfo = tmpmsginfo;
            indexes = tmpindexes;
			spacings = tmpspacings;
            mailpanel.add(mails, "Center");
            statusline.setText(" ");
            MainWindow.this.validate();
            int tmpindex = (newermail >= mails.getItemCount())
                ? mails.getItemCount() - 1 : newermail;
            mails.select(tmpindex);
            mails.makeVisible(tmpindex);
            lastindex = -1;
        }
    }

    protected class ThreadedRefreshMailList extends RefreshMailList {
		private class LLMsgInfo {
			boolean isreply = false;
			int index = 0;
			MessageInfo mi = null;
			LLMsgInfo next = null, prev = null;
			int spacing = 0;
			LLMsgInfo samep = null;

			public LLMsgInfo(LLMsgInfo anext, LLMsgInfo aprev
				, int ai, MessageInfo ami) {
				index = ai;
				mi = ami;
				next = anext;
				prev = aprev;
			}

			public LLMsgInfo() {
			}

			public void setReply() {
				isreply = true;
			}
			public void setReply(boolean a) {
				isreply = a;
			}
			public boolean isReply() {
				return isreply;
			}

			public void setIndex(int ai) {
				index = ai;
			}
			public int getIndex() {
				return index;
			}

			public void setMessageInfo(MessageInfo ami) {
				mi = ami;
			}
			public MessageInfo getMessageInfo() {
				return mi;
			}

			public void setNext(LLMsgInfo anext) {
				next = anext;
			}
			public LLMsgInfo getNext() {
				return next;
			}

			public void setPrev(LLMsgInfo aprev) {
				prev = aprev;
			}
			public LLMsgInfo getPrev() {
				return prev;
			}

			public void setSpacing(int as) {
				spacing = as;
			}
			public int getSpacing() {
				return spacing;
			}

			public void setSameParent(LLMsgInfo a) {
				samep = a;
			}
			public LLMsgInfo getSameParent() {
				return samep;
			}
		}

        public ThreadedRefreshMailList(String af) {
            folder = af;
        }
        
        public void run() {
            FolderManager currentfm
                = new FolderManager(currentAccount(), folder);
            int count = currentfm.getMessageCount();
			int size = 0;
			String readingstr = rc.getString("readingList"), mark = "";
			LLMsgInfo origin = new LLMsgInfo();
			Hashtable msgids = new Hashtable();
            for (int i = count - 1; i >= 0 && !cancel; i--) {
				MessageInfo tmpinfo = currentfm.getMessageInfo(i);
				if (!tmpinfo.getDeleteFlag()) {
					size++;
					LLMsgInfo llmi = new LLMsgInfo();
					llmi.setMessageInfo(tmpinfo);
					llmi.setIndex(i);
					if (msgids.get(tmpinfo.getMessageId()) != null) {
						LLMsgInfo tmpllmi
							= (LLMsgInfo) msgids.get(tmpinfo.getMessageId());
						tmpllmi.getPrev().setNext(llmi);
						llmi.setPrev(tmpllmi.getPrev());
						llmi.setNext(tmpllmi);
						tmpllmi.setPrev(llmi);
						tmpllmi.setReply();
					} else if (msgids.get(tmpinfo.getParentId()) != null) {
						LLMsgInfo tmpllmi
							= (LLMsgInfo) msgids.get(tmpinfo.getParentId());
						tmpllmi.getPrev().setNext(llmi);
						llmi.setPrev(tmpllmi.getPrev());
						llmi.setNext(tmpllmi);
						llmi.setReply(tmpllmi.isReply());
						tmpllmi.setPrev(llmi);
						tmpllmi.setSameParent(llmi);
					} else {
						llmi.setPrev(origin);
						llmi.setNext(origin.getNext());
						if (origin.getNext() != null)
							origin.getNext().setPrev(llmi);
						origin.setNext(llmi);
					}
					if (tmpinfo.getParentId() != null
					&& !tmpinfo.getParentId().equals("")) {
						if (msgids.get(tmpinfo.getParentId()) != null) {
							boolean flag = false;
							LLMsgInfo tmpllmi = origin.getNext(), oldllmi
								= (LLMsgInfo) msgids.get(tmpinfo.getParentId());
							while (tmpllmi != null) {
								if (tmpllmi == oldllmi) {
//									llmi.setSameParent(oldllmi);
									break;
								} else if (tmpllmi == llmi) {
//									oldllmi.setSameParent(llmi);
									flag = true;
									break;
								}
								tmpllmi = tmpllmi.getNext();
							}
							if (flag)
								msgids.put(tmpinfo.getParentId(), llmi);
						} else {
							msgids.put(tmpinfo.getParentId(), llmi);
						}
					}
                    if (tmpinfo.getSeenFlag())
						mark = "*";
					else
						mark = "";
                	statusline.setText(readingstr + i + "/" + count + mark);
				}
				try {
					sleep(1);
				} catch (InterruptedException ex) {}
			}
            currentfm.close();
            int newermail = -1;
			TableList tmpmails = createTableList();
            Vector tmpmsginfo = new Vector(size);
            VArray tmpindexes = new VArray(int.class, size);
            VArray tmpspacings = new VArray(int.class, size);
			LLMsgInfo tmpllmi = origin.getNext();
			int c = 0, s = 0;
			String waitstr = rc.getString("waitStr");
            while (tmpllmi != null) {
                MessageInfo tmpinfo = tmpllmi.getMessageInfo();
				if (tmpllmi.getSameParent() != null)
					s = tmpllmi.getSameParent().getSpacing();
				else if (tmpllmi.isReply())
					s += 1;
				else
					s = 0;
				tmpllmi.setSpacing(s);
                tmpmails.addItem(createMailList(tmpinfo, s));
                tmpmsginfo.addElement(tmpinfo);
                tmpindexes.append(tmpllmi.getIndex());
				tmpspacings.append(s);
				if (newermail < 0 && !tmpinfo.getSeenFlag())
					newermail = c;
				tmpllmi = tmpllmi.getNext();
            	statusline.setText(waitstr + (c++) + "/" + size);
            }
			currentfolder = folder;
            while (dontchange) {
                try {
                    sleep(100);
                } catch (InterruptedException ex) {}
            }
            if (mails != null)
                mailpanel.remove(mails);
            mails = tmpmails;
            msginfo = tmpmsginfo;
            indexes = tmpindexes;
			spacings = tmpspacings;
            mailpanel.add(mails, "Center");
            statusline.setText(" ");
            MainWindow.this.validate();
            int tmpindex = (newermail >= mails.getItemCount() || newermail < 0)
                ? mails.getItemCount() - 1 : newermail;
            mails.select(tmpindex);
            mails.makeVisible(tmpindex);
            lastindex = -1;
        }
    }

    protected VImage createVImage(String aname) {
        return new VImage(getClass().getResource(aname));
//      return new VImage(aname);
    }
    
    protected void transaction() {
        ProgressBox pb = new ProgressBox(MainWindow.this
            , rc.getString("transactionTitle"), true);
        for (int i = 0; i < accounts.size(); i++) {
            SequenceProgressProcess spp = new SequenceProgressProcess();
            spp.addProgressProcess(
                new SmtpProcess((Account) accounts.elementAt(i)));
            spp.addProgressProcess(
                new Pop3Process((Account) accounts.elementAt(i)));
            pb.addProgressProcess(spp);
        }
        pb.setVisible(true);
        saveProperties();
        refreshFolders();
    }

	protected void transactionCurrentAccountOnly() {
        ProgressBox pb = new ProgressBox(MainWindow.this
            , rc.getString("transactionTitle"), true);
        SequenceProgressProcess spp = new SequenceProgressProcess();
        spp.addProgressProcess(
            new SmtpProcess(currentAccount()));
        spp.addProgressProcess(
            new Pop3Process(currentAccount()));
        pb.addProgressProcess(spp);
        pb.setVisible(true);
        saveProperties();
        refreshFolders();
	}

	protected void transactionCurrentAccountSendOnly() {
        ProgressBox pb = new ProgressBox(MainWindow.this
            , rc.getString("transactionTitle"), true);
        pb.addProgressProcess(new SmtpProcess(currentAccount()));
        pb.setVisible(true);
        saveProperties();
        refreshFolders();
	}

	protected void transactionCurrentAccountReceiveOnly() {
        ProgressBox pb = new ProgressBox(MainWindow.this
            , rc.getString("transactionTitle"), true);
        pb.addProgressProcess(new Pop3Process(currentAccount()));
        pb.setVisible(true);
        saveProperties();
        refreshFolders();
	}
    
    protected String getTemplete(String cate) {
        String result = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(currentAccount().getName() + "." + cate
                + ".tpl"), "UTF8"));
            StringBuffer strbuf = new StringBuffer();
            String tmpstr;
            while ((tmpstr = reader.readLine()) != null)
                strbuf.append(tmpstr + "\n");
            reader.close();
            result = new String(strbuf);
        } catch (IOException ex) {}
        return result;
    }
    
    protected void newMessage() {
        SendMessage sm = new SendMessage(MainWindow.this, currentAccount());
        sm.setBody(getTemplete("New"));
        SendWindow sw = new SendWindow(MainWindow.this, sm);
    }
    
    protected String parseTemplete(String cate, RecvMessage rm) {
        String tmpl = getTemplete(cate);
        StringBuffer strbuf = new StringBuffer();
        int tmpindex = 0, endindex = 0;
        while ((tmpindex = tmpl.indexOf("%{", endindex)) != -1) {
            strbuf.append(tmpl.substring(endindex, tmpindex));
            endindex = tmpl.indexOf("}", tmpindex);
            if (endindex == -1) {
                endindex = tmpindex;
                break;
            }
            String tag = tmpl.substring(tmpindex + 2, endindex).toLowerCase();
            if (rm.getHeader(tag) != null)
                strbuf.append(CSV.toString(rm.getHeader(tag)));
            else if (tag.toLowerCase().equals("quoted-body")) {
                BufferedReader reader = new BufferedReader(
                    new StringReader(rm.getText().toString()));
                String tmpstr;
                try {
                    while ((tmpstr = reader.readLine()) != null)
                        strbuf.append(rc.getString("quoter")
                            + tmpstr + "\n");
                    reader.close();
                } catch (IOException ex) {}
            } else if (tag.toLowerCase().equals("body"))
                strbuf.append(rm.getText().toString());
            else
                strbuf.append("%{" + tag + "}");
            endindex++;
        }
        strbuf.append(tmpl.substring(endindex));
        return new String(strbuf);
    }
    
    protected void rewriteMessage() {
        SendMessage sm = new SendMessage(MainWindow.this, currentAccount());
        int index = mails.getSelectedIndex();
        MessageInfo tmpmi = (MessageInfo) msginfo.elementAt(index);
        RecvMessage rm = new RecvMessage(MainWindow.this
            , new File(currentAccount().getName() + ".mbx")
            , tmpmi.getOffset(), tmpmi.getSize());
        rm.process();
		Hashtable hs = rm.getHeaders();
		for (Enumeration e = hs.keys(); e.hasMoreElements(); ) {
			String h = (String) e.nextElement();
			if (h.equals("received")
				|| h.equals("content-type")
				|| h.equals("content-transfer-encoding"))
				continue;
			sm.setHeader(h, 0, rm.getHeader(h)[0]);
		}
        sm.setBody(rm.getText().toString().trim());
        mails.remove(index);
        msginfo.removeElementAt(index);
        tmpmi.setDeleteFlag();
        FolderManager fm
            = new FolderManager(currentAccount(), currentfolder);
        fm.writeMessageInfo(tmpmi, indexes.getInt(index));
        fm.close();
        indexes.remove(index, 1);
		spacings.remove(index, 1);
        new SendWindow(MainWindow.this, sm);
    }

	protected String removeQuotes(String addr) {
		return StringReplacer.replaceString(addr, "\"", "");
	}
    
    protected void replyMessages() {
        if (currentfolder.equals(rc.getString("draft"))) {
            rewriteMessage();
            return;
        }
        SendMessage sm = new SendMessage(MainWindow.this, currentAccount());
        StringBuffer strbuf = new StringBuffer();
        int indexes[] = mails.getSelectedIndexes();
        for (int i = 0; i < indexes.length; i++) {
            MessageInfo tmpmi = (MessageInfo) msginfo.elementAt(indexes[i]);
            RecvMessage rm = new RecvMessage(MainWindow.this
                , new File(currentAccount().getName() + ".mbx")
                , tmpmi.getOffset(), tmpmi.getSize());
            rm.process();
            String tmpsub = "";
            if (rm.getHeader("subject") != null) {
                tmpsub = rm.getHeader("subject")[0];
                if (tmpsub.length() > 2 &&
                    !tmpsub.toLowerCase().substring(0, 2).equals("re"))
                    tmpsub = "Re: " + tmpsub;
                else if (tmpsub.length() > 2 &&
                    Character.isLetter(tmpsub.toLowerCase().charAt(2)))
                    tmpsub = "Re: " + tmpsub;
            }
            sm.setHeader("subject", 0, tmpsub
                + (sm.getHeader("subject") != null ?
                " " + sm.getHeader("subject")[0] : ""));
            String from = (rm.getHeader("reply-to") != null)
                ? rm.getHeader("reply-to")[0] : rm.getHeader("from")[0];
			from = removeQuotes(from);
            sm.setHeader("to", 0, from
                + (sm.getHeader("to") != null ?
                "," + sm.getHeader("to")[0] : ""));
            if (rm.getHeader("cc") != null)
                sm.setHeader("cc", 0,
					removeQuotes(CSV.toString(rm.getHeader("cc")))
                    + (sm.getHeader("cc") != null ?
                    "," + sm.getHeader("cc")[0] : ""));
            if (rm.getHeader("message-id") != null)
                sm.setHeader("in-reply-to", 0, rm.getHeader("message-id")[0]);
            strbuf.append(parseTemplete("Reply", rm));
        }
        sm.setBody(new String(strbuf));
        new SendWindow(MainWindow.this, sm);
    }
    
    protected void forwardMessages() {
        if (currentfolder.equals(rc.getString("draft"))) {
            rewriteMessage();
            return;
        }
        SendMessage sm = new SendMessage(MainWindow.this, currentAccount());
        int[] indexes = mails.getSelectedIndexes();
        StringBuffer subbuf = new StringBuffer();
        StringBuffer bodybuf = new StringBuffer();
        SequenceProgressProcess spp = new SequenceProgressProcess();
        for (int i = 0; i < indexes.length; i++) {
            File tmpfile = new File("fwd" + i + ".eml");
			if (tmpfile.exists())
				tmpfile.delete();
            MessageInfo tmpmi = (MessageInfo) msginfo.elementAt(indexes[i]);
            RecvMessage rm = new RecvMessage(MainWindow.this
                , new File(currentAccount().getName() + ".mbx")
                , tmpmi.getOffset(), tmpmi.getSize());
            rm.process();
            if (rm.getHeader("subject") != null)
                subbuf.append("FWD: " + rm.getHeader("subject")[0] + " ");
            bodybuf.append(parseTemplete("Forward", rm));   
            SaveProcess sp = new SaveProcess(
                new File(currentAccount().getName() + ".mbx"), tmpfile
                , tmpmi.getOffset(), tmpmi.getSize());
            spp.addProgressProcess(sp);
            sm.addAttachFile(tmpfile.getAbsolutePath());
        }
        sm.setHeader("subject", 0, new String(subbuf).trim());
        sm.setBody(new String(bodybuf));
        ProgressBox pb = new ProgressBox(MainWindow.this
            , rc.getString("saveTitle"), true);
        pb.addProgressProcess(spp);
        pb.setVisible(true);
        new SendWindow(MainWindow.this, sm);
    }
    
    void createMenuBar() {
        MenuBar menubar = new MenuBar();
        menubar.add(createFileMenu());
        menubar.add(createEditMenu());
        menubar.add(createMailMenu());
        menubar.add(createViewMenu());
		menubar.setHelpMenu(createHelpMenu());
        setMenuBar(menubar);
    }

	class ExportProc extends ProgressProcess {
		File outputfile = null;
		ProgressProcess currentpp = null;

		public ExportProc(File ao) {
			outputfile = ao;
		}

		public String getTitle() {
			return currentAccount().getName();
		}

		public void interrupt() {
			super.interrupt();
			if (currentpp != null)
				currentpp.interrupt();
		}
	}
    
	class ExportSelectedMailsProc extends ExportProc {
		public ExportSelectedMailsProc(File ao) {
			super(ao);
		}

		public void run() {
        	dontchange = true;
        	int[] tmpidx = mails.getSelectedIndexes();
        	if (tmpidx.length == 0) {
            	dontchange = false;
            	return;
        	}
        	VArray tmpva = new VArray(int.class);
        	for (int i = 0; i < tmpidx.length; i++)
            	tmpva.append(tmpidx[i]);
        	tmpva.sort();
        	tmpva.trim();
        	tmpidx = (int[]) tmpva.getArray();
			VariableEvent ve = new VariableEvent(ExportSelectedMailsProc.this
				, rc.getString("writingStr"), tmpidx.length - 1, 0);
			invokeListener(ve);
        	for (int i = 0; i < tmpidx.length && !isInterrupted(); i++) {
            	MessageInfo tmpmi = (MessageInfo) msginfo.elementAt(tmpidx[i]);
				String fromline = "From "
					+ new AddressParser(tmpmi.getFrom()).getAddress() + " "
					+ tmpmi.getDate() + "\r\n";
				try {
					FileOutputStream fout = new FileOutputStream(
						outputfile.getAbsolutePath(), true);
					fout.write(fromline.getBytes("8859_1"));
					fout.close();
				} catch (IOException ex) {}
				currentpp = new SaveProcess(
                    new File(currentAccount().getName() + ".mbx"), outputfile
					, tmpmi.getOffset(), tmpmi.getSize());
				currentpp.start();
				try {
					((Thread) currentpp).join();
				} catch (InterruptedException ex) {}
				ve.setCurrentValue(i);
				invokeListener(ve);
        	}
			ve.setStatus(rc.getString("doneStr"));
			invokeListener(ve);
        	dontchange = false;
		}
	}

	class ExportAllMailsProc extends ExportProc {
		public ExportAllMailsProc(File ao) {
			super(ao);
		}

		public void run() {
			dontchange = true;
			if (currentfolder == null || currentfolder.equals("")) {
				dontchange = false;
				return;
			}
			FolderManager fm
				= new FolderManager(currentAccount(), currentfolder);
			VariableEvent ve = new VariableEvent(ExportAllMailsProc.this
				, rc.getString("writingStr"), fm.getMessageCount() - 1, 0);
			invokeListener(ve);
        	for (int i = 0; i<fm.getMessageCount() && !isInterrupted(); i++) {
            	MessageInfo tmpmi = fm.getMessageInfo(i);
				if (tmpmi.getDeleteFlag())
					continue;
				String fromline = "From "
					+ new AddressParser(tmpmi.getFrom()).getAddress() + " "
					+ tmpmi.getDate() + "\r\n";
				try {
					FileOutputStream fout = new FileOutputStream(
						outputfile.getAbsolutePath(), true);
					fout.write(fromline.getBytes("8859_1"));
					fout.close();
				} catch (IOException ex) {}
				currentpp = new SaveProcess(
                    new File(currentAccount().getName() + ".mbx"), outputfile
					, tmpmi.getOffset(), tmpmi.getSize());
				currentpp.start();
				try {
					((Thread) currentpp).join();
				} catch (InterruptedException ex) {}
				ve.setCurrentValue(i);
				invokeListener(ve);
        	}
			fm.close();
			ve.setStatus(rc.getString("doneStr"));
			invokeListener(ve);
        	dontchange = false;
		}
	}

    Menu createFileMenu() {
        Menu menu = new Menu(rc.getString("fileMenu"));
        MenuItem addaddress = new MenuItem(rc.getString("addaddressMenu")
            , new MenuShortcut('a'));
        addaddress.addActionListener(new ActionEventCatcher(
            new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                AddressParser ap = new AddressParser(recentfrom);
                AddressDlg ad = new AddressDlg(MainWindow.this);
                ad.setName(removeQuotes(ap.getComment()));
                ad.setAddress(ap.getAddress());
                ad.setVisible(true);
            }
        }));
        menu.add(addaddress);
		menu.addSeparator();
        MenuItem createac = new MenuItem(rc.getString("createacMenu"));
        createac.addActionListener(new ActionEventCatcher(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                AccountDlg ad = new AccountDlg(MainWindow.this);
                ad.setVisible(true);
                if (!ad.getStatus()) {
					ad.dispose();
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                accounts.addElement(ad.getAccount());
                achoice.add(ad.getAccount().getName());
				ad.dispose();
                saveProperties();
            }
        }));
        menu.add(createac);
        MenuItem changeac = new MenuItem(rc.getString("changeacMenu"));
        changeac.addActionListener(new ActionEventCatcher(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                AccountDlg ad = new AccountDlg(MainWindow.this
                    , currentAccount());
                int tmpidx = achoice.getSelectedIndex();
				ad.setVisible(true);
                if (!ad.getStatus()) {
					ad.dispose();
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                accounts.setElementAt(ad.getAccount(), tmpidx);
                achoice.replaceItem(ad.getAccount().getName(), tmpidx);
				ad.dispose();
            }
        }));
        menu.add(changeac);
		MenuItem deleteac = new MenuItem(rc.getString("deleteacMenu"));
		deleteac.addActionListener(new ActionEventCatcher(
			new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (accounts.size() < 2) {
					new MsgBox(MainWindow.this
						, rc.getString("warningTitle")
						, rc.getString("cantdeleteacStr"), true)
						.setVisible(true);
					return;
				}
				YesNoBox ynb = new YesNoBox(MainWindow.this
					, rc.getString("deleteacTitle")
					, rc.getString("confirmdeleteacLabel"));
				ynb.setVisible(true);
				if (ynb.getState()) {
					int tmpidx = achoice.getSelectedIndex();
					achoice.remove(tmpidx);
					accounts.removeElementAt(tmpidx);
					achoice.select(0);
					refreshFolders();
				}
			}
		}));
		menu.add(deleteac);
        menu.addSeparator();
        MenuItem addfolder = new MenuItem(rc.getString("addfolderMenu"));
        addfolder.addActionListener(new ActionEventCatcher(
            new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                InputBox ib = new InputBox(MainWindow.this
                    , rc.getString("addfolderTitle")
                    , rc.getString("inputfolderLabel"));
                ib.setVisible(true);
                if (ib.getStatus()) {
					try { // write test.
						String tmpname = currentAccount().getName() + "."
							+ ib.getString() + ".idx";
						if (new File(tmpname).exists()) {
							Toolkit.getDefaultToolkit().beep();
							return;
						}
						FileWriter fw = new FileWriter(tmpname);
						fw.write("write test");
						fw.close();
						new File(tmpname).delete();
					} catch (IOException ex) {
						Toolkit.getDefaultToolkit().beep();
						return;
					}
                    currentAccount().addFolder(ib.getString());
                    refreshFolders();
                }
            }
        }));
        menu.add(addfolder);
		MenuItem deletefolder = new MenuItem(rc.getString("deletefolderMenu"));
		deletefolder.addActionListener(new ActionEventCatcher(
			new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (folders.getSelectedIndex() < 0)
					return;
				YesNoBox ynb = new YesNoBox(MainWindow.this
					, rc.getString("deletefolderTitle")
					, rc.getString("confirmdeletefolderLabel"));
				ynb.setVisible(true);
				if (ynb.getState()) {
					int tmpidx = folders.getSelectedIndex();
                    String tmpfolder = folders.getItem(tmpidx, 0).substring(1);
					currentAccount().removeFolder(tmpfolder);
					refreshFolders();
				}
			}
		}));
		menu.add(deletefolder);
		MenuItem upfolder = new MenuItem(rc.getString("upfolderMenu")
			, new MenuShortcut('u'));
		upfolder.addActionListener(new ActionEventCatcher(
			new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (folders.getSelectedIndex() < 0)
					return;
				int tmpidx = folders.getSelectedIndex();
                String tmpfolder = folders.getItem(tmpidx, 0).substring(1);
				currentAccount().upFolder(tmpfolder);
				refreshFolders();
			}
		}));
		menu.add(upfolder);
		MenuItem downfolder = new MenuItem(rc.getString("downfolderMenu")
			, new MenuShortcut('d'));
		downfolder.addActionListener(new ActionEventCatcher(
			new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (folders.getSelectedIndex() < 0)
					return;
				int tmpidx = folders.getSelectedIndex();
                String tmpfolder = folders.getItem(tmpidx, 0).substring(1);
				currentAccount().downFolder(tmpfolder);
				refreshFolders();
			}
		}));
		menu.add(downfolder);
        menu.addSeparator();
        MenuItem export = new MenuItem(rc.getString("exportMenu"));
/*		export.addActionListener(new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int index = mails.getSelectedIndex();
                if (index < 0)
                    return;
                MessageInfo tmpmi = (MessageInfo) msginfo.elementAt(index);
                FileDialog fd = new FileDialog(MainWindow.this);
                fd.setMode(FileDialog.SAVE);
                fd.setVisible(true);
                if (fd.getFile() == null)
                    return;
                SaveProcess sp = new SaveProcess(
                    new File(currentAccount().getName() + ".mbx")
                    , new File(fd.getDirectory(), fd.getFile())
                    , tmpmi.getOffset(), tmpmi.getSize());
                ProgressBox pb = new ProgressBox(MainWindow.this
                    , rc.getString("saveTitle"), true);
                pb.addProgressProcess(sp);
                pb.setVisible(true);
            }
        }));*/
		export.addActionListener(new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int index[] = mails.getSelectedIndexes();
                if (index.length == 0)
                    return;
                FileDialog fd = new FileDialog(MainWindow.this);
                fd.setMode(FileDialog.SAVE);
                fd.setVisible(true);
                if (fd.getFile() == null)
                    return;
                ExportSelectedMailsProc ep = new ExportSelectedMailsProc(
                    new File(fd.getDirectory(), fd.getFile()));
                ProgressBox pb = new ProgressBox(MainWindow.this
                    , rc.getString("saveTitle"), true);
                pb.addProgressProcess(ep);
                pb.setVisible(true);
            }
        }));
        menu.add(export);
        MenuItem exportall = new MenuItem(rc.getString("allexportMenu"));
		exportall.addActionListener(
			new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                FileDialog fd = new FileDialog(MainWindow.this);
                fd.setMode(FileDialog.SAVE);
                fd.setVisible(true);
                if (fd.getFile() == null)
                    return;
                ExportAllMailsProc ep = new ExportAllMailsProc(
                    new File(fd.getDirectory(), fd.getFile()));
                ProgressBox pb = new ProgressBox(MainWindow.this
                    , rc.getString("saveTitle"), true);
                pb.addProgressProcess(ep);
                pb.setVisible(true);
            }
        }));
        menu.add(exportall);
		MenuItem importm = new MenuItem(rc.getString("importMenu"));
		importm.addActionListener(
			new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                FileDialog fd = new FileDialog(MainWindow.this);
                fd.setMode(FileDialog.LOAD);
                fd.setVisible(true);
                if (fd.getFile() == null)
                    return;
                ImportProcess ip = new ImportProcess(currentAccount()
                    , new File(fd.getDirectory(), fd.getFile()), currentfolder);
                ProgressBox pb = new ProgressBox(MainWindow.this
                    , rc.getString("importTitle"), true);
                pb.addProgressProcess(ip);
                pb.setVisible(true);
				refreshFolders();
            }
        }));
        menu.add(importm);
        menu.addSeparator();
        MenuItem defragmbox = new MenuItem(rc.getString("defragMenu"));
        defragmbox.addActionListener(new ActionEventCatcher(
            new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                ProgressBox pb = new ProgressBox(MainWindow.this
                    , rc.getString("defragTitle"), true);
                pb.addProgressProcess(new DefragProcess(currentAccount()));
                pb.setVisible(true);
                refreshFolders();
            }
        }));
        menu.add(defragmbox);
        menu.addSeparator();
        MenuItem exit = new MenuItem(rc.getString("exitMenu")
			, new MenuShortcut('q'));
        exit.addActionListener(new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                saveGUIPref();
                setVisible(false);
                dispose();
            }
        }));
        menu.add(exit);
        return menu;
    }
    
    Menu createEditMenu() {
        Menu menu = body.getEditMenu();
		for (int i = 0; i < menu.getItemCount(); i++) {
			MenuItem tmpitem = menu.getItem(i);
			String tmpstr = tmpitem.getActionCommand();
			if (tmpstr.equals(TextController.A_UNDO))
				tmpitem.setShortcut(new MenuShortcut('z'));
			else if (tmpstr.equals(TextController.A_CUT))
				tmpitem.setShortcut(new MenuShortcut('x'));
			else if (tmpstr.equals(TextController.A_COPY))
				tmpitem.setShortcut(new MenuShortcut('c'));
			else if (tmpstr.equals(TextController.A_PASTE))
				tmpitem.setShortcut(new MenuShortcut('v'));
		}
        return menu;
    }

	class FilteringSelectedMailsProc extends ProgressProcess {
		public String getTitle() {
			return currentAccount().getName();
		}

		public void run() {
        	dontchange = true;
        	int[] tmpidx = mails.getSelectedIndexes();
        	if (tmpidx.length == 0) {
            	dontchange = false;
            	return;
        	}
        	VArray tmpva = new VArray(int.class);
        	for (int i = 0; i < tmpidx.length; i++)
            	tmpva.append(tmpidx[i]);
        	tmpva.sort();
        	tmpva.trim();
        	tmpidx = (int[]) tmpva.getArray();
			AccountManager am = new AccountManager(currentAccount());
			am.load();
        	for (int i = 0; i < tmpidx.length && !isInterrupted(); i++) {
				VariableEvent ve = new VariableEvent(
					FilteringSelectedMailsProc.this, 
					rc.getString("nowfilteringStr"), tmpidx.length, i + 1);
				invokeListener(ve);
            	MessageInfo tmpmi = (MessageInfo) msginfo.elementAt(tmpidx[i]);
        		RecvMessage rm = new RecvMessage(MainWindow.this
       	    		, new File(currentAccount().getName() + ".mbx")
            		, tmpmi.getOffset(), tmpmi.getSize());
            	FolderManager fm = new FolderManager(currentAccount()
					, am.getFilteringFolder(rm.getHeaders()));
            	fm.writeMessageInfo(tmpmi, fm.getMessageCount());
            	fm.close();
				tmpmi.setDeleteFlag();
	           	fm = new FolderManager(currentAccount(), currentfolder);
            	fm.writeMessageInfo(tmpmi, indexes.getInt(tmpidx[i]));
            	fm.close();
        	}
        	dontchange = false;
		}
	}

	class FilteringAllMailsProc extends ProgressProcess {
		public String getTitle() {
			return currentAccount().getName();
		}

		public void run() {
        	dontchange = true;
			AccountManager am = new AccountManager(currentAccount());
			am.load();
        	for (int i = 0; i < msginfo.size() && !isInterrupted(); i++) {
				VariableEvent ve = new VariableEvent(
					FilteringAllMailsProc.this, 
					rc.getString("nowfilteringStr"), msginfo.size(), i + 1);
				invokeListener(ve);
            	MessageInfo tmpmi = (MessageInfo) msginfo.elementAt(i);
        		RecvMessage rm = new RecvMessage(MainWindow.this
       	    		, new File(currentAccount().getName() + ".mbx")
            		, tmpmi.getOffset(), tmpmi.getSize());
            	FolderManager fm = new FolderManager(currentAccount()
					, am.getFilteringFolder(rm.getHeaders()));
            	fm.writeMessageInfo(tmpmi, fm.getMessageCount());
            	fm.close();
				tmpmi.setDeleteFlag();
	           	fm = new FolderManager(currentAccount(), currentfolder);
            	fm.writeMessageInfo(tmpmi, indexes.getInt(i));
            	fm.close();
        	}
        	dontchange = false;
		}
	}
    
    Menu createMailMenu() {
        Menu menu = new Menu(rc.getString("mailMenu"));
        MenuItem transaction
            = new MenuItem(rc.getString("transactionMenu")
            , new MenuShortcut('t'));
        transaction.addActionListener(
            new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                transaction();
            }
        }));
        menu.add(transaction);
		Menu caomenu = new Menu(rc.getString("currentAccountOnlyMenu"));
        MenuItem caotransaction
            = new MenuItem(rc.getString("currentAccountOnlyTransactionMenu"));
        caotransaction.addActionListener(
            new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                transactionCurrentAccountOnly();
            }
        }));
        caomenu.add(caotransaction);
        MenuItem caosendonly
            = new MenuItem(rc.getString("currentAccountOnlySendOnlyMenu"));
        caosendonly.addActionListener(
            new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                transactionCurrentAccountSendOnly();
            }
        }));
        caomenu.add(caosendonly);
        MenuItem caoreceiveonly
            = new MenuItem(rc.getString("currentAccountOnlyReceiveOnlyMenu"));
        caoreceiveonly.addActionListener(
            new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                transactionCurrentAccountReceiveOnly();
            }
        }));
        caomenu.add(caoreceiveonly);
		menu.add(caomenu);
        menu.addSeparator();
        MenuItem newmail = new MenuItem(rc.getString("newmailMenu")
            , new MenuShortcut('n'));
        newmail.addActionListener(
            new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                newMessage();
            }
        }));
        menu.add(newmail);
        MenuItem replymail = new MenuItem(rc.getString("replymailMenu")
            , new MenuShortcut('r'));
        replymail.addActionListener(
            new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                replyMessages();
            }
        }));
        menu.add(replymail);
        MenuItem forwardmail = new MenuItem(rc.getString("forwardmailMenu")
            , new MenuShortcut('f'));
        forwardmail.addActionListener(
            new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                forwardMessages();
            }
        }));
        menu.add(forwardmail);
        menu.addSeparator();
		MenuItem searchmail = new MenuItem(rc.getString("searchmailMenu"));
		searchmail.addActionListener(
			new ActionEventCatcher(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				SearchDlg sd = new SearchDlg(MainWindow.this);
				sd.setVisible(true);
				if (sd.getState()) {
					ProgressBox pb = new ProgressBox(MainWindow.this
						, rc.getString("searchingTitle"), true);
					SearchProcess sp;
					if (sd.getCurrentFolderOnlyState())
						sp = new SearchProcess(currentAccount()
							, sd.getString(), currentfolder
							, sd.getIgnoreCaseState()
							, sd.getRegexpState());
					else
						sp = new SearchProcess(currentAccount()
							, sd.getString()
							, sd.getIgnoreCaseState()
							, sd.getRegexpState());
					pb.addProgressProcess(sp);
					pb.setVisible(true);
//					folders.deselect(folders.getSelectedIndex());
                   	refml = refreshMailList(rc.getString("search"));
                   	refml.start();
				}
			}
		}));
		menu.add(searchmail);
		menu.addSeparator();
        menu.add(movemail = new Menu(rc.getString("movemailMenu")));
        menu.add(copymail = new Menu(rc.getString("copymailMenu")));
		pmovemail = new Menu(rc.getString("movemailMenu"));
		pcopymail = new Menu(rc.getString("copymailMenu"));
		menu.addSeparator();
		final CheckboxMenuItem autofilter = new CheckboxMenuItem(
			rc.getString("autofilterMenu"),
			new Boolean(rc.getString("autoFiltering")).booleanValue());
		autofilter.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				rc.setString("autoFiltering"
					, new Boolean(autofilter.getState()).toString());
			}
		});
		menu.add(autofilter);
		MenuItem selectedfilter
			= new MenuItem(rc.getString("selectedmailfilterMenu"));
		selectedfilter.addActionListener(
			new ActionEventCatcher(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				SequenceProgressProcess spp = new SequenceProgressProcess();
				spp.addProgressProcess(new FilteringSelectedMailsProc());
				spp.addProgressProcess(new DefragFolderProcess(
					currentAccount(), currentfolder));
                ProgressBox pb = new ProgressBox(MainWindow.this
                    , rc.getString("filteringTitle"), true);
                pb.addProgressProcess(spp);
                pb.setVisible(true);
				refreshFolders();
			}
		}));
		menu.add(selectedfilter);
		MenuItem allfilter = new MenuItem(rc.getString("allmailfilterMenu"));
		allfilter.addActionListener(
			new ActionEventCatcher(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				SequenceProgressProcess spp = new SequenceProgressProcess();
				spp.addProgressProcess(new FilteringAllMailsProc());
				spp.addProgressProcess(new DefragFolderProcess(
					currentAccount(), currentfolder));
                ProgressBox pb = new ProgressBox(MainWindow.this
                    , rc.getString("filteringTitle"), true);
                pb.addProgressProcess(spp);
                pb.setVisible(true);
				refreshFolders();
			}
		}));
		menu.add(allfilter);
        menu.addSeparator();
        MenuItem templetes = new MenuItem(rc.getString("templeteMenu"));
        templetes.addActionListener(
            new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                TempleteDlg td
                    = new TempleteDlg(MainWindow.this, currentAccount());
                td.setVisible(true);
            }
        }));
        menu.add(templetes);
        MenuItem filterset = new MenuItem(rc.getString("filterMenu"));
        filterset.addActionListener(
            new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                FilterDlg fd
                    = new FilterDlg(MainWindow.this, currentAccount());
                fd.setVisible(true);
            }
        }));
        menu.add(filterset);
        return menu;
    }
    
    protected void moveMail(String folder, boolean delete) {
        dontchange = true;
        int[] tmpidx = mails.getSelectedIndexes();
        if (tmpidx.length == 0) {
            dontchange = false;
            return;
        }
        VArray tmpva = new VArray(int.class);
        for (int i = 0; i < tmpidx.length; i++)
            tmpva.append(tmpidx[i]);
        tmpva.sort();
        tmpva.trim();
        tmpidx = (int[]) tmpva.getArray();
        for (int i = 0; i < tmpidx.length; i++) {
            MessageInfo tmpmi = (MessageInfo) msginfo.elementAt(tmpidx[i]);
            FolderManager fm = new FolderManager(currentAccount(), folder);
            fm.writeMessageInfo(tmpmi, fm.getMessageCount());
            fm.close();
        }
        if (delete) {
            for (int i = 0; i < tmpidx.length; i++) {
                MessageInfo tmpmi
                    = (MessageInfo) msginfo.elementAt(tmpidx[i] - i);
                mails.remove(tmpidx[i] - i);
                msginfo.removeElementAt(tmpidx[i] - i);
                tmpmi.setDeleteFlag();
                FolderManager fm
                    = new FolderManager(currentAccount(), currentfolder);
                fm.writeMessageInfo(tmpmi, indexes.getInt(tmpidx[i] - i));
                fm.close();
                indexes.remove(tmpidx[i] - i, 1);
				spacings.remove(tmpidx[i] - i, 1);
            }
            if (lastindex < mails.getItemCount()) {
                mails.select(lastindex);
                mails.makeVisible(lastindex);
                refreshBody();
            } else if (lastindex > 0) {
                mails.select(mails.getItemCount() - 1);
                mails.makeVisible(mails.getItemCount() - 1);
                refreshBody();
            }
        }
		refreshMailCount();
        dontchange = false;
    }
    
    protected class MoveMail implements ActionListener {
        String folder;
        boolean delete = false;
        
        public MoveMail(String af, boolean ad){
            folder = af;
            delete = ad;
        }
        
        public void actionPerformed(ActionEvent ae) {
            moveMail(folder, delete);
        }
    }
    
    void createMoveMailMenu() {
        movemail.removeAll();
        if (folders != null) {
            for (int i = 0; i < folders.getItemCount(); i++) {
                String currentfolder = folders.getItem(i, 0).substring(1);
                MenuItem tmpmi = new MenuItem(currentfolder);
                tmpmi.addActionListener(new MoveMail(currentfolder, true));
                movemail.add(tmpmi);
            }
        }
        pmovemail.removeAll();
        if (folders != null) {
            for (int i = 0; i < folders.getItemCount(); i++) {
                String currentfolder = folders.getItem(i, 0).substring(1);
                MenuItem tmpmi = new MenuItem(currentfolder);
                tmpmi.addActionListener(new MoveMail(currentfolder, true));
                pmovemail.add(tmpmi);
            }
        }
    }
    
    void createCopyMailMenu() {
        copymail.removeAll();
        if (folders != null) {
            for (int i = 0; i < folders.getItemCount(); i++) {
                String currentfolder = folders.getItem(i, 0).substring(1);
                MenuItem tmpmi = new MenuItem(currentfolder);
                tmpmi.addActionListener(new MoveMail(currentfolder, false));
                copymail.add(tmpmi);
            }
        }
        pcopymail.removeAll();
        if (folders != null) {
            for (int i = 0; i < folders.getItemCount(); i++) {
                String currentfolder = folders.getItem(i, 0).substring(1);
                MenuItem tmpmi = new MenuItem(currentfolder);
                tmpmi.addActionListener(new MoveMail(currentfolder, false));
                pcopymail.add(tmpmi);
            }
        }
    }
    
    Menu createViewMenu() {
        Menu menu = new Menu(rc.getString("viewMenu"));
		final CheckboxMenuItem threadview = new CheckboxMenuItem(
			rc.getString("threadviewMenu"),
			new Boolean(rc.getString("threadView")).booleanValue());
		threadview.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				rc.setString("threadView"
					, new Boolean(threadview.getState()).toString());
			}
		});
		menu.add(threadview);
		menu.addSeparator();
        MenuItem allheader = new MenuItem(rc.getString("allHeaderMenu")
            , new MenuShortcut('l'));
        allheader.addActionListener(
            new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String tmpstr;
                try {
                    tmpstr = rc.getString("visibleHeader");
                    rc.setString("visibleHeader", "");
                    refreshBody();
                    rc.setString("visibleHeader", tmpstr);
                } catch (MissingResourceException ex) {
                    rc.setString("visibleHeader", "");
                    refreshBody();
                    rc.remove("visibleHeader");
                }
            }
        }));
        menu.add(allheader);
        MenuItem setvisible = new MenuItem(rc.getString("setVisibleMenu"));
        setvisible.addActionListener(
            new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                VisibleDlg vd = new VisibleDlg(MainWindow.this);
                vd.setVisible(true);
            }
        }));
        menu.add(setvisible);
        menu.addSeparator();
        MenuItem preferences = new MenuItem(rc.getString("preferencesMenu"));
        preferences.addActionListener(
            new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                PreferenceDlg pd = new PreferenceDlg(MainWindow.this);
                pd.setVisible(true);
            }
        }));
        menu.add(preferences);
        return menu;
    }

    Menu createHelpMenu() {
        Menu menu = new Menu(rc.getString("helpMenu"));
		MenuItem help = new MenuItem(rc.getString("helpMenu"));
		help.addActionListener(
			new ActionEventCatcher(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				HelpWindow hw = new HelpWindow();
				hw.setVisible(true);
			}
		}));
		menu.add(help);
		menu.addSeparator();
		MenuItem kfc = new MenuItem(rc.getString("kfcCopyrightMenu"));
		kfc.addActionListener(
			new ActionEventCatcher(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				jp.kyasu.awt.Dialog.showKFCCopyright(MainWindow.this);
			}
		}));
		menu.add(kfc);
        return menu;
    }

    void createWindow() {
        setTitle(rc.getString("appName"));
        setIconImage(Toolkit.getDefaultToolkit().getImage(
            getClass().getResource(rc.getString("morningimageName"))));
        setLayout(new BorderLayout());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                saveGUIPref();
                setVisible(false);
                dispose();
            }
            public void windowClosed(WindowEvent we) {
                saveProperties();
                System.exit(0);
            }
        });

        chkimg = createVImage(rc.getString("checkimageName"));
        attimg = createVImage(rc.getString("attachimageName"));
		inboximg = createVImage(rc.getString("inboximageName"));
		unsendimg = createVImage(rc.getString("unsendimageName"));
		sentimg = createVImage(rc.getString("sentimageName"));
		draftimg = createVImage(rc.getString("draftimageName"));
		trashimg = createVImage(rc.getString("trashimageName"));
        folderimg = createVImage(rc.getString("folderimageName"));

        int scrollbarthickness
            = Integer.parseInt(rc.getString("scrollbarThickness"));
        NativePanel np1 = new NativePanel(new BorderLayout());
        add(np1, "Center");
        SplitPanel sp1 = new SplitPanel(SplitPanel.HORIZONTAL);
        np1.add("Center", sp1);
        statusline = new Label(" ");
        np1.add("South", statusline);
        int[] tmpsize = new int[2];
        tmpsize = CSV.cutInt(rc.getString("folderlistSize"));
        p1 = new SolidPanel(new BorderLayout(), tmpsize[0], tmpsize[1]);
        sp1.add(p1);
        Panel p2 = new Panel(new GridLayout(1, 0, 0, 0));
        p1.add("North", p2);
        Button b1 = new Button(new VActiveButton(
            new Text(createVImage(rc.getString("timageName")))));
        b1.addActionListener(new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                transaction();
            }
        }));
		b1.setToolTipText(rc.getString("transactionMenu"));
        p2.add(b1);
        Button b2 = new Button(new VActiveButton(
            new Text(createVImage(rc.getString("nimageName")))));
        b2.addActionListener(new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                newMessage();
            }
        }));
		b2.setToolTipText(rc.getString("newmailMenu"));
        p2.add(b2);
        Button b3 = new Button(new VActiveButton(
            new Text(createVImage(rc.getString("rimageName")))));
        b3.addActionListener(new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                replyMessages();
            }
        }));
		b3.setToolTipText(rc.getString("replymailMenu"));
        p2.add(b3);
        Button b4 = new Button(new VActiveButton(
            new Text(createVImage(rc.getString("fimageName")))));
        b4.addActionListener(new ActionEventCatcher(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                forwardMessages();
            }
        }));
		b4.setToolTipText(rc.getString("forwardmailMenu"));
        p2.add(b4);
        folderpanel = new Panel(new BorderLayout());
        p1.add("Center", folderpanel);
        achoice = new Choice();
//		achoice.setFocusTraversable(false);
        achoice.setForeground(
			string2Color(rc.getString("folderlistForeground")));
        achoice.setBackground(
			string2Color(rc.getString("folderlistBackground")));
        achoice.setSelectionForeground(
			string2Color(rc.getString("folderlistSelectionForeground")));
        achoice.setSelectionBackground(
			string2Color(rc.getString("folderlistSelectionBackground")));
        achoice.addKeyListener(new KeyProcessor());
        achoice.addItemListener(new ItemEventCatcher(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                refreshFolders();
            }
        }));
        for (int i = 0; i < accounts.size(); i++)
            achoice.add(((Account) accounts.elementAt(i)).getName());
        folderpanel.add("North", achoice);
        SplitPanel sp2 = new SplitPanel(SplitPanel.VERTICAL);
        sp1.add(sp2);
        tmpsize = CSV.cutInt(rc.getString("maillistSize"));
        mailpanel = new SolidPanel(new BorderLayout(), tmpsize[0], tmpsize[1]);
        sp2.add(mailpanel);
		int scflag = TextArea.SCROLLBARS_BOTH;
		try {
			if (new Boolean(rc.getString("lineWrap")).booleanValue())
				scflag = TextArea.SCROLLBARS_VERTICAL_ONLY;
		} catch (MissingResourceException ex) {}
        body = new TextArea("", 0, 0, scflag);
        body.setEditable(false);
        body.setClickable(true);
		//body.setClickToFocus();
		//body.setFocusTraversable(false);
		body.setBackground(string2Color(rc.getString("mailbodyBackground")));
        body.setScrollbarThickness(scrollbarthickness);
        body.addKeyListener(new KeyProcessor());
		PopupMenu tmpmenu = body.getPopupMenu();
		tmpmenu.addSeparator();
		tmpmenu.add(createMailBodyPrefMenu());
		body.setPopupMenu(tmpmenu);
        sp2.add(body);
        createMenuBar();
        try {
            tmpsize = CSV.cutInt(rc.getString("mainwindowLocation"));
            this.setLocation(tmpsize[0], tmpsize[1]);
            tmpsize = CSV.cutInt(rc.getString("mainwindowSize"));
            // resize bug?
            this.setSize(tmpsize[0], tmpsize[1]);
            setVisible(true);
            this.setSize(tmpsize[0], tmpsize[1]);
        } catch (MissingResourceException ex) {
            pack();
            setVisible(true);
        }
		achoice.requestFocus();
        refreshFolders();
    }
    
    public static void main(String[] args) {
        MainWindow mw = new MainWindow();
    }
}
