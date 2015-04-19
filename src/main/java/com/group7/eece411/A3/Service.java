package com.group7.eece411.A3;

import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Service extends TimerTask{

	protected UDPClient client;
	protected Timer timer;
	protected int period;
	
	public Service() throws UnknownHostException {
		this.client = new UDPClient();
	}
	
	public Service(int period, int port) throws UnknownHostException {
		this.client = new UDPClient(port);
		this.period = period;
	}
	
	public Service(int period) {
		this.period = period;
	}
	
	public void run() 
    {
		System.out.println("Do Nothing");
    }
	
	public void start() {
		this.timer = new Timer(true); //daemon thread
		this.timer.schedule(this, 0, this.period);
	}

    
    public void stop() {
    	this.timer.cancel();    	
    }
    
    public void terminate() {
    	this.stop();   
    	if(client != null) {
    		client.closeSocket();
    	}
    }
}
