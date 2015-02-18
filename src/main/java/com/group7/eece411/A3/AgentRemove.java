package com.group7.eece411.A3;

import java.io.IOException;

public class AgentRemove extends Agent {

	public AgentRemove(Protocol p) throws IOException {
		super(p);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		System.out.println("REMOVE something from "+target.getHost());
		System.out.println("key : "+this.decodeKey);
		try {
			if(db.isThisNode(target)) {
				target.remove(this.decodeKey);
				System.out.println("REMOVED "+this.decodeKey);
				//TODO : send 0x00 success
			} else {
				// send remote request
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
