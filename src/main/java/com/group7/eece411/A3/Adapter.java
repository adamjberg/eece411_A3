package com.group7.eece411.A3;

import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;


public class Adapter implements Runnable {

	Packet packet;
	Packet requestPacket;
	NodeInfo target;
	UDPClient client;

	public Adapter(Packet packet, NodeInfo target) throws IOException {
		this.packet = packet;
		this.requestPacket = Protocol.forwardRequest(packet, target);
		this.target = target;
		this.client = new UDPClient();
		this.client.setTimeout(1500);
	}
	public void run() {
		boolean isReceived = false;
		while(!isReceived) {
			//Datastore.getInstance().addLog("Forward", this.requestPacket.toString());
			
			try {
				this.client.send(this.requestPacket);		
				this.client.send(this.requestPacket);	
				this.client.send(this.requestPacket);	
				// Receive the response from the target node
				Packet response = this.client.receiveResponse();
				//Datastore.getInstance().addLog("Received from Forward", response.toString());
				while(!response.getUIDString().equals(requestPacket.getUIDString())) {
					response = this.client.receiveResponse();
					//Datastore.getInstance().addLog("Received from Forward", response.toString());
					
				}
				this.client.closeSocket();
				isReceived = true;
				// Copy the old unique ID
				Protocol.decodeUniqueId(packet.getUID(), response.getHeader());
	
				// Set the destination to the original requester
				response.setDestinationIP(packet.getDestinationIP());
				response.setDestinationPort(packet.getDestinationPort());
				
				// Store the uniqueID in the cache with the response 
				Datastore.getInstance().storeCache(response.getUIDString(), response);
				Datastore.getInstance().storeProcessCache(response.getUIDString(), false);		
				
				Datastore.getInstance().setNodeStatus(target.getLocation(), true);
			} catch (IOException ioe) {
				Datastore.getInstance().addLog("IOException", this.requestPacket.toString());
				Datastore.getInstance().addException("IOException", ioe);
				isReceived = fail();
			}
		}
	}
	
	private boolean fail() {
		// Mark the target node as down
		Datastore.getInstance().setNodeStatus(target.getLocation(), false);
		
		target = Datastore.getInstance().getResponsibleNode(packet.getHeader("key")[0]);
		
		if(Datastore.getInstance().isThisNode(target)) {
			// Store the packet back in the queue to be handles again
			Datastore.getInstance().queue(packet);
			this.client.closeSocket();
			return true;
		} else {
			requestPacket.setDestinationIP(target.getHost());
			requestPacket.setDestinationPort(target.getPort());
			this.client.setTimeout(1500);
			return false;
		}
	}
}
