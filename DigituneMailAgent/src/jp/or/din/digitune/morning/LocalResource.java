/*
 * Local Resource Handle class
 *		1998/05/29 (C)Copyright T.Kazawa(Digitune)
 */

package jp.or.din.digitune.morning;

import jp.or.din.digitune.util.ResourceProperties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.MissingResourceException;

public class LocalResource {
	static final String RUNTIME = "runtime.properties";
	static LocalResource lr = null;
	ResourceProperties rc = null;
	
	protected LocalResource() {
		rc = new ResourceProperties("jp.or.din.digitune.morning.resource");
		try {
			FileInputStream fin = new FileInputStream(RUNTIME);
			rc.load(fin);
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static LocalResource getResource() {
		if (lr == null) {
			lr = new LocalResource();
		}
		return lr;
	}
	
	public void save() {
		if (rc != null) {
			try {
				FileOutputStream fileout = new FileOutputStream(RUNTIME);
				rc.save(fileout, "Morning (DMA) Runtime Resource");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public String getString(String key) throws MissingResourceException {
		if (rc != null)
			return rc.getProperty(key);
		else
			return null;
	}
	
	public void setString(String key, String value) {
		if (rc != null)
			rc.put(key, value);
	}
	
	public void remove(String key) {
		if (rc != null)
			rc.remove(key);
	}
}
