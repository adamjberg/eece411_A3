package com.group7.eece411.A3;

import java.io.IOException;

public class KVStore {

	public static final long REQUEST_TIMEOUT = 5;
	private UDPClient client;
	
	public KVStore(UDPClient client) {
		this.client = client;
	}

	public Packet getFrom(Packet packet, NodeInfo target) throws IOException {
		byte[] value = target.get(packet.getStringHeader("key"));		
		Packet response = null;
		if(value == null) {
			response = Protocol.sendResponse(packet, null, 1);
		} else {
			response = Protocol.sendResponse(packet, target.get(packet.getStringHeader("key")), 0);
		}
		this.client.responseTo(response);
		//Datastore.getInstance().addLog("Send", response.toString());
		return response;
	}
	
	public Packet putIn(Packet packet, NodeInfo target) throws IOException {
		Packet response = null;
		if(!target.put(packet.getStringHeader("key"), packet.getPayload())) {
			response = Protocol.sendResponse(packet, null, 2);
		} else {
			response = Protocol.sendResponse(packet, null, 0);				
		}
		this.client.responseTo(response);
		//Datastore.getInstance().addLog("Send", response.toString());
		return response;
	}
	
	public Packet removeFrom(Packet packet, NodeInfo target) throws IOException {
		Packet response = null;
		if(target.get(packet.getStringHeader("key")) != null) {
			target.remove(packet.getStringHeader("key"));
			response = Protocol.sendResponse(packet, null, 0);
		} else {
			response = Protocol.sendResponse(packet, null, 1);
		}
		this.client.responseTo(response);
		//Datastore.getInstance().addLog("Send", response.toString());
		return response;
	}
}
