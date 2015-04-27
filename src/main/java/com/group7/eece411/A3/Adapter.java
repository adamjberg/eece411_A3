package com.group7.eece411.A3;

import java.io.IOException;
import java.net.UnknownHostException;


public class Adapter implements Runnable {

	Packet packet;
	Packet requestPacket;
	Replicas target;
	UDPClient client;
	UDPClient replier;

	public Adapter() throws UnknownHostException {
		this.client = new UDPClient();
		this.client.setTimeout(1000);
	}
	
	public Adapter(Packet packet, Replicas target, UDPClient replier) throws IOException {
		this.update(packet, target);
		this.client = new UDPClient();
		this.client.setTimeout(1000);
		this.replier = replier;
	}
	
	public void update(Packet packet, Replicas target) throws UnknownHostException, IOException {
		this.requestPacket = Protocol.forwardRequest(packet, target.getShortestPath());
		this.packet = packet;
		this.target = target;
	}
	
	public void run() {
		boolean isReceived = false;
		long time = 0;
		while(!isReceived) {
			//Datastore.getInstance().addLog("Forward", this.requestPacket.toString());
			
			try {
				this.client.send(this.requestPacket, 3);	
				time = System.currentTimeMillis();
				// Receive the response from the target node
				Packet response = this.client.receiveResponse(requestPacket.getUIDString());
				//Datastore.getInstance().addLog("Received from Forward", response.toString());
				time = System.currentTimeMillis() - time;
				this.client.closeSocket();
				isReceived = true;
				// Copy the old unique ID
				Protocol.decodeUniqueId(packet.getUID(), response.getHeader());
	
				// Set the destination to the original requester
				response.setDestinationIP(packet.getDestinationIP());
				response.setDestinationPort(packet.getDestinationPort());
						
				this.replier.addPacketToSend(response);

				this.target.recordResponseTime(time);
				// Store the uniqueID in the cache with the response 
				//Datastore.getInstance().storeCache(response.getUIDString(), response);
				//Datastore.getInstance().storeProcessCache(response.getUIDString(), false);		
				
				Datastore.getInstance().setNodeStatus(target.getLocation(), true);
			} catch (IOException ioe) {
				Datastore.getInstance().addLog("Received time out", target.getShortestPath().getHost());
				isReceived = fail();
			}
		}
	}
	
	private boolean fail() {
		// Mark the target node as down
		Datastore.getInstance().setNodeStatus(target.getLocation(), false);
		if(target.isOnline()) {
			requestPacket.setDestinationIP(target.getShortestPath().getHost());
			requestPacket.setDestinationPort(target.getShortestPath().getPort());
			this.client.setTimeout(1000);
			return false;
		} else {
			target = Datastore.getInstance().getResponsibleNode(packet.getHeader("key")[0]);
			
			if(Datastore.getInstance().isThisNode(target)) {
				// Store the packet back in the queue to be handles again
				Datastore.getInstance().queue(packet);
				this.client.closeSocket();
				return true;
			} else {
				requestPacket.setDestinationIP(target.getShortestPath().getHost());
				requestPacket.setDestinationPort(target.getShortestPath().getPort());
				this.client.setTimeout(1000);
				return false;
			}
		}
	}
}
