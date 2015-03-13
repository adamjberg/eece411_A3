package com.group7.eece411.A3;

import java.io.IOException;

public class KVStore {

	public static final long REQUEST_TIMEOUT = 5;
	private UDPClient client;
	
	public KVStore(UDPClient client) {
		this.client = client;
	}

	public void getFrom(Packet packet, NodeInfo target) throws IOException {
		byte[] value = target.get(packet.getStringHeader("key"));		
		Packet response = null;
		if(value == null) {
			Datastore.getInstance().addLog("GET", "KEY does not exisit.");
			response = Protocol.sendResponse(packet, null, 1);
		} else {
			Datastore.getInstance().addLog("GET", "GET("+(new String(packet.getHeader("key"), "utf-8")) + ") : " +StringUtils.byteArrayToHexString(value));
			response = Protocol.sendResponse(packet, target.get(packet.getStringHeader("key")), 0);
		}
		this.client.responseTo(packet, response);
	}
	
	public void putIn(Packet packet, NodeInfo target) throws IOException {
		Packet response = null;
		if(!target.put(packet.getStringHeader("key"), packet.getPayload())) {
			Datastore.getInstance().addLog("PUT", "Failed PUT (key,value) to "+target.getHost());
			response = Protocol.sendResponse(packet, null, 2);
		} else {
			Datastore.getInstance().addLog("PUT", "(key,value) to "+target.getHost());
			response = Protocol.sendResponse(packet, null, 0);				
		}
		this.client.responseTo(packet, response);
	}
	
	public void removeFrom(Packet packet, NodeInfo target) throws IOException {
		Packet response = null;
		if(target.get(packet.getStringHeader("key")) != null) {
			target.remove(packet.getStringHeader("key"));
			Datastore.getInstance().addLog("REMOVE", "from "+target.getHost());
			response = Protocol.sendResponse(packet, null, 0);
		} else {
			Datastore.getInstance().addLog("REMOVE", "Failed REMOVE key from "+target.getHost());
			response = Protocol.sendResponse(packet, null, 1);
		}
		this.client.responseTo(packet, response);
	}
}
