package com.group7.eece411.A3;

import java.io.IOException;
import java.net.SocketException;

public class App {
	private UDPClient client;
	private Datastore db;
	private NodeInfo thisNode;

	public static void main(String[] args) throws Exception {
		System.setProperty("java.net.preferIPv4Stack", "true");
		try {
			App app = new App();
			app.run();
		} catch(IOException ex) {
			System.out.println(ex.toString());
		} catch(Exception e) {
			System.out.println(e.toString());
			//TODO : send message to monitor server
		}
	}

	public App() throws IOException {
		this.db = Datastore.getInstance();
		thisNode = this.db.findThisNode();
		this.client = new UDPClient(thisNode.getPort());
		this.client.setTimeout(0);
		this.client.createSocket();
	}

	public void run() throws SocketException, IOException {
		Packet p = null;
		do {
			try{
				p = this.client.receive(); 
				router(p);
			}  catch(Exception e) {
    			System.out.println(e.toString());
    		}
			
			//Let agent class handle all the complex logic 
			//App class is only responsible for receiving and respond if exception get thrown			
			
		} while (true);
	}
	
	private void router(Packet p) throws IOException {
		System.out.println("Routing...");
		switch (p.getHeader("command")[0]) {
			case 1: 
				(new AgentPut(p)).run();
				break;
			case 2: 
				(new AgentGet(p)).run();
				break;
			case 3: 
				(new AgentRemove(p)).run();
				break;
			case 4: 
				break;
			default:
				this.client.send(Protocol.sendResponse(p, null, 5));
				break;
		}
	}
}
