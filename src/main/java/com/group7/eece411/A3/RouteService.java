package com.group7.eece411.A3;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class RouteService extends Service {

	private App program;
	private KVStore kvStore;
	
	public RouteService(int period, App app) throws UnknownHostException {
		super(period, 0);
		this.program = app;
		this.kvStore = new KVStore(this.client);
	}

	public void run() {
		if(Datastore.breakerPoint) return;
		try {
			ArrayList<Packet> tasklist = (ArrayList<Packet>) Datastore.getInstance().poll();
			for(Packet task : tasklist) {
				try {
					process(task);
				} catch(IOException ioe) {
					Datastore.getInstance().addLog("KVStore Error", Arrays.toString(ioe.getStackTrace()));
					this.client.responseTo(task, Protocol.sendResponse(task, null, 4));
				}
			}
		} catch(Exception e) {
			Datastore.getInstance().addException("Exception", e);
		}
    }
	
	private void process(Packet p) throws IOException {
		NodeInfo target = null;
		int cmdCode = ByteOrder.ubyte2int(p.getHeader("command")[0]);
		if(p.getHeader("key") != null) {
			target = Datastore.getInstance().getInternalResponsibleNode(p.getHeader("key")[0]);
		}
		switch (cmdCode) {
			case 1: 
				kvStore.putIn(p, target);
				break;
			case 2: 
				kvStore.getFrom(p, target);
				break;
			case 3: 
				kvStore.removeFrom(p, target);
				break;
			case 4:
				this.client.send(Protocol.sendResponse(p, null, 0));
				this.program.terminate();
				System.exit(0);
				break;
			case 21: 				
				kvStore.putIn(p, target);
				break;
			case 22: 
				kvStore.getFrom(p, target);
				break;
			case 23: 
				kvStore.removeFrom(p, target);
				break;
			case 24:
				this.stop();
				break;
			case 99:
				Datastore.getInstance().addLog("ERROR", "Error value length "+ByteOrder.leb2int(p.getHeader("value-length"), 2));
				this.client.responseTo(p, Protocol.sendResponse(p, null, 4));
				break;
			default:
				Datastore.getInstance().addLog("UNKNOWN", "Unknown Command Code "+p.getHeader("command")[0]);
				this.client.responseTo(p, Protocol.sendResponse(p, null, 5));
				break;
		}
	}
}
