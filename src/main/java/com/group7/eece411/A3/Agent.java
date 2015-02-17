package com.group7.eece411.A3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
		target = getResponsibleNode(key);
		System.out.println(target.getHost());
	}
	
	/*
	 * Get the node that responsible for the key, it can be the caller node or other remote nodes
	 */
	private NodeInfo getResponsibleNode(byte[] key) throws IOException {
		List<Integer> allLocations = Datastore.getInstance().findAllLocations();
		Integer closestLocation = null;
		for(Integer loc : allLocations) {
			System.out.println(loc);
			if(loc < key[0] && loc > closestLocation) {
				closestLocation = loc;
			}
		}
		if(closestLocation == null) { 
			//for key[0] between 0 and the location of first node
			closestLocation = allLocations.get(allLocations.size()-1);
		}
		return Datastore.getInstance().find(closestLocation);
	}
		
	public void run() {
		
	}
}
