/**
 * 
 */
package com.group7.eece411.A3;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Ehsan
 * 
 */
public class RequestData extends Protocol {

	public static final int COMMAND_SIZE_IN_BYTES = 1;
	public static final int MAX_VALUE_LENGTH = 15000;
	public static final int KEY_SIZE_IN_BYTES = 32;
	public static final int VALUE_LENGTH_SIZE_IN_BYTES = 2;
	public static final int MIN_MESSAGE_SIZE = KEY_SIZE_IN_BYTES
			+ COMMAND_SIZE_IN_BYTES + VALUE_LENGTH_SIZE_IN_BYTES;
	public static final int MAX_MESSAGE_SIZE = MAX_VALUE_LENGTH
			+ MIN_MESSAGE_SIZE;

	private Header header;
	public String key;
	public String value;
	private HashMap <String, byte[]>  HMdata;
	
	//For sending a request from this node to another, expecting respond through @port
	public RequestData(int cmd, String key, byte[] value, int port)
			throws NotFoundCmdException, UnsupportedEncodingException, UnknownHostException {
		this(new byte[]{(byte)cmd},
				StringUtils.hexStringToByteArray(key), 
				ByteOrder.int2leb(value.length), value, new Header(port));
	}

	//For receiving a request, fields will be set once data is received
	public RequestData() {
		HMdata = new HashMap<String, byte[]> ();
	}

	//Clone the protocol or use it for forwarding a request to another node
	public RequestData(byte[] c, byte[] k, byte[] val_len, byte[] r_v, Header h) {
		HMdata = new HashMap<String, byte[]> (); 
		HMdata.put("command", c);
		HMdata.put("key", k);
		HMdata.put("value-length", val_len);
		HMdata.put("value", r_v);
		this.header = h;
	}	
	
	@Override
	public Protocol fromBytes(byte[] totalBytes) throws UnknownHostException,
			NotFoundCmdException {
		this.header = new Header(Arrays.copyOfRange(totalBytes, 0, 16));
		byte[] inBytes = Arrays.copyOfRange(totalBytes, 16, totalBytes.length);
		if (inBytes.length > MAX_MESSAGE_SIZE || inBytes.length < MIN_MESSAGE_SIZE)
		{
			throw new NotFoundCmdException("Request size is incorrect.");
		}
		
		// Retrieve the data from the array
		HMdata.put("command", new byte[] {inBytes[0]});// The first elemt is the Response code 
		HMdata.put("key", Arrays.copyOfRange(inBytes, 1, KEY_SIZE_IN_BYTES+1)); // the [1:32) the elements are the length
		HMdata.put("value-length", Arrays.copyOfRange(inBytes, 33, 33+VALUE_LENGTH_SIZE_IN_BYTES)); // the [33:34) the elements are the length

		int val_len_int = ByteOrder.leb2int(HMdata.get("value-length"), 0, VALUE_LENGTH_SIZE_IN_BYTES);
		System.out.println("length of value : " + val_len_int);
		if(MIN_MESSAGE_SIZE + val_len_int > inBytes.length || val_len_int > MAX_VALUE_LENGTH) { //Check the length of val
			throw new NotFoundCmdException("Invalid value size."); 
		}
		
		HMdata.put("value", Arrays.copyOfRange(inBytes, MIN_MESSAGE_SIZE, MIN_MESSAGE_SIZE+val_len_int));
		
		return new RequestData(HMdata.get("command"), 
								HMdata.get("key"), 
								HMdata.get("value-length"), 
								HMdata.get("value"), new Header(Arrays.copyOfRange(totalBytes, 0, 16)));	
	}

	@Override	
	public Integer getHeaderCode(String Head) {
		byte[] bytes = HMdata.get(Head);
		if(Head.equals("command") || Head.equals("value-length")) {
			if(bytes.length >= 4) {
				return ByteOrder.leb2int(bytes, 0);
			}
			return ByteOrder.leb2int(bytes, 0, bytes.length);
		} 
		return -1;
	}
	
	public byte[] getRawHeader(String Head) {
		return HMdata.get(Head);
	}
	
	@Override
	public byte[] toBytes() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(getMessageSizeInBytes())
				.order(java.nio.ByteOrder.LITTLE_ENDIAN);
		byteBuffer.put(HMdata.get("command"));
		byteBuffer.put(HMdata.get("key"));
		byteBuffer.put(HMdata.get("value-length")); 
		if(HMdata.get("value") != null) {
			byteBuffer.put(HMdata.get("value")); 
		}
		return byteBuffer.array();
	}
	
	private int getMessageSizeInBytes() {
		if (value.length() > 0) {
			return MIN_MESSAGE_SIZE + HMdata.get("value").length;
		} else {
			return MIN_MESSAGE_SIZE;
		}
	}

	@Override
	public byte[] getUniqueId() {
		return this.header.getUniqueId();
	}
	
	@Override
	public Header getHeader() {
		return this.header;
	}
}
