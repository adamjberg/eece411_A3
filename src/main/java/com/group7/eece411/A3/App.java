package com.group7.eece411.A3;

import java.io.IOException;
import java.net.SocketException;

public class App {
	private UDPClient listener;
	private Datastore db;

	public static void main(String[] args) throws Exception {
		System.setProperty("java.net.preferIPv4Stack", "true");

		App app = new App();
		app.run();

	}

	public App() throws IOException {
		this.db = new Datastore();
		Protocol res = new RequestData();
		NodeInfo n = this.db.findThisNode();
		this.listener = new UDPClient(n.getPort(), res);
		this.listener.setTimeout(0);
		this.listener.createSocket();
	}

	public void run() throws SocketException, IOException {
		do {
			try {
				RequestData receivedData = (RequestData) this.listener
						.receive();

				if (receivedData != null) {
					System.out.println(receivedData.key);

				} else {
					System.out.println("NULL");
				}
			} catch (NotFoundCmdException ex) {

			}
		} while (true);
	}
}
