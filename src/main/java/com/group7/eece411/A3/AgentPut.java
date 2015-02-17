package com.group7.eece411.A3;

import java.io.IOException;

public class AgentPut extends Agent {

	public AgentPut(byte[] key, byte[] value) throws IOException {
		super(key, value);
	}

	@Override
	public void run() {
		// TODO put key value pair into NodeInfo target
		System.out.println("Put some value");
	}

}
