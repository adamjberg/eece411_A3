/**
 * 
 */
package com.group7.eece411.A3;

import java.util.Arrays;


/**
 * @author Ehsan
 *
 */
public class Protocol {
	
	public static final int COMMAND_SIZE_IN_BYTES = 1;
	public static final int MAX_VALUE_LENGTH = 15000;
	public static final int KEY_SIZE_IN_BYTES = 32;
	public static final int VALUE_LENGTH_SIZE_IN_BYTES = 2;
	public static final int MIN_MESSAGE_SIZE = KEY_SIZE_IN_BYTES + COMMAND_SIZE_IN_BYTES; //As A3 document describe, Value-length is optional
	public static final int MAX_MESSAGE_SIZE = VALUE_LENGTH_SIZE_IN_BYTES + MAX_VALUE_LENGTH + MIN_MESSAGE_SIZE;

	public static Packet sendResponse(Packet req, byte[] value, int responseCode) {
		Header h = new Header();
		decodeUniqueId(req.getUID(), h);
		h.setField("response", new byte[]{(byte)responseCode});
		if(responseCode == 0 && value != null && value.length > 0 && value.length <= MAX_VALUE_LENGTH) {			;
			h.setField("value-length", ByteOrder.int2leb(value.length));
		}
		return new Packet(h, value);
	}
	
	public static Packet receiveRequest(byte[] packet) {
		Packet p = null;
		Header header = new Header();
		decodeUniqueId(Arrays.copyOfRange(packet, 0, 16), header);
		byte[] inBytes = Arrays.copyOfRange(packet, 16, packet.length);
		if (inBytes.length > MAX_MESSAGE_SIZE || inBytes.length < MIN_MESSAGE_SIZE)	{
			header.setField("command", new byte[] {99});
			p = new Packet(header);
		} else {
			// Retrieve the data from the array
			header.setField("command", new byte[] {inBytes[0]});// The first elemt is the Command code 
			header.setField("key", Arrays.copyOfRange(inBytes, 1, KEY_SIZE_IN_BYTES+1)); // the [1:32) the elements are the length
			if(inBytes[0] == 1) { //Only if it is a put command
				header.setField("value-length", Arrays.copyOfRange(inBytes, KEY_SIZE_IN_BYTES+1, MIN_MESSAGE_SIZE+VALUE_LENGTH_SIZE_IN_BYTES)); // the [33:34) the elements are the length
				int val_len_int = ByteOrder.leb2int(header.getRawHeaderValue("value-length"), 0, VALUE_LENGTH_SIZE_IN_BYTES);
				System.out.println("length of value : " + val_len_int);
				if(MIN_MESSAGE_SIZE + VALUE_LENGTH_SIZE_IN_BYTES + val_len_int > inBytes.length || 
						val_len_int > MAX_VALUE_LENGTH) { //Check the length of val
					header.setField("command", new byte[] {99}); 
					p = new Packet(header);
				} else if(val_len_int > 0) {
					p = new Packet(header, Arrays.copyOfRange(inBytes, MIN_MESSAGE_SIZE+VALUE_LENGTH_SIZE_IN_BYTES, MIN_MESSAGE_SIZE+VALUE_LENGTH_SIZE_IN_BYTES+val_len_int));
				} else {
					p = new Packet(header, new byte[0]);
				}
			} else {
				p = new Packet(header);
			}
			
			System.out.println("command : "+header.getRawHeaderValue("command")[0]);
			System.out.println("key : "+header.getHeaderValue("key"));				
		}
		return p;
	}
	
	private static void decodeUniqueId(byte[] uniqueId, Header h) {
		h.setUniqueId(uniqueId);
		h.setField("sourceIP", Arrays.copyOfRange(uniqueId, 0, 4));
		h.setField("port", Arrays.copyOfRange(uniqueId, 4, 6));
		h.setField("random", Arrays.copyOfRange(uniqueId, 6, 8));
		h.setField("timestamp", Arrays.copyOfRange(uniqueId, 8, 16));
	}
}
