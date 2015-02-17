package com.group7.eece411.A3;

import java.io.IOException;

public class AgentPut extends Agent {

	public AgentPut(byte[] key, byte[] value) throws IOException {
		super(key, value);
	}

	@Override
	public void run() {
		// TODO put key value pair into NodeInfo target
		System.out.println("Put some value to "+target.getHost());
		System.out.println("key : "+this.decodeKey);
		try {
			if(db.isThisNode(target)) {
				if(!target.put(this.decodeKey, this.value)) {
					//TODO : failed to put, 0x02 Out of space
				}
			} else {
				//TODO : send request to remote key
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
			//TODO : send 0x04.  Internal KVStore failure
		}
	}

}
