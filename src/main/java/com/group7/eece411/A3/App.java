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
			RequestData receivedData = (RequestData) this.client.receive();
			
			int location = keyToLocation(receivedData.key);
			NodeInfo destNodeInfo = this.db.find(location);
						
			if(destNodeInfo == thisNode)
			{
				// TODO: Handle the request based on the RequestCommand
			}
			else
			{
				forwardRequestTo(receivedData, destNodeInfo);
			}
			
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
}
