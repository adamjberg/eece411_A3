package com.group7.eece411.A3;

import java.nio.ByteBuffer;
import java.util.Arrays;

/*
 * A Packet class is an abstraction of formating bytes.  This class contains
 * formatted header and payload.  Most important, it convert everything into
 * byte[] for UDPClient to send.
 */
public class Packet {

	private Header header;
	private byte[] payload;
	
	public Packet(Header h) {
		this.header = h;
		this.payload = new byte[0];
	}
	
	public Packet(Header h, byte[] payload) {
		this(h);
		this.payload = payload;
		if(payload == null) {
			this.payload = new byte[0];
		}
	}
	
	public byte[] getPayload() {
		return this.payload;
	}
	
	public Header getHeader() {
		return this.header;
	}
	public byte[] getHeader(String headerName) {
		return this.header.getRawHeaderValue(headerName);
	}
	
	public String getStringHeader(String headerName) {
		return this.header.getHeaderValue(headerName);
	}
	
	/*
	 * Convert a packet into a list of bytes
	 */
	public byte[] getBytes() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(this.header.size() + this.payload.length).order(java.nio.ByteOrder.LITTLE_ENDIAN);
		byteBuffer.put(this.header.getBytes());
		byteBuffer.put(this.payload);
		return byteBuffer.array();
	}
	
	public String getSourceIp() {
		byte[] ip = this.header.getRawHeaderValue("sourceIP");
		return ByteOrder.ubyte2int(ip[0])+"."+
				ByteOrder.ubyte2int(ip[1])+"."+
				ByteOrder.ubyte2int(ip[2])+"."+
				ByteOrder.ubyte2int(ip[3]);
	}
	
	public int getSourcePort() {
		return ByteOrder.leb2int(this.header.getRawHeaderValue("port"), 0, 2);
	}
	
	public byte[] getUID() {
		return this.header.getUniqueId();
	}
	
	public Packet clone() {
		return new Packet(this.header.clone(), Arrays.copyOfRange(this.payload, 0, this.payload.length));
	}
}
