package com.group7.eece411.A3;

import java.io.IOException;

public class AgentPut extends Agent {

	public AgentPut(Protocol p) throws IOException {
		super(p);
	}

	@Override
	public void run() {
		// TODO put key value pair into NodeInfo target
		System.out.println("PUT some value to "+target.getHost() +"...");
		try {
			if(db.isThisNode(target)) {
				if(!target.put(this.decodeKey, this.protocol.getRawHeader("value"))) {
					//TODO : failed to put, 0x02 Out of space
				} else {
					System.out.println("PUT key : "+this.decodeKey+
							", value : "+StringUtils.byteArrayToHexString(this.protocol.getRawHeader("value")) + 
							" in "+target.getHost());
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
