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
			System.out.println(ex.getMessage());
		} catch(Exception e) {
			System.out.println(e.getMessage());
			//TODO : send message to monitor server
		}
	}

	public App() throws IOException {
		this.db = Datastore.getInstance();
		Protocol res = new RequestData();
		thisNode = this.db.findThisNode();
		this.client = new UDPClient(thisNode.getPort(), res);
		this.client.setTimeout(0);
		this.client.createSocket();
	}

	public void run() throws SocketException, IOException, NotFoundCmdException {
		do {
			try{
				//comment out temporary for testing
				//Protocol p = this.client.receive(); 
				//createAgent(p);
				testCase();
			} catch(NotFoundCmdException ex) {
    			System.out.println(ex.toString());
    			//TODO : send response 0x05: Unrecognized command
    		} catch(Exception e) {
    			System.out.println(e.getMessage());
    			//TODO : send response 0x04: Internal KVStore failure
    		}
			
			//Let agent class handle all the complex logic 
			//App class is only responsible for receiving and respond if exception get thrown			
			
		} while (true);
	}
	
	private void createAgent(Protocol p) throws NotFoundCmdException, IOException {
		switch (p.getHeaderCode("command")) {
			case 1: 
				(new Thread(new AgentPut(p))).start();
				break;
			case 2: 
				(new Thread(new AgentGet(p))).start();
				break;
			case 3: 
				(new Thread(new AgentRemove(p))).start();
				break;
			case 4: 
				break;
			default:
				throw new NotFoundCmdException("Invalid Command code."); 
		}
	}
	/*
	 * This is a test method 
	 */
	private void testCase() throws NotFoundCmdException, IOException {
		createAgent(new RequestData(1, "deadbeef", new byte[]{2}, 7777)); //put("deadbeef", x02);
		createAgent(new RequestData(2, "deadbeef", new byte[]{}, 7777)); //get("deadbeef");
		createAgent(new RequestData(1, "deadbeef22", new byte[]{1,2,3}, 7777)); //put("deadbeef22", x01,x02,x03);
		createAgent(new RequestData(2, "deadbeef22", new byte[]{}, 7777)); //get("deadbeef22");
		while(true);
	}
	/*
	 * This just passes the request on to the appropriate node
	 */
	private void forwardRequestTo(RequestData req, NodeInfo destNode) throws IllegalArgumentException, IOException
	{
		client.send(destNode.getHost(), destNode.getPort(), req);
	}
}
