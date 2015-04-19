package com.group7.eece411.A3;

import java.io.IOException;
import java.net.UnknownHostException;

public class ReplicaAdapter implements Runnable {

	private Packet packet;
	private UDPClient client;
	private boolean isPredecessor;
	
	public ReplicaAdapter(Packet p, boolean isPredecessor) throws UnknownHostException {
		this.packet = p;
		this.client = new UDPClient();
		this.isPredecessor = isPredecessor;
	}

	public void run() {
		try {
			this.client.forwardCopies(Protocol.forwardCopies(packet, isPredecessor));
		} catch (IOException e) {
			Datastore.getInstance().addException("IOException", e);
		}
	}
}
