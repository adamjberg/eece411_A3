/**
 * 
 */
package eece411_assg3_b;

import java.util.Hashtable;

/**
 * @author Ehsan
 *
 */
public abstract class Protocol {
	
	Hashtable<String, byte[]> source = new Hashtable<String,byte[]>();
	
	
	public abstract byte [] getHeader(); 
	public abstract String strigyfy();
	public abstract String Destrigyfy();

	
	
}
