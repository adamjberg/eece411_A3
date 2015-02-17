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
				(new Thread(new AgentPut(p.getRawHeader("key"), p.getRawHeader("value")))).start();
				break;
			case 2: 
				(new Thread(new AgentGet(p.getRawHeader("key")))).start();
				break;
			case 3: 
				(new Thread(new AgentRemove(p.getRawHeader("key")))).start();
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
		byte[] command = new byte[]{1};
		byte[] key = new byte[32];
		byte[] decodeKey =  (new String("A Key!")).getBytes();
		if(decodeKey.length > 32) {
			throw new NotFoundCmdException("key exceeds the length 32!");
		}
		System.arraycopy(decodeKey, 0, key, 0, decodeKey.length);
		byte[] val_len = new byte[]{0, 1};
		byte[] value = new byte[]{2};
		RequestData rd = new RequestData(command, key, val_len, value);
		createAgent(rd);
		command = new byte[]{2};
		key = new byte[32];
		decodeKey = (new String("A Key!")).getBytes();
		if(decodeKey.length > 32) {
			throw new NotFoundCmdException("key exceeds the length 32!");
		}
		System.arraycopy(decodeKey, 0, key, 0, decodeKey.length);
		val_len = new byte[]{0, 1};
		value = new byte[]{2};
		RequestData rd2 = new RequestData(command, key, val_len, value);
		createAgent(rd2);
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
