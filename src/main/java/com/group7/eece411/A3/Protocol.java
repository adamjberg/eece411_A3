/**
 * 
 */
package com.group7.eece411.A3;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * This class holds the rules on how a Packet should be formatted.  While the Packet class provides
 * an abstraction to the formats of header, this class provides an abstraction to the
 * construction of packet.
 */
public class Protocol {
	
	public static final int COMMAND_SIZE_IN_BYTES = 1;
	public static final int MAX_VALUE_LENGTH = 15000;
	public static final int KEY_SIZE_IN_BYTES = 32;
	public static final int VALUE_LENGTH_SIZE_IN_BYTES = 2;
	public static final int MIN_MESSAGE_SIZE = KEY_SIZE_IN_BYTES + COMMAND_SIZE_IN_BYTES; //As A3 document describe, Value-length is optional
	public static final int MAX_MESSAGE_SIZE = VALUE_LENGTH_SIZE_IN_BYTES + MAX_VALUE_LENGTH + MIN_MESSAGE_SIZE;

	/*
	 * Create response packet for a request we received earlier.
	 */
	public static Packet sendResponse(Packet req, byte[] value, int responseCode) {
		Header h = new Header();
		decodeUniqueId(req.getUID(), h);
		h.setField("response", new byte[]{(byte)responseCode});
		if(responseCode == 0 && value != null && value.length > 0 && value.length <= MAX_VALUE_LENGTH) {			;
			h.setField("value-length", ByteOrder.int2leb(value.length));
		}
		Packet p = new Packet(h, value);
		p.setSourceIP(req.getSourceIp());
		p.setSourcePort(req.getSourcePort());
		return p;
	}
	
	/*
	 * Create a response packet from bytes we received. eg) we are receiving
	 * response from a request we sent earlier.
	 */
	public static Packet receiveResponse(byte[] packet, Packet req) {
		//TODO : check uniqueId etc.
		if(Arrays.equals(Arrays.copyOfRange(packet, 0, 16), req.getUID())) {
			
		}
		return null;
	}
	
	/*
	 * Create request packet for sending
	 */
	public static Packet sendRequest() {
		return null;
	}
	
	/*
	 * Create request packet to forward
	 */
	public static Packet forwardRequest(Packet packet) throws UnknownHostException, IOException {
		Packet clone = packet.clone();
		byte[] uniqueId = generateUniqueID();
		decodeUniqueId(uniqueId, clone.getHeader());
		clone.getHeader().setField("sourceIP", Arrays.copyOfRange(uniqueId, 0, 4));
		return clone;
	}
	
	/*
	 * Create request packet from bytes we received.  We are receiving a request.
	 */
	public static Packet receiveRequest(byte[] packet, String sourceIP, int sourcePort) {
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
		p.setSourceIP(sourceIP);
		p.setSourcePort(sourcePort);
		return p;
	}
	
	/*
	 * Helper method to setup the header
	 */
	private static void decodeUniqueId(byte[] uniqueId, Header h) {
		h.setUniqueId(uniqueId);
		h.setField("sourceIP", Arrays.copyOfRange(uniqueId, 0, 4));
		h.setField("port", Arrays.copyOfRange(uniqueId, 4, 6));
		h.setField("random", Arrays.copyOfRange(uniqueId, 6, 8));
		h.setField("timestamp", Arrays.copyOfRange(uniqueId, 8, 16));
	}
	
	public static byte[] generateUniqueID() throws UnknownHostException, IOException {
		ByteBuffer resultBuffer = ByteBuffer.allocate(16).order(java.nio.ByteOrder.LITTLE_ENDIAN)
				.put(InetAddress.getLocalHost().getAddress())
				.putShort((short)Datastore.getInstance().findThisNode().getPort())
				.put(new byte[2])
				.putLong(System.currentTimeMillis());
		return resultBuffer.array();
	}
}
