/**
 * 
 */
package com.group7.eece411.A3;

/**
 * @author Ehsan
 *
 */
public abstract class Protocol {
	
	public abstract boolean isValidate();
	public abstract Protocol convert(byte[] d);
	public abstract byte[] getHeader(String head);
	
}
