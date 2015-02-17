package com.group7.eece411.A3;

public class Agent implements Runnable{
	
	protected Datastore ds;
	protected byte[] key;
	protected byte[] value;
	
	public Agent(Datastore ds, byte[] key, byte[] value) {
		this.ds = ds;
		this.key = key;
		this.value = value;
	}
	
	public Agent(Datastore ds, byte[] key) {
		this.ds = ds;
		this.key = key;
	}
	
	public void run() {
		
	}
}
