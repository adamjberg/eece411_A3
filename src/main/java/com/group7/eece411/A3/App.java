package com.group7.eece411.A3;

import java.io.IOException;
import java.net.SocketException;

import com.group7.eece411.A3.RequestData.RequestCommand;

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

	public void run() throws SocketException, IOException, NotFoundCmdException {
		do {
			RequestData receivedData = (RequestData) this.listener.receive();
		} while (true);
	}
}
