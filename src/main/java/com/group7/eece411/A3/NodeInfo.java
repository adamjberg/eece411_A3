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
	private ConcurrentHashMap<String, String> kvStore;
	
	public NodeInfo (String host, int port, int location) {
		this.hostName = host;
		this.port = port;
		this.location = location;
		this.kvStore = new ConcurrentHashMap<String, String>();
	}

	public String get(String key) {
		return kvStore.get(key);
	}
	
	public boolean put(String key, String value) {
		if(this.kvStore.size() >= SPACESIZE) {
			return false;
		}
		this.kvStore.put(key, value);
		return true;
	}
	
	public void remove(int key) {
		this.kvStore.remove(key);
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

