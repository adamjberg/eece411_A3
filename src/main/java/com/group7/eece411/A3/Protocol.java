/**
 * 
 */
package com.group7.eece411.A3;


/**
 * @author Ehsan
 *
 */
public abstract class Protocol {
	
	public abstract Protocol fromBytes(byte[] d);
	public abstract byte[] toBytes();
	
}
