package com.group7.eece411.A3;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RouteService extends Service {

	private App program;
	private KVStore kvStore;
	private ExecutorService networkService;
	
	public RouteService(int period, App app) throws UnknownHostException {
		super(period, 0);
		this.program = app;
		this.kvStore = new KVStore(this.client);
		this.networkService = Executors.newFixedThreadPool(2);
	}

	public void run() {
		
		try {
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
		NodeInfo target = null;
		Packet packetToSender = null;
		int cmdCode = ByteOrder.ubyte2int(p.getHeader("command")[0]);
		if(p.getHeader("key") != null) {
			target = Datastore.getInstance().getInternalResponsibleNode(p.getHeader("key")[0]);
		}
		switch (cmdCode) {
			case 1: 
				if(kvStore.putIn(p, target).getHeader("response")[0] == 0) {
					networkService.execute(new ReplicaAdapter(p, target.isPredecessor()));
				}
				break;
			case 2: 
				kvStore.getFrom(p, target);
				break;
			case 3: 
				if(kvStore.removeFrom(p, target).getHeader("response")[0] == 0) {
					networkService.execute(new ReplicaAdapter(p, target.isPredecessor()));
				}
				break;
			case 4:
				this.client.send(Protocol.sendResponse(p, null, 0));
				this.program.terminate();
				break;
			case 21: 		
				packetToSender = kvStore.putIn(p, target);
				this.client.responseTo(packetToSender); //send ack again
				if(packetToSender.getHeader("response")[0] == 0) {
					networkService.execute(new ReplicaAdapter(p, target.isPredecessor()));
				}
				this.client.responseTo(packetToSender); //send ack again
				packetToSender = Protocol.respondToSender(p, packetToSender);
				this.client.responseTo(packetToSender); //send to the original requester
				break;
			case 22: 
				packetToSender = kvStore.getFrom(p, target);
				this.client.responseTo(packetToSender); //send ack again
				this.client.responseTo(packetToSender); //send ack again
				packetToSender = Protocol.respondToSender(p, packetToSender);
				this.client.responseTo(packetToSender);
				break;
			case 23: 
				packetToSender = kvStore.removeFrom(p, target);
				this.client.responseTo(packetToSender); //send ack again
				if(packetToSender.getHeader("response")[0] == 0) {
					networkService.execute(new ReplicaAdapter(p, target.isPredecessor()));
				}
				this.client.responseTo(packetToSender); //send ack again
				packetToSender = Protocol.respondToSender(p, packetToSender);
				this.client.responseTo(packetToSender);
				break;
			case 24:
				this.stop();
				break;
			case 31:
				target.put(p.getStringHeader("key"), p.getPayload());
				//Datastore.getInstance().addLog("REPLICA PUT", target.toString());
				break;
			case 33:
				if(target.get(p.getStringHeader("key")) != null) {
					target.remove(p.getStringHeader("key"));
					//Datastore.getInstance().addLog("REPLICA REMOVE", target.toString());
				}
				break;
			default:
				this.client.responseTo(Protocol.sendResponse(p, null, 5));
				//Datastore.getInstance().addLog("UNKNOWN", "Unknown Command Code "+p.getHeader("command")[0]);
				break;
		}
	}
}
