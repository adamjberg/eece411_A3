package com.group7.eece411.A3;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;


public class Adapter implements Runnable {

	Packet packet;
	UDPClient listener;
	public Adapter(Packet packet) throws UnknownHostException {
		this.packet = packet;
		this.listener = new UDPClient(1234); //TODO auto-generate free port
	}
	public void run() {
		Datastore.getInstance().addLog("Forward", "forward request to "+this.packet.getDestinationIP());
		try {
			this.listener.send(this.packet);
		} catch (IOException ioe) {
			Datastore.getInstance().addLog("IOException", Arrays.toString(ioe.getStackTrace()));
		}
		// TODO listen for reply
		try {
			this.listener.receive();
		} catch (IOException e) {
			// TODO Timeout/ Target host is done
		}
	}

}
