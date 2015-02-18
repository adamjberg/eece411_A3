package com.group7.eece411.A3;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

public class Header {
	public static final int LENGTH_IN_BYTES = 16;
	
	private InetAddress source;
	private int port;
	private short randomNum;
	private long timestamp;
	private byte[] uniqueId;
	
	public Header(InetAddress s, int p) {
		source = s;
		port = p;
	}
	
	public Header(int port) throws UnknownHostException {
		this.source = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
		this.port = port;
		this.uniqueId = generateUniqueID();
	}
	
	public Header(byte[] uniqueId) {
		this.uniqueId = uniqueId;
		//TODO : decode uniqueId and set timestamp, source, and port
	}
	
	public byte[] getUniqueId() {
		return this.uniqueId;
	}
	public byte[] generateUniqueID() {
		timestamp = System.currentTimeMillis();
		Random r = new Random(timestamp);
		randomNum = (short) r.nextInt();
		
		ByteBuffer resultBuffer = ByteBuffer.allocate(16).order(java.nio.ByteOrder.LITTLE_ENDIAN)
					.put(source.getAddress())
					.putShort((short)this.port)
					.put(new byte[2])
					.putLong(timestamp);
		this.uniqueId = resultBuffer.array();
		return this.uniqueId;
	}
	
	public void decode(byte[] message) throws UnknownHostException {
		ByteBuffer msgBuffer = ByteBuffer.wrap(message).order(java.nio.ByteOrder.LITTLE_ENDIAN);
		
		// Decode first 4 bytes into byte array which gets converted into InetAddress
		byte[] ip = new byte[4];
		msgBuffer.get(ip, 0, 4);
		source = InetAddress.getByAddress(ip);
		
		// Get Short (2 bytes) into port
		port = msgBuffer.getShort();
		randomNum = msgBuffer.getShort();
		
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
	
	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof Header) {
			Header that = (Header) other;
			result = this.source == that.source
					&& this.port == that.port
					&& this.randomNum == that.randomNum
					&& this.timestamp == that.timestamp;
		}
		return result;
	}
}
