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
	private String sourceIP;
	private int sourcePort;
	
	public Packet(Header h) {
		this.header = h;
		this.payload = new byte[0];
		this.sourcePort = 0;
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
	
	public void setSourceIP(String ip) {
		this.sourceIP = ip;
	}
	
	public void setSourcePort(int port) {
		this.sourcePort = port;
	}
	
	public String getSourceIp() {
		if(this.sourceIP == null) {
			byte[] ip = this.header.getRawHeaderValue("sourceIP");
			this.sourceIP = ByteOrder.ubyte2int(ip[0])+"."+
					ByteOrder.ubyte2int(ip[1])+"."+
					ByteOrder.ubyte2int(ip[2])+"."+
					ByteOrder.ubyte2int(ip[3]);
		}
		return this.sourceIP;
	}
	
	public int getSourcePort() {
		if(this.sourcePort == 0) {
			this.sourcePort = ByteOrder.leb2int(this.header.getRawHeaderValue("port"), 0, 2);
		}
		return this.sourcePort;
	}
	
	public byte[] getUID() {
		return this.header.getUniqueId();
	}
	
	public Packet clone() {
		Packet p = new Packet(this.header.clone(), Arrays.copyOfRange(this.payload, 0, this.payload.length));
		p.setSourceIP(this.getSourceIp());
		p.setSourcePort(this.getSourcePort());
		return p;
	}
	
	public Long getDate() {
		ByteBuffer.allocate(8).order(java.nio.ByteOrder.LITTLE_ENDIAN);
		ByteBuffer bb = ByteBuffer.wrap(this.header.getRawHeaderValue("timestamp"));
		return bb.getLong();
	}
	
	@Override
	public String toString() {
		return "{sourceIP:\""+this.sourceIP+"\","
				+ "sourcePort:\""+this.sourcePort+"\","
						+ "header:"+this.header.toString()+"}";
	}
	
	public String getUIDString() {
		return this.header.getUIDString();
	}
}
