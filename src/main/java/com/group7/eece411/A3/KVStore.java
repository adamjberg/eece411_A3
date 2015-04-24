package com.group7.eece411.A3;

import java.io.IOException;

public class KVStore {
	private UDPClient client;
	
	public KVStore(UDPClient client) {
		this.client = client;
	}

	public Packet getFrom(Packet packet, Replicas target) throws IOException {
		byte[] value = target.get(packet.getStringHeader("key"));		
		//Packet response = null;
		if(value == null) {
			return Protocol.sendResponse(packet, null, 1);
		} else {
			return Protocol.sendResponse(packet, target.get(packet.getStringHeader("key")), 0);
		}
		//this.client.responseTo(response);
		//this.client.responseTo(response);
		//Datastore.getInstance().addLog("Send", response.toString());
		//return response;
	}
	
	public Packet putIn(Packet packet, Replicas target) throws IOException {
		//Packet response = null;
		if(!target.put(packet.getStringHeader("key"), packet.getPayload())) {
			return Protocol.sendResponse(packet, null, 2);
		} else {
			return Protocol.sendResponse(packet, null, 0);				
		}
		//Datastore.getInstance().addLog("Send", response.toString());
		//this.client.responseTo(response);
		//this.client.responseTo(response);
		//Datastore.getInstance().addLog("Send", response.toString());
		//return response;
	}
	
	public Packet removeFrom(Packet packet, Replicas target) throws IOException {
		//Packet response = null;
		if(target.get(packet.getStringHeader("key")) != null) {
			target.remove(packet.getStringHeader("key"));
			return Protocol.sendResponse(packet, null, 0);
		} else {
			return Protocol.sendResponse(packet, null, 1);
		}
		//this.client.responseTo(response);
		//this.client.responseTo(response);
		//Datastore.getInstance().addLog("Send", response.toString());
		//return response;
	}
}
