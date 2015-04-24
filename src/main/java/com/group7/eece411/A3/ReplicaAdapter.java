package com.group7.eece411.A3;

import java.io.IOException;
import java.net.UnknownHostException;

public class ReplicaAdapter implements Runnable {

	private Packet packet;
	private UDPClient client;
	
	public ReplicaAdapter(Packet p) throws UnknownHostException {
		this.packet = p;
		this.client = new UDPClient();
	}

	public void run() {
		try {
			this.client.forwardCopies(Protocol.forwardCopies(packet));
		} catch (IOException e) {
			Datastore.getInstance().addException("IOException", e);
		}
	}
}
