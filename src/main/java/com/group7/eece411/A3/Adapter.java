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
		this.client.setTimeout(200);
	}
	public void run() {
		Datastore.getInstance().addLog("Forward", this.requestPacket.toString());
		int count = 3;
		while(count > 0) {
			try {
				this.client.send(this.requestPacket);
			} catch (IOException ioe) {
				Datastore.getInstance().addLog("IOException", Arrays.toString(ioe.getStackTrace()));
			}
			try {
				// Receive the response from the target node
				Packet response = this.client.receiveResponse();
				// Copy the old unique ID
				Protocol.decodeUniqueId(packet.getUID(), response.getHeader());
	
				// Set the destination to the original requester
				response.setDestinationIP(packet.getDestinationIP());
				response.setDestinationPort(packet.getDestinationPort());

				this.client.responseTo(packet, response);
				// Store the uniqueID in the cache with the response 
				this.client.closeSocket();
				break;	
			} catch (IOException e) {
				count--;
				if(count <= 0) {
					Datastore.getInstance().addLog("Timeout", target.getHost() + " is unreachable.");
					// Mark the target node as down
					Datastore.getInstance().setNodeStatus(target.getLocation(), false);
					
					target = Datastore.getInstance().getResponsibleNode(packet.getHeader("key")[0]);
					
					if(Datastore.getInstance().isThisNode(target)) {
						// Store the packet back in the queue to be handles again
						Datastore.getInstance().queue(packet);
						this.client.closeSocket();
					} else {
						count = 3;
						this.client.setTimeout(200);
					}
				} else {
					this.client.setTimeout(this.client.getTimeout()*2);
					try {
						this.client.createSocket();
					} catch (SocketException e1) {
						Datastore.getInstance().addException("SocketException", e1);
						break;
					}
				}
			}
		}
	}
}
