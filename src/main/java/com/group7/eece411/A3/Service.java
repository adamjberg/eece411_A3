package com.group7.eece411.A3;

import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class Service extends TimerTask{

	protected UDPClient client;
	protected Timer timer;
	protected int period;
	
	public Service(int period) throws UnknownHostException {
		this.client = new UDPClient(5628);
		this.period = period;
	}
	
	public void run() 
    {
		System.out.println("Do Nothing");
    }
	
	public void start() {
		this.timer = new Timer(true); //daemon thread
		this.timer.scheduleAtFixedRate(this, 0, this.period);
	}

    
    public void stop() {
    	this.timer.cancel();    	
    }
    
    public void terminate() {
    	this.stop();   
    	client.closeSocket();
    }
}
