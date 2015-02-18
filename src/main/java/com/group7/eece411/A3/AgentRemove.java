package com.group7.eece411.A3;

import java.io.IOException;

public class AgentRemove extends Agent {

	public AgentRemove(Packet p) throws IOException {
		super(p);
	}

	@Override
	public void run() {
		System.out.println("REMOVE key("+this.packet.getStringHeader("key")+") from "+target.getHost());
		try {
			if(db.isThisNode(target)) {
				if(target.get(this.packet.getStringHeader("key")) != null) {
					target.remove(this.packet.getStringHeader("key"));
					System.out.println("REMOVED "+this.packet.getStringHeader("key"));
					this.client.send(Protocol.sendResponse(this.packet, null, 0));
				} else {
					this.client.send(Protocol.sendResponse(this.packet, null, 1));
				}
			} else {
				// send remote request
				this.client.send(packet);
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
