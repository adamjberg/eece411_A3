package com.group7.eece411.A3;

import java.io.IOException;

public class AgentGet extends Agent {
	
	public AgentGet(Protocol p) throws IOException {
		super(p);
	}

	@Override
	public void run() {		
		System.out.println("GET a value from "+target.getHost() +"...");
		byte[] value = null;
		try {
			if(db.isThisNode(target)) {
				value = target.get(decodeKey);			
				if(value == null) {
					System.out.println("Cannot GET the value, key : "+decodeKey);
					respondUnscucessful(1);
				} else {
					System.out.println("Value GET from key : "+decodeKey + " is " +StringUtils.byteArrayToHexString(value));
					repondsuccess();
				}
			} else {
				// send request to remote node
				// sending_port is 7777 now -> not sure if itz the right port
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
