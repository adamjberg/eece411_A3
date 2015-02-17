/**
 * 
 */
package com.group7.eece411.A3;

import java.net.UnknownHostException;


/**
 * @author Ehsan
 *
 */
public abstract class Protocol {
	
	public abstract Protocol fromBytes(byte[] d) throws UnknownHostException, NotFoundCmdException;
	public abstract byte[] toBytes();
}
