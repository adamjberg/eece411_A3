package com.group7.eece411.A3;

import java.util.ArrayList;

public class Agent implements Runnable{
	
	protected Datastore ds;
	protected byte[] key;
	protected byte[] value;
	protected NodeInfo target;
	
	public Agent(Datastore ds, byte[] key, byte[] value) {
		this.ds = ds;
		this.key = key;
		this.value = value;
		target = getCorrespondNode(key);
	}
	
	public Agent(Datastore ds, byte[] key) {
		this.ds = ds;
		this.key = key;
	}
	
	private NodeInfo getCorrespondNode(byte[] key) {
		//ArrayList<NodeInfo> successors = (ArrayList<NodeInfo>) ds.findAll(); //Error here, need to debug
		//TODO : find the node that responsible for the key
		return null;
	}
	
	/*
	 * The simplest hash function, just take the first byte
	 */
	private int keyToLocation(String key)
	{
		byte[] bytes = StringUtils.hexStringToByteArray(key);
		return bytes[0];
	}
	
	public void run() {
		
	}
}
