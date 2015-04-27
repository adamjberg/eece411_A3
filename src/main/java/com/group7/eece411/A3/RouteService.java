package com.group7.eece411.A3;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RouteService extends Service {

	private App program;
	private ExecutorService networkService;
	private UDPClient replier;
	
	public RouteService(int period, App app, UDPClient client) throws UnknownHostException {
		super(period, 0);
		this.program = app;
		this.networkService = Executors.newFixedThreadPool(2);
		this.replier = client;
	}

	public void run() {
		
		try {
			this.replier.sendQueue();
			ArrayList<Packet> tasklist = (ArrayList<Packet>) Datastore.getInstance().poll();
			for(Packet task : tasklist) {
				try {
					process(task);
				} catch(IOException ioe) {
					Datastore.getInstance().addLog("KVStore Error", Arrays.toString(ioe.getStackTrace()));
					this.client.responseTo(Protocol.sendResponse(task, null, 4));
				}
			}
		} catch(Exception e) {
			Datastore.getInstance().addException("Exception", e);
		}		
    }
	
	private void process(Packet p) throws IOException {
		Replicas target = Datastore.getInstance().findThisNode();
		Packet packetToSender = null;
		switch (p.getCode()) {
			case 1: 
				packetToSender = KVStore.putIn(p, target);
				this.replier.responseTo(packetToSender);
				if(packetToSender.getHeader("response")[0] == 0) {
					networkService.execute(new ReplicaAdapter(p));
				}
				break;
			case 2: 
				packetToSender = KVStore.getFrom(p, target);
				this.replier.responseTo(packetToSender);
				break;
			case 3: 
				packetToSender = KVStore.removeFrom(p, target);
				this.replier.responseTo(packetToSender);
				if(packetToSender.getHeader("response")[0] == 0) {
					networkService.execute(new ReplicaAdapter(p));
				}
				break;
			case 4:
				this.replier.responseTo(Protocol.sendResponse(p, null, 0));
				this.program.terminate();
				break;
			case 21: 		
				packetToSender = KVStore.putIn(p, target);
				this.client.responseTo(packetToSender, 2); //send ack 
				if(packetToSender.getHeader("response")[0] == 0) {
					networkService.execute(new ReplicaAdapter(p));
				}
				//packetToSender = Protocol.respondToSender(p, packetToSender);
				//this.client.responseTo(packetToSender); //send to the original requester
				break;
			case 22: 
				packetToSender = KVStore.getFrom(p, target);
				this.client.responseTo(packetToSender, 2); //send ack 
				break;
			case 23: 
				packetToSender = KVStore.removeFrom(p, target);
				this.client.responseTo(packetToSender, 2); //send ack 
				if(packetToSender.getHeader("response")[0] == 0) {
					networkService.execute(new ReplicaAdapter(p));
				}
				break;
			case 24:
				this.stop();
				break;
			case 31:
				target.put(p.getStringHeader("key"), p.getPayload());
				//Datastore.getInstance().addLog("REPLICA PUT", target.toString());
				break;
			case 33:
				target.remove(p.getStringHeader("key"));
				//Datastore.getInstance().addLog("REPLICA REMOVE", target.toString());
				break;
			default:
				this.replier.responseTo(Protocol.sendResponse(p, null, 5));
				//Datastore.getInstance().addLog("UNKNOWN", "Unknown Command Code "+p.getHeader("command")[0]);
				break;
		}
	}
}
