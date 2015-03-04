package com.group7.eece411.A3;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class RouteService extends Service {

	private App program;
	private KVStore kvStore;
	
	public RouteService(int period, App app) throws UnknownHostException {
		super(period, 41172);
		this.program = app;
		this.kvStore = new KVStore(this.client);
	}

	public void run() {
		try {
			ArrayList<Packet> tasklist = (ArrayList<Packet>) Datastore.getInstance().poll();
			for(Packet task : tasklist) {
				Datastore.getInstance().addLog("RECEIVE", task.toString());
				try {
					process(task);
				} catch(IOException ioe) {
					Datastore.getInstance().addLog("KVStore Error", Arrays.toString(ioe.getStackTrace()));
					this.client.send(Protocol.sendResponse(task, null, 4));
				}
			}
		} catch(Exception e) {
			Datastore.getInstance().addException("Exception", e);
		}
    }
	
	private void process(Packet p) throws IOException {
		NodeInfo target = Datastore.getInstance().getResponsibleNode(p.getHeader("key")[0]);
		
		switch (p.getHeader("command")[0]) {
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
				this.program.terminate();
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
			case 31: 
				kvStore.forwardResponse(p);
				break;
			case 32: 
				kvStore.forwardResponse(p);
				break;
			case 33: 
				kvStore.forwardResponse(p);
				break;
			case 34:
				kvStore.forwardResponse(p);
				break;
			case 35:
				kvStore.forwardResponse(p);
				break;
			default:
				Datastore.getInstance().addLog("UNKNOWN", "Unknown Command Code "+p.getHeader("command")[0]);
				this.client.send(Protocol.sendResponse(p, null, 5));
				break;
		}
	}
	
	
}
