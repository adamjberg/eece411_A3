package com.group7.eece411.A3;

import java.io.IOException;

public class AgentRemove extends Agent {

	public AgentRemove(Protocol p) throws IOException {
		super(p);
	}

	@Override
	public void run() {
		System.out.println("REMOVE key("+this.decodeKey+") from "+target.getHost());
		try {
			if(db.isThisNode(target)) {
				if(target.get(decodeKey) != null) {
					target.remove(this.decodeKey);
					System.out.println("REMOVED "+this.decodeKey);
					respond(0, this.protocol);	
				} else {
					respond(1, this.protocol);	
				}
			} else {
				// send remote request
				//UDPClient local_client = new UDPClient(sending_port, protocol);
				this.client.send(target.getHost(), target.getPort(), protocol);
				this.client.closeSocket();
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
			respond(4, this.protocol);	
		}
	}

}
