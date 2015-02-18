package com.group7.eece411.A3;

import java.io.IOException;

public class AgentGet extends Agent {
	
	public AgentGet(Packet p) throws IOException {
		super(p);
	}

	@Override
	public void run() {		
		System.out.println("GET a value from "+target.getHost() +"...");
		byte[] value = null;
		try {
			if(db.isThisNode(target)) {
				value = target.get(this.packet.getStringHeader("key"));			
				if(value == null) {
					System.out.println("Cannot GET the value, key : "+this.packet.getStringHeader("key"));
					this.client.send(Protocol.sendResponse(this.packet, null, 1));
				} else {
					System.out.println("Value GET from key : "+this.packet.getStringHeader("key") + " is " +StringUtils.byteArrayToHexString(value));
					this.client.send(Protocol.sendResponse(this.packet, 
							target.get(this.packet.getStringHeader("key")), 0));
				}
			} else {
				this.client.send(target.getHost(), target.getPort(), packet);	
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
			try {
				this.client.send(Protocol.sendResponse(this.packet, null, 4));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}					
		} finally {
			this.client.closeSocket();
		}
	}
	

}
