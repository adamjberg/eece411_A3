/**
 * 
 */
package com.group7.eece411.A3;


/**
 * @author Ehsan
 *
 */
public abstract class Protocol {
	
	
	public abstract boolean isValidate() throws NotFoundCmdException;
	public abstract Protocol convert(byte[] d) throws NotFoundCmdException ;
	public abstract byte[] getHeader(String head) ;
	
}
