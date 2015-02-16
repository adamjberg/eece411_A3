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
	private int key;
	private ConcurrentHashMap<Integer, Integer> kvStore;
	
	public NodeInfo (String host, int port, int key) {
		this.hostName = host;
		this.port = port;
		this.key = key;
		this.kvStore = new ConcurrentHashMap<Integer, Integer>();
	}

	public Integer getValue(int key) {
		return kvStore.get(key);
	}
	
	public boolean put(int key, int value) {
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
	
	public int getKey() {
		return this.key;
	}
}

