package com.group7.eece411.A3;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KVService extends Service {

	public KVService(int period) throws UnknownHostException {
		super(period);
	}

	public void run() {
		try {
			ArrayList<Packet> tasklist = (ArrayList<Packet>) Datastore.getInstance().poll();
			for(Packet task : tasklist) {
				Datastore.getInstance().addLog("DEBUG", "RECEIVE : "+task.toString());
				try {
					process(task);
				} catch(IOException ioe) {
					Datastore.getInstance().addLog("KVStore Error", Arrays.toString(ioe.getStackTrace()));
					this.client.send(Protocol.sendResponse(task, null, 4));
				}
			}
		} catch(Exception e) {
			Datastore.getInstance().addLog("Exception", Arrays.toString(e.getStackTrace()));
		}
    }
	
	private void process(Packet p) throws IOException {
		NodeInfo target = getResponsibleNode(p.getHeader("key"));
		switch (p.getHeader("command")[0]) {
			case 1: 
				this.putAction(p, target);
				break;
			case 2: 
				this.getAction(p, target);
				break;
			case 3: 
				this.removeAction(p, target);
				break;
			case 4: 
				break;
			default:
				this.client.send(Protocol.sendResponse(p, null, 5));
				break;
		}
	}
	
	private void getAction(Packet packet, NodeInfo target) throws IOException {
		byte[] value = null;
		Packet response = null;
		if(Datastore.getInstance().isThisNode(target)) {
			value = target.get(packet.getStringHeader("key"));			
			if(value == null) {
				Datastore.getInstance().addLog("INFO", "Cannot GET the value");
				response = Protocol.sendResponse(packet, null, 1);
			} else {
				Datastore.getInstance().addLog("INFO", "Value GET from key : "+packet.getStringHeader("key") + " is " +StringUtils.byteArrayToHexString(value));
				response = Protocol.sendResponse(packet, target.get(packet.getStringHeader("key")), 0);
			}
			Datastore.getInstance().storeCache(packet.getUIDString(), response);
			this.client.send(response);
		} else {
			Datastore.getInstance().addLog("INFO", "Forwarding to "+target.getHost());
			//TODO: construct and send request packet
			this.client.send(target.getHost(), target.getPort(), packet);	
		}
	}
	
	private void putAction(Packet packet, NodeInfo target) throws IOException {
		System.out.println("Try to PUT (key,value) to "+target.getHost() +"...");
		/*try {
			if(db.isThisNode(target)) {
				if(!target.put(this.packet.getStringHeader("key"), this.packet.getPayload())) {
					this.client.send(Protocol.sendResponse(this.packet, null, 2));
				} else {
					System.out.println("PUT key : "+this.packet.getStringHeader("key")+
							", value : "+StringUtils.byteArrayToHexString(this.packet.getPayload()) + 
							" in "+target.getHost());
					this.client.send(Protocol.sendResponse(this.packet, null, 0));
					
				}
			} else {
				System.out.println("Forwarding to "+target.getHost());
				this.client.send(target.getHost(), target.getPort(), packet);	
			}*/
	}
	
	private void removeAction(Packet packet, NodeInfo target) throws IOException {
		System.out.println("Try to REMOVE key from "+target.getHost());
		/*
			if(db.isThisNode(target)) {
				if(target.get(this.packet.getStringHeader("key")) != null) {
					target.remove(this.packet.getStringHeader("key"));
					System.out.println("REMOVED "+this.packet.getStringHeader("key"));
					this.client.send(Protocol.sendResponse(this.packet, null, 0));
				} else {
					this.client.send(Protocol.sendResponse(this.packet, null, 1));
				}
			} else {
				// send remote request
				System.out.println("Forwarding to "+target.getHost());
				this.client.send(target.getHost(), target.getPort(), packet);	
			}*/
	}
	/*
	 * Get the node that responsible for the key, it can be the caller node or other remote nodes
	 */
	private NodeInfo getResponsibleNode(byte[] key) {
		List<Integer> allLocations = Datastore.getInstance().findAllLocations();
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
		return Datastore.getInstance().find(closestLocation);
	}
}
