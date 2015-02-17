package com.group7.eece411.A3;

import java.util.ArrayList;

public class AgentGet extends Agent {

	public AgentGet(Datastore ds, byte[] key) {
		super(ds, key);
	}

	@Override
	public void run() {
		
		System.out.println("get a value");
		ArrayList<NodeInfo> successors = (ArrayList<NodeInfo>) ds.findAll();
		//TODO : find the node that responsible for the key
	}
	
	/*
	 * The simplest hash function, just take the first byte
	 */
	private int keyToLocation(String key)
	{
		byte[] bytes = StringUtils.hexStringToByteArray(key);
		return bytes[0];
	}
	

}
