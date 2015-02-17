package com.group7.eece411.A3;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class Agent implements Runnable{
	
	protected byte[] key;
	protected String decodeKey;
	protected byte[] value;
	protected NodeInfo target;
	protected Datastore db;
	
	public Agent(byte[] key, byte[] value) throws IOException {
		this(key);
		this.value = value;
	}
	
	public Agent(byte[] key) throws IOException {
		this.key = key;
		this.decodeKey = StringUtils.byteArrayToHexString(key);
		this.db = Datastore.getInstance();
		target = getResponsibleNode(key);
	}
	
	/*
	 * Get the node that responsible for the key, it can be the caller node or other remote nodes
	 */
	private NodeInfo getResponsibleNode(byte[] key) {
		List<Integer> allLocations = db.findAllLocations();
		Integer closestLocation = null;
		for(Integer loc : allLocations) {
			if(loc < key[0] && loc > closestLocation) {
				closestLocation = loc;
			}
		}
		if(closestLocation == null) { 
			//for key[0] between 0 and the location of first node
			closestLocation = allLocations.get(allLocations.size()-1);
		}
		return db.find(closestLocation);
	}
		
	public void run() {
		
	}
}
