package com.group7.eece411.A3;

import java.io.IOException;

public class KVStore {

	public static Packet getFrom(Packet packet, Replicas target) throws IOException {
		byte[] value = target.get(packet.getStringHeader("key"));		
		//Packet response = null;
		if(value == null) {
			return Protocol.sendResponse(packet, null, 1);
		} else {
			return Protocol.sendResponse(packet, value, 0);
		}
		//this.client.responseTo(response);
		//this.client.responseTo(response);
		//Datastore.getInstance().addLog("Send", response.toString());
		//return response;
	}
	
	public static Packet putIn(Packet packet, Replicas target) throws IOException {
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
	
	public static Packet removeFrom(Packet packet, Replicas target) throws IOException {
		//Packet response = null;
		if(target.remove(packet.getStringHeader("key")) == true) {
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
