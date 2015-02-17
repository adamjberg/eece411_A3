/**
 * 
 */
package eece411_assg3_b;


/**
 * @author Ehsan
 *
 */
public abstract class Protocol {
	
	
	public abstract boolean isValidate() throws NotFoundCmdException;
	public abstract Protocol convert(byte[] d) throws NotFoundCmdException ;
	public abstract byte[] getHeader(String head) ;
	
}
