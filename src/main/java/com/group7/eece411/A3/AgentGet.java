package com.group7.eece411.A3;

import java.io.IOException;
import java.util.ArrayList;

public class AgentGet extends Agent {

	public AgentGet(Protocol p) throws IOException {
		super(p);
	}

	@Override
	public void run() {
		
		System.out.println("get a value from "+target.getHost() +"...");
		//TODO : get the value from NodeInfo target 
		byte[] value = null;
		try {
			if(db.isThisNode(target)) {
				value = target.get(decodeKey);
			} else {
				//TODO : send request to remote node
			}
			if(value == null) {
				//TODO : 0x01.  Non-existent key requested in a get or delete operation
			} else {
				System.out.println("Value get from key : "+decodeKey + " is " +StringUtils.byteArrayToHexString(value));
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
			//TODO : send 0x04.  Internal KVStore failure
		}
	}
	

}
