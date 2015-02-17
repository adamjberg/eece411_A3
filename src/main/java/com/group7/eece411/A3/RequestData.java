/**
 * 
 */
package com.group7.eece411.A3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;


/**
 * @author Ehsan
 * 
 */
public class RequestData extends Protocol {

	HashMap <String, byte[]>  HMdata = new HashMap<String, byte[]> (); 

	public static final int MAX_REQUEST_SIZE = 15005; // 15000 + 2 + 32 + 1 bytes 
	public static final int KEY_SIZE = 32;
	public static final int VALUE_SIZE = 15000;

	public byte command;
	public byte[] key;
	public int val_len;
	public byte[] value;

	public RequestData(byte c, byte[] k, int v_l, byte[] r_v) {
		// constructor stub
		byte[] c_arr = new byte[1];
		c_arr[1] = c;
		HMdata.put("command", c_arr);
		HMdata.put("key", Conversions.int2leb(v_l, 0));
		HMdata.put("val_len", Conversions.int2leb(v_l, 0));
		HMdata.put("value", r_v);
	}
	
	public RequestData() {
		//nothing to do
	}

	public String requestMaker() throws NotFoundCmdException {
		StringBuilder sb = new StringBuilder();
		if (value.length > VALUE_SIZE || val_len > VALUE_SIZE) {
			throw new NotFoundCmdException("Invalid value size.");
		} else {
			sb.append(Conversions.byteArrayToHexString(HMdata.get("command")));
			sb.append(Conversions.byteArrayToHexString(HMdata.get("key")));
			sb.append(String.valueOf(HMdata.get("val_len")));
			sb.append(Conversions.byteArrayToHexString(HMdata.get("value")));
		}
		return sb.toString();
	}


	/**
	 * Remember you are constructing the object you are not actually sending the put command. 
	 * Val is a HEX String
	 * @throws IOException 
	 * */	
	public byte [] ConstructPut( String k, String val ) throws IOException
	{
		byte [] val_arr = Conversions.hexStringToByteArray(val);
		byte[] c_arr = { 0x01 };
		HMdata.put("command", c_arr);
		HMdata.put("key", Conversions.hexStringToByteArray(k));
		HMdata.put("val_len", Conversions.int2leb(val_arr.length, 0));
		HMdata.put("value", val_arr);
				
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(HMdata.get("command"));
		outputStream.write(HMdata.get("key"));
		outputStream.write(HMdata.get("val_len"));
		outputStream.write(HMdata.get("value"));

		return outputStream.toByteArray();
	}	
	
	
	public byte [] ConstructGet( byte [] key ) throws IOException
	{
		byte[] c_arr = { 0x02 };
		HMdata.put("command", c_arr);
		HMdata.put("key", key);
		HMdata.put("val_len", Conversions.int2leb(0, 0));
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(HMdata.get("command"));
		outputStream.write(HMdata.get("key"));
		outputStream.write(HMdata.get("val_len"));

		return outputStream.toByteArray();
	
	}	
	
	
	/*
	 * Prolly need to change the argument to int type.  
	 * */
	public byte [] ConstructRemove( byte [] k) throws IOException
	{
		byte[] c_arr = { 0x03 };
		HMdata.put("command", c_arr);
		HMdata.put("key", key);
		HMdata.put("val_len", Conversions.int2leb(0, 0));
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(HMdata.get("command"));
		outputStream.write(HMdata.get("key"));
		outputStream.write(HMdata.get("val_len"));

		return outputStream.toByteArray();
	
	}

	@Override
	public boolean isValidate() throws NotFoundCmdException {
		byte [] comm_temp =HMdata.get("command"); 	
		byte command_code =  comm_temp[0];
		
		byte [] key= HMdata.get("key"); 
		byte [] val_len = HMdata.get("val_len");
		int val_len_int = Conversions.leb2int(val_len, 0);		
		byte [] val = HMdata.get("value");
		
		//check the validity of the  data 
		if (!checkCommandCode(command_code))
		{
			throw new NotFoundCmdException("Invalid Command code.");
		}
		
		//Check the length of val
		if (val_len_int > VALUE_SIZE || val_len_int<0 || val.length >VALUE_SIZE )
		{
			throw new NotFoundCmdException("Invalid value length."); 
		}
		
		return true;
	}

	@Override
	public Protocol convert(byte[] d) throws NotFoundCmdException {
		if (d.length > MAX_REQUEST_SIZE)
		{
			throw new NotFoundCmdException("Maximum request size is reached.");
		}
		//Create a new instance to better preserve data.
		ResponseData rd  = new ResponseData();
		
		// Retrieve the data from the array
		byte command_code = d[0]; // The first elemt is the Response code 		
		
		byte [] key= Arrays.copyOfRange(d, 1, 33) ; // the [1:33) the elements are the length
		byte [] val_len = Arrays.copyOfRange(d, 33, 37) ; // the [33:37) the elements are the length
		int val_len_int = Conversions.leb2int(val_len, 0);		
		byte [] val = Arrays.copyOfRange(d, 37, d.length);
		
		//check the validity of the retrieved data 
		if (!checkCommandCode(command_code))
		{
			throw new NotFoundCmdException("Invalid Command code."); 
		}
		
		//Check the length of val
		if (val_len_int > VALUE_SIZE || val_len_int<0 || val.length >VALUE_SIZE )
		{
			throw new NotFoundCmdException("Invalid value size."); 
		}
		return rd;
	}
	
	
	/**
	 * Given a command byte this function checks the validity of the command.
	 * I have some hard coded stuff but dont laugh  
	 * */
	private boolean checkCommandCode(byte comm)
	{
		if ( comm == 0x01 || comm == 0x02 || comm == 0x03 || comm == 0x04)
		{
			return true; 
		}
		else 
			return true ;
	}

	@Override
	public byte[] getHeader(String Head) {
		return HMdata.get(Head);
	}
}
