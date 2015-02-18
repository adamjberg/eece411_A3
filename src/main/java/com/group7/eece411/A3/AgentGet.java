package com.group7.eece411.A3;

import java.io.IOException;
import java.util.ArrayList;

import com.group7.eece411.A3.ResponseData.ResponseCode;

public class AgentGet extends Agent {
	
	public AgentGet(Protocol p) throws IOException {
		super(p);
	}

	@Override
	public void run() {
		
		System.out.println("GET a value from "+target.getHost() +"...");
		//TODO : get the value from NodeInfo target.  
		byte[] value = null;
		try {
			if(db.isThisNode(target)) {
				value = target.get(decodeKey);
			} else {
				// send request to remote node
				// sending_port is 7777 now -> not sure if itz the right port
				UDPClient local_client = new UDPClient(sending_port, protocol);
				local_client.send(target.getHost(), target.getPort(), protocol);
				local_client.closeSocket();
				
			}
			if(value == null) {
				//TODO : 0x01.  Non-existent key requested in a get or delete operation
				System.out.println("Cannot GET the value, key : "+decodeKey);
			} else {
				System.out.println("Value GET from key : "+decodeKey + " is " +StringUtils.byteArrayToHexString(value));
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
			//TODO : send 0x04.  Internal KVStore failure
			
		}
	}
	

}
