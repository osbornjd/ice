/**
 * 
 */
package org.eclipse.ice.developer.apps.ui;

/**
 * @author Anara Kozhokanova
 *
 */
public class OSPackage {
	private String name;

	/**
	 * 
	 */
	public OSPackage() {
		this.name = "";  // if not set to empty explicitly, 'null' will show in its bound field
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}