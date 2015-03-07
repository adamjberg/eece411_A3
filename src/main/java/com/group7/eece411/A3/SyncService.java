package com.group7.eece411.A3;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class SyncService extends Service {

	public SyncService(int period) throws UnknownHostException {
		super(period, 9999);
		this.client.setTimeout(5000);
	}

	public void run() {
		//NodeInfo target = Datastore.getInstance().findRandomNode(); //find half nodes
		//Datastore.getInstance().addLog("SYNC", "PUSH "+target.getHost());
		DatagramPacket p;
		byte[] buffer;
		while(true) {
			buffer = new byte[16384];
			try {
				p = this.client.receive(buffer);
				sync(buffer);
			} catch (IOException e) {
				//Datastore.getInstance().addException("IOException", e);
				break;
			}
		}
		//Datastore.getInstance().addLog("HASHTABLE", Datastore.getInstance().findAll());
		
	}
	
	private void sync(byte[] bytes) {
		try {
			String str = new String(bytes, "utf8");
			//Datastore.getInstance().addLog("DEBUG", str);
			//TODO: sync with datastore and set node updated
		} catch (UnsupportedEncodingException e) {
			Datastore.getInstance().addLog("UnsupportedEncodingException", Arrays.toString(e.getStackTrace()));
		}
	}
}
