package com.group7.eece411.A3;

import java.net.UnknownHostException;


public class Adapter implements Runnable {

	Packet packet;
	UDPClient listener;
	public Adapter(Packet packet) throws UnknownHostException {
		this.packet = packet;
		this.listener = new UDPClient(1234); //TODO auto-generate free port
	}
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
