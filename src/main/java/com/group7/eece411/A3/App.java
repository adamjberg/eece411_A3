package com.group7.eece411.A3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.group7.eece411.A3.UDPClient;

public class App {
	private UDPClient listener;
	private Datastore db;

    public static void main( String[] args ) throws Exception {
    	System.setProperty("java.net.preferIPv4Stack", "true");

    	try {
	    	App app = new App();	
			app.run();

    	} catch(Exception ex) {
    		System.out.println(ex.toString());
    	}
    		
    }

    public App() throws IOException {
    	this.db = new Datastore();
    	Protocol res = new RequestData();
    	this.listener = new UDPClient(this.db.findThisNode().getPort(), res);
    	this.listener.setTimeout(0);
    	this.listener.createSocket();
    }
    
    public void run() throws SocketException, IOException {
    	do {
    		try {
    			RequestData receivedData = (RequestData) this.listener.receive();
    			
    			System.out.println(receivedData.key);
    		} catch(NotFoundCmdException ex) {
    			
    		}
    	} while(true);
    }	
}
