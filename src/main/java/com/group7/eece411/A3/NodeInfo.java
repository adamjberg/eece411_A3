package com.group7.eece411.A3;

import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Ehsan
 *
 */
public class NodeInfo {
	public final static int SPACESIZE = 100000;
	private String hostName;
	private int port;
	private int location;
	private ConcurrentHashMap<String, byte[]> kvStore;
	private long spaceAvailable = 64 * 1024 * 1024;
	
	public NodeInfo (String host, int port, int location) {
		this.hostName = host;
		this.port = port;
		this.location = location;
		this.kvStore = new ConcurrentHashMap<String, byte[]>();
	}

	public byte[] get(String key) {
		return kvStore.get(key);
	}
	
	public boolean put(String key, byte[] value) {
		if(this.kvStore.size() >= SPACESIZE) {
			return false;
		}
		if(spaceAvailable - value.length < 0) {
			return false;
		}
		spaceAvailable -= value.length;
		this.kvStore.put(key, value);
		return true;
	}
	
	public void remove(String key) {
		byte[] bytesRemoved = this.kvStore.remove(key);
		spaceAvailable += bytesRemoved.length;
	}
	
	public String getHost() {
		return this.hostName;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public int getLocation() {
		return this.location;
	}
}

