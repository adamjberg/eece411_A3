/**
 * 
 */
package eece411_assg3_b;

import java.util.HashMap;

/**
 * @author Ehsan
 *
 */
public abstract class Protocol {
	
	
	public abstract boolean isValidate();
	public abstract Protocol convert(byte[] d);
	public abstract byte[] getHeader(String head);
	
}
