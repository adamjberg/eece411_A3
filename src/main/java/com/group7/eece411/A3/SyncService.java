package com.group7.eece411.A3;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

public class SyncService extends Service {

	public SyncService(int period) throws UnknownHostException {
		super(period, 9999);
		this.client.setTimeout(100);
	}

	public void run() {
		//NodeInfo target = Datastore.getInstance().findRandomNode(); //find half nodes
		//Datastore.getInstance().addLog("SYNC", "PUSH "+target.getHost());
		Packet p;
		try {
			while(true) {
				p = this.client.receive();
				Datastore.getInstance().queue(p);
		/*
		Collection<NodeInfo> successors = Datastore.getInstance().findAll();
		successors.remove(Datastore.getInstance().findThisNode()); // Remove yourself from successor list
		
		// Send messages to all successors
		for (NodeInfo successor : successors) {
			Datastore.getInstance().addLog("SYNC", "PUSH "+ successor.getHost()+":"+successor.getPort());
			try {
				client.send(successor.getHost(), 9999, Datastore.getInstance().findThisNode().getKVString().getBytes());
			} catch (IOException e) {
				Datastore.getInstance().addException("IOException", e);
			}
			
		}
		
		DatagramPacket p;
		byte[] buffer;
		while(true) {
			buffer = new byte[16384];
			try {
				p = this.client.receive(buffer);
				sync(buffer);
			} catch (IOException e) {
				Datastore.getInstance().addException("IOException", e);
		*/
			}
		} catch (IOException e) {
			//Datastore.getInstance().addException("IOException", e);
		}
		
	}
	
	private void sync(byte[] bytes) {
		try {
			String str = new String(bytes, "utf8");
			Datastore.getInstance().addLog("DEBUG Datastore SYNC", str);
			//TODO: sync with datastore and set node updated
		} catch (UnsupportedEncodingException e) {
			Datastore.getInstance().addLog("UnsupportedEncodingException", Arrays.toString(e.getStackTrace()));
		}
	}
}
