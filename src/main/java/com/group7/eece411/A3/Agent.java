package com.group7.eece411.A3;

import java.io.IOException;
import java.util.List;

public class Agent implements Runnable{
		
	/* Ehsan Added this so not sure if Danny meant this: 
	 * target Holds the information regarding the responsible node for a specified key. 
	 * That is the node target has the value for the key requested.
	 * */
	protected NodeInfo target;
	protected Datastore db;
	protected Packet packet;
	protected UDPClient client;
	
	public Agent(Packet p) throws IOException {
		this.packet = p;
		this.db = Datastore.getInstance();
		target = getResponsibleNode(p.getHeader("key"));
		this.client = new UDPClient();
	}
	
	/*
	 * Get the node that responsible for the key, it can be the caller node or other remote nodes
	 */
	private NodeInfo getResponsibleNode(byte[] key) {
		System.out.println("Finding node...");
		List<Integer> allLocations = db.findAllLocations();
		Integer closestLocation = null;
		for(Integer loc : allLocations) {
			if(loc < key[0] && (closestLocation == null || loc > closestLocation)) {
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
	
	/*public static void respond(int responseCode, Packet p) {
		try {
			UDPClient c = new UDPClient();
			byte[] value = new byte[0];
			if(responseCode == 0 && p.getHeader("command")[0] == 2) {				
				value = p.getHeader("value");
				System.out.println("Sending value "+value.length);
			}
			Protocol res = new ResponseData(p.getHeader().clone(), responseCode, value);
			System.out.println("Sending response...");
			c.send(p.getHeader().getIP(), 
								p.getHeader().getPort(), res);
			c.closeSocket();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			//TODO : send to monitor server;
		} 
	}*/
	
}
