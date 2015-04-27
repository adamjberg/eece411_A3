package com.group7.eece411.A3;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {
	private UDPClient listener; 
	private Datastore db; 
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
		this.listener = new UDPClient(this.db.findThisNode().getShortestPath().getPort());
		this.listener.setTimeout(0);
		this.listener.createSocket();
		this.services = new HashMap<String, Service>();
		this.services.put("monitor", (new MonitorService(15000))); //every 15 seconds
		this.services.put("kvStore", (new RouteService(50, this, this.listener))); //every 0.1 sec
		this.networkService = Executors.newFixedThreadPool(10);
	}

	public UDPClient getClient() {
		return this.listener;
	}
	
	public void run() {
		this.start();
		
		Packet p = null;
		int cmdCode;
		Replicas target = null;
		do {
			try{
				p = this.listener.receive(); 
				
				if(p != null) {
					//Datastore.getInstance().addLog("RECEIVE", p.toString());
					cmdCode = p.getCode();
					if(p.getHeader("key") != null && ((cmdCode > 0 && cmdCode < 4) || (cmdCode > 20 && cmdCode < 24))) {
						target = Datastore.getInstance().getResponsibleNode(p.getHeader("key")[0]);	
						if((cmdCode > 0 && cmdCode < 4) && !Datastore.getInstance().isThisNode(target)) {
							networkService.execute(new Adapter(p, target, this.listener));
						} else {
							if(cmdCode > 20 && cmdCode < 24) {
								Datastore.getInstance().forceTargetSelf(target);
							}
							this.db.queue(p);
						}
					} else {
						this.db.queue(p);
					}
				} 			
			}  catch(Exception e) {
				Datastore.getInstance().addLog("EXCEPTION", Arrays.toString(e.getStackTrace()));	
				e.printStackTrace();
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
		Iterator<Service> serviceIterator = this.services.values().iterator();
		while(serviceIterator.hasNext()) {
			serviceIterator.next().terminate();
			serviceIterator.remove();
		}
		networkService.shutdown();
		/*try {
			this.listener.responseTo(response);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		System.exit(0);
	}
}
