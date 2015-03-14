package com.group7.eece411.A3;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class App {
	private UDPClient listener; 
	private Datastore db; 
	private NodeInfo thisNode;
	private HashMap<String, Service> services;
	private ExecutorService networkService;

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
		this.services.put("kvStore", (new RouteService(50, this))); //every 0.1 sec
		this.services.put("sync", (new SyncService(10000))); //every 10 sec
		this.networkService = Executors.newFixedThreadPool(15);
	}

	public void run() {
		this.start();
		
		Packet p = null;
		do {
			try{
				p = this.listener.receive(); 
				//Datastore.getInstance().addLog("RECEIVE", p.toString());
				Packet cachePacket = Datastore.getInstance().getCache(p.getUIDString());
				if(cachePacket != null) {
					this.listener.responseCache(p, cachePacket);						
				} else if(Datastore.getInstance().getProcessCache(p.getUIDString()) == null 
						|| Datastore.getInstance().getProcessCache(p.getUIDString()).equals(new Boolean(false))) { 
					Datastore.getInstance().storeProcessCache(p.getUIDString(), true);
					//make sure we only process once
					
					int cmdCode = ByteOrder.ubyte2int(p.getHeader("command")[0]);
					NodeInfo target = Datastore.getInstance().findThisNode();
					if(p.getHeader("key") != null) {						
						if(cmdCode > 20 && cmdCode < 24) {
							target = Datastore.getInstance().forceTargetSelf(target);
						} else if(cmdCode > 0 && cmdCode < 4) {
							target = Datastore.getInstance().getResponsibleNode(p.getHeader("key")[0]);
						}						
					} 
					if(Datastore.getInstance().isThisNode(target)) {
						this.db.queue(p);
					} else {
						networkService.execute(new Adapter(p, target));
					}
				} 			
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
		networkService.shutdown();
	}
}
