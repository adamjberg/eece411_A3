/**
 * 
 */
package com.group7.eece411.A3;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
		
		clone.setDestinationIP(target.getHost());
		clone.setDestinationPort(target.getPort());
		return clone;
	}
	
	public static List<Packet> forwardCopies(Packet packet) throws UnknownHostException, IOException {
		List<Packet> l = new ArrayList<Packet>();
		int cmd = ByteOrder.ubyte2int(packet.getHeader("command")[0]);
		int forwardCommand = ( cmd > 20 ? cmd + 10 : cmd + 30);
		Packet clone;
		Collection<NodeInfo> list = Datastore.getInstance().findThisNode().getNodes();
		for(NodeInfo n : list) {
			if(n.isOnline() && !n.isSelf()) {
				clone = packet.clone();
				decodeUniqueId(generateUniqueID(), clone.getHeader());
				clone.getHeader().setField("command", new byte[]{(byte)forwardCommand});
				clone.setDestinationIP(n.getHost());
				clone.setDestinationPort(n.getPort());
				l.add(clone);
			}
		}
		return l;
	}
	/*
	 * Create request packet from bytes we received.  We are receiving a request.
	 */
	public static Packet receiveRequest(Header header, byte[] inBytes, String host, int port) {
		Packet p = null;
		if (inBytes.length > MAX_REQUEST_SIZE || inBytes.length < MIN_REQUEST_SIZE)	{
			if(inBytes.length >= 1 && (int)inBytes[0] == 4) {
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
			if(inBytes[0] == 1 || inBytes[0] == 21 || inBytes[0] == 31) { //Only if it is a put command
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
				} else {
					p = new Packet(header, Arrays.copyOfRange(inBytes, MIN_REQUEST_SIZE+VALUE_LENGTH_SIZE_IN_BYTES, MIN_REQUEST_SIZE+VALUE_LENGTH_SIZE_IN_BYTES+val_len_int));
				} 
			} else {
				p = new Packet(header);
			}			
		}
		
		p.setDestinationIP(host);
		p.setDestinationPort(port);
		p.setCode(ByteOrder.ubyte2int(header.getRawHeaderValue("command")[0]));
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
		p.setCode(ByteOrder.ubyte2int(header.getRawHeaderValue("response")[0]));
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
				.putShort((short)Datastore.getInstance().findThisNode().getShortestPath().getPort())
				.putShort((short)(new Random()).nextInt(1 << 15))
				.putLong(System.currentTimeMillis()+counter);
		counter++;
		return resultBuffer.array();
	}
}
