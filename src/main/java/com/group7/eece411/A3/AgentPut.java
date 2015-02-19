package com.group7.eece411.A3;

import java.io.IOException;

public class AgentPut extends Agent {

	public AgentPut(Packet p) throws IOException {
		super(p);
	}

	@Override
	public void run() {
		System.out.println("Try to PUT (key,value) to "+target.getHost() +"...");
		try {
			if(db.isThisNode(target)) {
				if(!target.put(this.packet.getStringHeader("key"), this.packet.getPayload())) {
					this.client.send(Protocol.sendResponse(this.packet, null, 2));
				} else {
					System.out.println("PUT key : "+this.packet.getStringHeader("key")+
							", value : "+StringUtils.byteArrayToHexString(this.packet.getPayload()) + 
							" in "+target.getHost());
					this.client.send(Protocol.sendResponse(this.packet, null, 0));
					
				}
			} else {
				System.out.println("Forwarding to "+target.getHost());
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
