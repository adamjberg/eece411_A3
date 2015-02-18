package com.group7.eece411.A3;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class Agent implements Runnable{
	
	// This port is used to resend the request to the node that is responsible 
	protected int  sending_port = 7777; 
	
	// what does this decode key do?
	protected String decodeKey;
	
	
	/* Ehsan Added this so not sure if Danny meant this: 
	 * target Holds the information regarding the responsible node for a specified key. 
	 * That is the node target has the value for the key requested.
	 * */
	protected NodeInfo target;
	protected Datastore db;
	protected Protocol protocol;
	protected UDPClient client;
	
	public Agent(Protocol p) throws IOException {
		this.protocol = p;
		this.decodeKey = StringUtils.byteArrayToHexString(p.getRawHeader("key"));
		this.db = Datastore.getInstance();
		target = getResponsibleNode(p.getRawHeader("key"));
		this.client = new UDPClient();
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
	
	protected void respondUnscucessful(int responseCode) {
		try {
			Protocol res = new ResponseData(protocol.getHeader().clone(), responseCode, new byte[]{});
			this.client.send(protocol.getHeader().getIP().getHostAddress(), 
								protocol.getHeader().getPort(), res);
			this.client.closeSocket();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			//TODO : send to monitor server;
		} 
	}
}
