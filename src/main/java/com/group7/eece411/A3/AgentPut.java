package com.group7.eece411.A3;

public class AgentPut extends Agent {

	public AgentPut(Datastore ds, byte[] key, byte[] value) {
		super(ds, key, value);
	}

	@Override
	public void run() {
		// TODO put key value pair into NodeInfo target
		System.out.println("Put some value");
	}

}
