package com.group7.eece411.A3;

import java.io.IOException;

public class AgentRemove extends Agent {

	public AgentRemove(Protocol p) throws IOException {
		super(p);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("REMOVE something from "+target.getHost());
		System.out.println("key : "+this.decodeKey);
		try {
			if(db.isThisNode(target)) {
				target.remove(this.decodeKey);
				System.out.println("REMOVED "+this.decodeKey);
			} else {
				// send remote request
				UDPClient local_client = new UDPClient(sending_port, protocol);
				local_client.send(target.getHost(), target.getPort(), protocol);
				local_client.closeSocket();
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
			//TODO : send 0x04.  Internal KVStore failure
		}
	}

}
