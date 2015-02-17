package com.group7.eece411.A3;

import java.io.IOException;
import java.net.SocketException;

public class App {
	private UDPClient client;
	private Datastore db;
	private NodeInfo thisNode;

	public static void main(String[] args) throws Exception {
		System.setProperty("java.net.preferIPv4Stack", "true");

		App app = new App();
		app.run();
	}

	public App() throws IOException {
		this.db = new Datastore();
		Protocol res = new RequestData();
		thisNode = this.db.findThisNode();
		this.client = new UDPClient(thisNode.getPort(), res);
		this.client.setTimeout(0);
		this.client.createSocket();
	}

	public void run() throws SocketException, IOException, NotFoundCmdException {
		do {
			try{
				Protocol p = this.client.receive();
				int result = p.getHeader("command");
				System.out.println(result);
				//TODO : pass to command
			} catch(NotFoundCmdException ex) {
    			System.out.println(ex.toString());
    			//TODO : send response 0x05: Unrecognized command
    		} catch(Exception e) {
    			System.out.println(e.getMessage());
    			//TODO : send response 0x04: Internal KVStore failure
    		}
			
			//Let command class handle all these complex logic 
			//App class is only responsible for receiving and respond if exception get thrown
			
			/*
			int location = keyToLocation(p.getRawHeader("key")); 
			
			NodeInfo destNodeInfo = this.db.find(location);
						
			if(destNodeInfo == thisNode)
			{
				// TODO: Handle the request based on the RequestCommand
			}
			else
			{
				forwardRequestTo(receivedData, destNodeInfo);
			}*/
			
		} while (true);
	}
	
	/*
	 * The simplest hash function, just take the first byte
	 */
	private int keyToLocation(String key)
	{
		byte[] bytes = StringUtils.hexStringToByteArray(key);
		return bytes[0] + 128;
	}
	
	/*
	 * This just passes the request on to the appropriate node
	 */
	private void forwardRequestTo(RequestData req, NodeInfo destNode) throws IllegalArgumentException, IOException
	{
		client.send(destNode.getHost(), destNode.getPort(), req);
	}

	public enum RequestCommand {
		INVALID(0x00), PUT(0x01), GET(0x02), REMOVE(0x03), SHUTDOWN(0x04);
		private byte value;

		RequestCommand(int code) {
			this.value = (byte) code;
		}
	}
}
