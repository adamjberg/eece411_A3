package com.group7.eece411.A3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class Agent implements Runnable{
	
	protected byte[] key;
	protected byte[] value;
	protected NodeInfo target;
	
	public Agent(byte[] key, byte[] value) throws IOException {
		this(key);
		this.value = value;
	}
	
	public Agent(byte[] key) throws IOException {
		this.key = key;
		target = getCorrespondNode(key);
	}
	
	private NodeInfo getCorrespondNode(byte[] key) throws IOException {
		Collection<NodeInfo> list = Datastore.getInstance().findAll();
		ArrayList<NodeInfo> successors = new ArrayList<NodeInfo>(list);
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
