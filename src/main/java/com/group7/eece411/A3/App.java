package com.group7.eece411.A3;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class App {
	private UDPClient listener;
	private Datastore db;
	private NodeInfo thisNode;
	private HashMap<String, Service> services;

	public static void main(String[] args) throws Exception {
		System.setProperty("java.net.preferIPv4Stack", "true");
		try {
			App app = new App();
			app.run();
		} catch(IOException ex) {
			System.out.println(ex.toString());
		} catch(Exception e) {
			System.out.println(e.toString());
		}
	}

	//bootstrap
	public App() throws IOException {
		this.db = Datastore.getInstance();
		thisNode = this.db.findThisNode();
		this.listener = new UDPClient(thisNode.getPort());
		this.listener.setTimeout(0);
		this.listener.createSocket();
		this.services = new HashMap<String, Service>();
		this.services.put("monitor", (new MonitorService(30000)));
		this.services.put("kvStore", (new KVService(100)));
	}

	public void run() {
		Iterator<Service> serviceIterator = this.services.values().iterator();
		while(serviceIterator.hasNext()) {
			serviceIterator.next().start();
		}
		
		Packet p = null;
		do {
			try{
				p = this.listener.receive(); 
				Packet cachePacket = this.db.getCache(p.getUIDString());
				if( cachePacket == null) {
					this.db.queue(p);
				} else {
					//respond cache 
					this.db.addLog("DEBUG", cachePacket.getUIDString());
				}
			}  catch(Exception e) {
				Datastore.getInstance().addLog("EXCEPTION", Arrays.toString(e.getStackTrace()));	
    		}
		} while (true);
	}	
	
}
