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
			ex.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
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
		this.services.put("monitor", (new MonitorService(10000))); //every 10 seconds
		this.services.put("kvStore", (new RouteService(100, this))); //every 0.05 sec
		this.services.put("sync", (new SyncService(10000))); //every 10 sec
	}

	public void run() {
		this.start();
		
		Packet p = null;
		do {
			try{
				p = this.listener.receive(); 
				this.db.queue(p);				
			}  catch(Exception e) {
				Datastore.getInstance().addLog("EXCEPTION", Arrays.toString(e.getStackTrace()));	
    		}
		} while (!this.services.values().isEmpty());
	}	
	
	public void start() {
		Iterator<Service> serviceIterator = this.services.values().iterator();
		while(serviceIterator.hasNext()) {
			serviceIterator.next().start();
		}
	}
	
	public void terminate() {
		Datastore.getInstance().addLog("SYSTEM", "The system is shutting down.");
		Iterator<Service> serviceIterator = this.services.values().iterator();
		while(serviceIterator.hasNext()) {
			serviceIterator.next().terminate();
			serviceIterator.remove();
		}
	}
}
