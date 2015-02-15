package com.group7.eece411.A3;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Header {
	private InetAddress source;
	private int port;
	private long timestamp;
	
	public Header(InetAddress s, int p) {
		source = s;
		port = p;
	}
	
	public Header() {
		source = null;
		port = -1;
	}
	
	public byte[] generateUniqueID() {
		ByteBuffer resultBuffer = ByteBuffer.allocate(16).order(java.nio.ByteOrder.LITTLE_ENDIAN)
					.put(source.getAddress())
					.putShort((short)this.port)
					.put(new byte[2])
					.putLong(System.currentTimeMillis());
		return resultBuffer.array();
	}
	
	public void decode(byte[] message) throws UnknownHostException {
		ByteBuffer msgBuffer = ByteBuffer.wrap(message).order(java.nio.ByteOrder.LITTLE_ENDIAN);
		
		// Decode first 4 bytes into byte array which gets converted into InetAddress
		byte[] ip = new byte[4];
		msgBuffer.get(ip, 0, 4);
		source = InetAddress.getByAddress(ip);
		
		// Get Short (2 bytes) into port
		port = msgBuffer.getShort();
		
		// Get timestamp
		timestamp = msgBuffer.getLong();
	}
	
	public byte[] decodeAndGetMessage(byte[] message) throws UnknownHostException {
		byte[] actualMessage = Arrays.copyOfRange(message, 16, message.length);
		decode(message);
		return actualMessage;
	}
	
	public InetAddress getIP() {
		return source;
	}
	
	public int getPort() {
		return port;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
}
