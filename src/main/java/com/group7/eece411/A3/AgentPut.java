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
					respondUnscucessful(2);	
				} else {
					System.out.println("PUT key : "+this.decodeKey+
							", value : "+StringUtils.byteArrayToHexString(this.protocol.getRawHeader("value")) + 
							" in "+target.getHost());
				}
			} else {
				//send request to remote key
				//UDPClient local_client = new UDPClient(sending_port, protocol);
				this.client.send(target.getHost(), target.getPort(), protocol);
				this.client.closeSocket();
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
			respondUnscucessful(4);	
		}
	}

}
