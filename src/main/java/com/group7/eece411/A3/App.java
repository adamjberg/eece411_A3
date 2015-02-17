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
	private ConcurrentHashMap<CommandCode, Command> respondActions;
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
    	setupRespondActions();
    	Protocol res = new RequestData();
    	this.listener = new UDPClient(this.db.findThisNode().getPort(), res);
    	this.listener.setTimeout(0);
    	this.listener.createSocket();
    }
    
    private void setupRespondActions() {
    	this.respondActions = new ConcurrentHashMap<CommandCode, Command>();
    	respondActions.put(CommandCode.PUT, new CommandPut(this.db));
    	respondActions.put(CommandCode.REMOVE, new CommandRemove(this.db));
    	respondActions.put(CommandCode.GET, new CommandGet(this.db));
    }
    
    public void run() throws SocketException, IOException {
    	do {
    		try {
    			this.listener.receive();
    		} catch(NotFoundCmdException ex) {
    			
    		}
    	} while(true);
    }	
	
	public enum CommandCode {
		PUT(0x01), GET(0x02), REMOVE(0x03), SHUTDOWN(0x04);
		private int value;
		CommandCode(int code) {
			this.value = code;
		}
	}
}
