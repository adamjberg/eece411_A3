/**
 * 
 */
package eece411_assg3_b;

import javax.swing.Spring;

/**
 * @author Ehsan
 * 
 */
public class RequestData {

	public static final int KEY_SIZE = 32;
	public static final int VALUE_SIZE = 15000;

	public byte command;
	public byte[] key;
	public int val_len;
	public byte[] value;

	public RequestData(byte c, byte[] k, int v_l, byte[] r_v) {
		// constructor stub
		command = c;
		key = k;
		val_len = v_l;
		value = r_v;
	}

	public String commandMaker() throws IllegalArgumentException {
		StringBuilder sb = new StringBuilder();
		if (value.length > VALUE_SIZE || val_len > VALUE_SIZE) {
			throw new IllegalArgumentException();
		} else {
			sb.append(Conversions.byteToHexString(command));
			sb.append(Conversions.byteArrayToHexString(key));
			sb.append(String.valueOf(val_len));
			sb.append(Conversions.byteArrayToHexString(value));
		}
		return sb.toString();
	}

	public void  commandDestrigyfy() {
		// todo
	}

	/*
	 * 
	 * */
	public void put( byte [] k, byte [] res )
	{
		command = 0x01 ; 
		key = k ; 
		value = res;
		val_len = res.length;
	}

	/*
	 * Val is a HEX String
	 * */	
	public void put( String k, String val )
	{
		command = 0x01 ; 
		key = Conversions.hexStringToByteArray(k) ; 
		value = Conversions.hexStringToByteArray( val);
		val_len = value.length;
	}
	public void get( byte [] k )
	{
		command = 0x02 ; 
		key = k ; 
	}
}
