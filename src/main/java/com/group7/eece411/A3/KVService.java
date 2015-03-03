package com.group7.eece411.A3;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import org.christianschenk.simplecache.SimpleCache;

public class KVService extends Service {

	public static final long REQUEST_TIMEOUT = 5;
	private SimpleCache<Packet> requestCache;
	
	public KVService(int period) throws UnknownHostException {
		super(period);
		this.requestCache = new SimpleCache<Packet>(REQUEST_TIMEOUT);
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
		NodeInfo target = Datastore.getInstance().getResponsibleNode(p.getHeader("key")[0]);
		
		switch (p.getHeader("command")[0]) {
			case 1: 
				this.putRequest(p, target);
				break;
			case 2: 
				this.getRequest(p, target);
				break;
			case 3: 
				this.removeRequest(p, target);
				break;
			case 4: 
				this.stop();
				break;
			case 21: 
				this.putRequest(p, target);
				break;
			case 22: 
				this.getRequest(p, target);
				break;
			case 23: 
				this.removeRequest(p, target);
				break;
			case 31: 
				this.putRespond(p, target);
				break;
			case 32: 
				this.getRespond(p, target);
				break;
			case 33: 
				this.removeRespond(p, target);
				break;
			default:
				this.client.send(Protocol.sendResponse(p, null, 5));
				break;
		}
	}
	
	private void getRequest(Packet packet, NodeInfo target) throws IOException {
		if(forwardRequest(packet, target)) return;
		byte[] value = target.get(packet.getStringHeader("key"));		
		Packet response = null;
		if(value == null) {
			Datastore.getInstance().addLog("INFO", "Cannot GET the value");
			response = Protocol.sendResponse(packet, null, 1);
		} else {
			Datastore.getInstance().addLog("INFO", "Value GET from key : "+packet.getStringHeader("key") + " is " +StringUtils.byteArrayToHexString(value));
			response = Protocol.sendResponse(packet, target.get(packet.getStringHeader("key")), 0);
		}
		Datastore.getInstance().storeCache(packet.getUIDString(), response);
		this.client.send(response);
	}
	
	private void putRequest(Packet packet, NodeInfo target) throws IOException {
		if(forwardRequest(packet, target)) return;
		Packet response = null;
		if(!target.put(packet.getStringHeader("key"), packet.getPayload())) {
			Datastore.getInstance().addLog("Info", "Cannot PUT (key,value) to "+target.getHost());
			response = Protocol.sendResponse(packet, null, 2);
		} else {
			Datastore.getInstance().addLog("Info", "PUT (key,value) to "+target.getHost());
			response = Protocol.sendResponse(packet, null, 0);				
		}
		Datastore.getInstance().storeCache(packet.getUIDString(), response);
		this.client.send(response);
	}
	
	private void removeRequest(Packet packet, NodeInfo target) throws IOException {
		if(forwardRequest(packet, target)) return;
		Packet response = null;
		if(target.get(packet.getStringHeader("key")) != null) {
			target.remove(packet.getStringHeader("key"));
			Datastore.getInstance().addLog("Info", "REMOVE key from "+target.getHost());
			response = Protocol.sendResponse(packet, null, 0);
		} else {
			Datastore.getInstance().addLog("Info", "Cannot REMOVE key from "+target.getHost());
			response = Protocol.sendResponse(packet, null, 1);
		}
		Datastore.getInstance().storeCache(packet.getUIDString(), response);
		this.client.send(response);
	}
	
	public void putRespond(Packet packet, NodeInfo target) {
		
	}
	
	public void getRespond(Packet packet, NodeInfo target) {
		
	}
	public void removeRespond(Packet packet, NodeInfo target) {
		
	}
	
	private Boolean forwardRequest(Packet packet, NodeInfo target) throws UnknownHostException, IOException {
		if(!Datastore.getInstance().isThisNode(target)) {
			Datastore.getInstance().addLog("INFO", "Forwarding to "+target.getHost());
			Packet requestPacket = Protocol.forwardRequest(packet);
			this.requestCache.put(requestPacket.getUIDString(), packet);
			this.client.send(requestPacket);
			return true;
		} return false;
	}
}
