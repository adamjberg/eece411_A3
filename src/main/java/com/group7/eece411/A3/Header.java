package com.group7.eece411.A3;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Header {
	public static final int LENGTH_IN_BYTES = 16;
	
	private byte[] uniqueId;
	private int length;
	private ConcurrentHashMap<String, String> fields;
	private ConcurrentHashMap<String, byte[]> rawFields;
	private ArrayList<String> order;
	
	public Header() {
		fields = new ConcurrentHashMap<String, String>();
		rawFields = new ConcurrentHashMap<String, byte[]>();
		order = new ArrayList<String>();
		this.length = 0;
	}
	
	public void setUniqueId(byte[] uid) {
		this.uniqueId = uid;
	}
	
	public void setField(String fieldName, byte[] value) {
		if(this.rawFields.get(fieldName) == null) {
			order.add(fieldName);			
		} else {
			this.length -= this.rawFields.get(fieldName).length;
		}
		this.rawFields.put(fieldName, value);
		this.length += this.rawFields.get(fieldName).length;
		this.fields.put(fieldName, StringUtils.byteArrayToHexString(value));
	}
		
	public void setField(String fieldName, String value) {
		if(this.rawFields.get(fieldName) == null) {
			order.add(fieldName);			
		} else {
			this.length -= this.rawFields.get(fieldName).length;
		}
		this.rawFields.put(fieldName, StringUtils.hexStringToByteArray(value));
		this.length += this.rawFields.get(fieldName).length;
		this.fields.put(fieldName, value);
	}
	
	public String getHeaderValue(String fieldName) {
		return this.fields.get(fieldName);
	}
	
	public byte[] getRawHeaderValue(String fieldName) {
		return this.rawFields.get(fieldName);
	}
	
	public byte[] getBytes() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(this.length).order(java.nio.ByteOrder.LITTLE_ENDIAN);
		for(int i = 0; i < this.order.size(); i++) {
			byteBuffer.put(this.rawFields.get(this.order.get(i)));
		}
		return byteBuffer.array();
	}
	
	public int size() {
		return this.length;
	}
	public Header clone() {
		Header result = new Header();
		result.fields = new ConcurrentHashMap<String, String>(this.fields);
		result.rawFields = new ConcurrentHashMap<String, byte[]>(this.rawFields);
		result.length = this.length;
		result.order = new ArrayList<String>(this.order);
		result.uniqueId = Arrays.copyOfRange(this.uniqueId, 0, this.uniqueId.length);
		return result;
	}
	
	public byte[] getUniqueId() {
		return this.uniqueId;
	}
	
	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof Header) {
			Header that = (Header) other;
			for(int i = 0; i < this.order.size(); i++) {
				if(!this.fields.get(this.order.get(i)).equals(that.fields.get(that.order.get(i)))) {
					return false;
				}
			}
			
			for(int i = 0; i < this.uniqueId.length; i++) {
				if(this.uniqueId[i] != that.uniqueId[i]) {
					return false;
				}
			}
			return that.length == this.length && this.uniqueId.length == that.uniqueId.length;
		}
		return result;
	}
}
