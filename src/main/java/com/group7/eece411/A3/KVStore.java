package com.group7.eece411.A3;

import java.io.IOException;
import java.net.UnknownHostException;

import org.christianschenk.simplecache.SimpleCache;

public class KVStore {

	public static final long REQUEST_TIMEOUT = 5;
	private SimpleCache<Packet> requestCache;
	private UDPClient client;
	
	public KVStore(UDPClient client) {
		this.client = client;
		this.requestCache = new SimpleCache<Packet>(REQUEST_TIMEOUT);
	}

	public void getFrom(Packet packet, NodeInfo target) throws IOException {
		if(forwardRequest(packet, target)) return;
		byte[] value = target.get(packet.getStringHeader("key"));		
		Packet response = null;
		if(value == null) {
			Datastore.getInstance().addLog("GET", "KEY does not exisit.");
			response = Protocol.sendResponse(packet, null, 1);
		} else {
			Datastore.getInstance().addLog("GET", "GET("+(new String(packet.getHeader("key"), "utf-8")) + ") : " +StringUtils.byteArrayToHexString(value));
			response = Protocol.sendResponse(packet, target.get(packet.getStringHeader("key")), 0);
		}
		Datastore.getInstance().storeCache(packet.getUIDString(), response);
		this.client.send(response);
	}
	
	public void putIn(Packet packet, NodeInfo target) throws IOException {
		if(forwardRequest(packet, target)) return;
		Packet response = null;
		if(!target.put(packet.getStringHeader("key"), packet.getPayload())) {
			Datastore.getInstance().addLog("PUT", "Failed PUT (key,value) to "+target.getHost());
			response = Protocol.sendResponse(packet, null, 2);
		} else {
			Datastore.getInstance().addLog("PUT", "(key,value) to "+target.getHost());
			response = Protocol.sendResponse(packet, null, 0);				
		}
		Datastore.getInstance().storeCache(packet.getUIDString(), response);
		this.client.send(response);
	}
	
	public void removeFrom(Packet packet, NodeInfo target) throws IOException {
		if(forwardRequest(packet, target)) return;
		Packet response = null;
		if(target.get(packet.getStringHeader("key")) != null) {
			target.remove(packet.getStringHeader("key"));
			Datastore.getInstance().addLog("REMOVE", "from "+target.getHost());
			response = Protocol.sendResponse(packet, null, 0);
		} else {
			Datastore.getInstance().addLog("REMOVE", "Failed REMOVE key from "+target.getHost());
			response = Protocol.sendResponse(packet, null, 1);
		}
		Datastore.getInstance().storeCache(packet.getUIDString(), response);
		this.client.send(response);
	}
	
	public void respondPut(Packet packet) {
		
	}
	
	public void respondGet(Packet packet) {
		
	}
	public void respondRemove(Packet packet) {
		
	}
	
	private Boolean forwardRequest(Packet packet, NodeInfo target) throws UnknownHostException, IOException {
		if(!Datastore.getInstance().isThisNode(target)) {
			Datastore.getInstance().addLog("INFO", "Forwarding to "+target.getHost());
			Packet requestPacket = Protocol.forwardRequest(packet);
			this.requestCache.put(requestPacket.getUIDString(), packet);
			//TODO create new Adapter to send requestPacket and listen for reply
			this.client.send(requestPacket);
			return true;
		} return false;
	}
}
