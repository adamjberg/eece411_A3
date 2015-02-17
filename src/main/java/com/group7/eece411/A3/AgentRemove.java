package com.group7.eece411.A3;

import java.io.IOException;

public class AgentRemove extends Agent {

	public AgentRemove(byte[] key) throws IOException {
		super(key);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Remove something from "+target.getHost());
		System.out.println("key : "+this.decodeKey);
		try {
			if(db.isThisNode(target)) {
				target.remove(this.decodeKey);
			} else {
				//TODO : send remote request
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
			//TODO : send 0x04.  Internal KVStore failure
		}
	}

}
