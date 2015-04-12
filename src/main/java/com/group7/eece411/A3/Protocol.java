/**
 * 
 */
package com.group7.eece411.A3;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

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
	public static final int MIN_REQUEST_SIZE = KEY_SIZE_IN_BYTES + COMMAND_SIZE_IN_BYTES; //As A3 document describe, Value-length is optional
	public static final int MAX_REQUEST_SIZE = VALUE_LENGTH_SIZE_IN_BYTES + MAX_VALUE_LENGTH + MIN_REQUEST_SIZE;
	public static final int MIN_RESPONSE_SIZE = 1;
	public static final int MAX_RESPONSE_SIZE = MIN_RESPONSE_SIZE + VALUE_LENGTH_SIZE_IN_BYTES + MAX_VALUE_LENGTH;
	public static int counter = 0;
	/*
	 * Create response packet for a request we received earlier.
	 */
	public static Packet sendResponse(Packet req, byte[] value, int responseCode) {
		Header h = new Header();
		decodeUniqueId(req.getUID(), h);
		int response = 0;
		h.setField("response", new byte[]{(byte)( response + responseCode)});
		if(responseCode == 0 && value != null && value.length > 0 && value.length <= MAX_VALUE_LENGTH) {
			h.setField("value-length", ByteOrder.int2leb(value.length));
		}
		Packet p = new Packet(h, value);
		p.setDestinationIP(req.getDestinationIP());
		p.setDestinationPort(req.getDestinationPort());
		return p;
	}
	
	public static Packet respondToSender(Packet req, Packet res) {
		Packet clone = res.clone();
		decodeUniqueId(req.getSenderUID(), clone.getHeader());
		clone.setDestinationIP(req.getSourceIP());
		clone.setDestinationPort(req.getSourcePort());
		return clone;
	}
	/*
	 * Create request packet to forward
	 */
	public static Packet forwardRequest(Packet packet, NodeInfo target) throws UnknownHostException, IOException {
		Packet clone = packet.clone();
		byte[] uniqueId = generateUniqueID();
		decodeUniqueId(uniqueId, clone.getHeader());
		int forwardCommand = (ByteOrder.ubyte2int(packet.getHeader("command")[0]) + 20);
		clone.getHeader().setField("command", new byte[]{(byte)forwardCommand});
		
		ByteBuffer buffer = ByteBuffer.allocate(22).order(java.nio.ByteOrder.LITTLE_ENDIAN)
				.put(InetAddress.getByName(packet.getDestinationIP()).getAddress())
				.putShort((short)packet.getDestinationPort())
				.put(packet.getUID());
		clone.getHeader().setField("senderInfo", buffer.array());
		clone.setDestinationIP(target.getHost());
		clone.setDestinationPort(target.getPort());
		return clone;
	}
	
	/*
	 * Create request packet from bytes we received.  We are receiving a request.
	 */
	public static Packet receiveRequest(byte[] packet, String host, int port) {
		Packet p = null;
		Header header = new Header();
		decodeUniqueId(Arrays.copyOfRange(packet, 0, 16), header);
		byte[] inBytes = Arrays.copyOfRange(packet, 16, packet.length);
		if (inBytes.length > MAX_REQUEST_SIZE || inBytes.length < MIN_REQUEST_SIZE)	{
			if((int)inBytes[0] == 4 || inBytes.length == 1) {
				header.setField("command", new byte[] {inBytes[0]});
			} else {
				header.setField("command", new byte[] {99});
			}
			p = new Packet(header);
		} else {
			// Retrieve the data from the array
			header.setField("command", new byte[] {inBytes[0]});// The first elemt is the Command code
			byte[] keyBytes = Arrays.copyOfRange(inBytes, 1, KEY_SIZE_IN_BYTES+1);
			if(inBytes[0] > 0 && inBytes[0] < 4) {
				int hashCode = StringUtils.byteArrayToHexString(keyBytes).hashCode();
				byte[] tempKey = new byte[32];
				ByteOrder.int2leb(hashCode, tempKey, 0);
				header.setField("key", tempKey);
			} else {
				header.setField("key", keyBytes); // the [1:32) the elements are the length
			}
			if(inBytes[0] == 1 || inBytes[0] == 21) { //Only if it is a put command
				header.setField("value-length", Arrays.copyOfRange(inBytes, KEY_SIZE_IN_BYTES+1, MIN_REQUEST_SIZE+VALUE_LENGTH_SIZE_IN_BYTES)); // the [33:34) the elements are the length
				int val_len_int = ByteOrder.leb2int(header.getRawHeaderValue("value-length"), 0, VALUE_LENGTH_SIZE_IN_BYTES);
				if(inBytes[0] == 1) {
					if(MIN_REQUEST_SIZE + VALUE_LENGTH_SIZE_IN_BYTES + val_len_int > inBytes.length || 
							val_len_int > MAX_VALUE_LENGTH) { //Check the length of val
						header.setField("command", new byte[] {99}); 
						p = new Packet(header);
					} else if(val_len_int > 0) {
						p = new Packet(header, Arrays.copyOfRange(inBytes, MIN_REQUEST_SIZE+VALUE_LENGTH_SIZE_IN_BYTES, MIN_REQUEST_SIZE+VALUE_LENGTH_SIZE_IN_BYTES+val_len_int));
					} else {
						p = new Packet(header, new byte[0]);
					}
				} else { //code 21 internal put
					p = new Packet(header, Arrays.copyOfRange(inBytes, MIN_REQUEST_SIZE+VALUE_LENGTH_SIZE_IN_BYTES+22, MIN_REQUEST_SIZE+VALUE_LENGTH_SIZE_IN_BYTES+val_len_int+22));
				}
			} else {
				p = new Packet(header);
			}			
		}
		
		if(inBytes[0] > 20 && inBytes[0] < 24) {
			InetAddress senderIP;
			int start = MIN_REQUEST_SIZE;
			try {
				if(inBytes[0] == 21) {
					start = MIN_REQUEST_SIZE+VALUE_LENGTH_SIZE_IN_BYTES;
				}
				senderIP = InetAddress.getByAddress(Arrays.copyOfRange(inBytes, start, start+4));
				p.setSourceIP(senderIP.getHostAddress());
				p.setSourcePort(ByteOrder.leb2int(Arrays.copyOfRange(inBytes, start+4, start+6), 0, 2));
				p.setSenderUID(Arrays.copyOfRange(inBytes, start+6, start+22));
			} catch (UnknownHostException e) {
				Datastore.getInstance().addException("UnknownHostException", e);
			}
		}
		
		p.setDestinationIP(host);
		p.setDestinationPort(port);
		return p;
	}
	
	/*
	 * Create response packet from bytes we received.  We are receiving a response.
	 */
	public static Packet receiveResponse(byte[] packet, String host, int port) {
		Packet p = null;
		Header header = new Header();
		decodeUniqueId(Arrays.copyOfRange(packet, 0, 16), header);
		byte[] inBytes = Arrays.copyOfRange(packet, 16, packet.length);
		if (inBytes.length > MAX_RESPONSE_SIZE || inBytes.length < MIN_RESPONSE_SIZE)	{
			header.setField("response", new byte[] {99});
			p = new Packet(header);
		} else {
			// Retrieve the data from the array
			header.setField("response", new byte[] {inBytes[0]});// The first elemt is the Response code
			if(inBytes[0] == 0 && inBytes.length >= MIN_RESPONSE_SIZE + VALUE_LENGTH_SIZE_IN_BYTES) {
				header.setField("value-length", Arrays.copyOfRange(inBytes, MIN_RESPONSE_SIZE, MIN_RESPONSE_SIZE+VALUE_LENGTH_SIZE_IN_BYTES)); // the [1:2) the elements are the length
				int val_len_int = ByteOrder.leb2int(header.getRawHeaderValue("value-length"), 0, VALUE_LENGTH_SIZE_IN_BYTES);

				if(MIN_RESPONSE_SIZE + VALUE_LENGTH_SIZE_IN_BYTES + val_len_int > inBytes.length || 
						val_len_int > MAX_VALUE_LENGTH) { //Check the length of val
					return null;
				} else if(val_len_int > 0) {
					p = new Packet(header, Arrays.copyOfRange(inBytes, MIN_RESPONSE_SIZE+VALUE_LENGTH_SIZE_IN_BYTES, MIN_RESPONSE_SIZE+VALUE_LENGTH_SIZE_IN_BYTES+val_len_int));
				} else {
					p = new Packet(header);
				}
			}
			else {
				p = new Packet(header);
			}
		}
		p.setDestinationIP(host);
		p.setDestinationPort(port);
		return p;
	}

	/*
	 * Helper method to setup the header
	 */
	public static void decodeUniqueId(byte[] uniqueId, Header h) {
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
				.putShort((short)(new Random()).nextInt(1 << 15))
				.putLong(System.currentTimeMillis()+counter);
		counter++;
		return resultBuffer.array();
	}
}
