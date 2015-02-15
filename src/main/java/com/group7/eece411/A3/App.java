package com.group7.eece411.A3;

import java.util.Vector;

import com.group7.eece411.A3.UDPClient;

/**
 * Hello world!
 *
 */
public class App {
	private static Vector<Header> receivedUniqueHeaders = new Vector<Header>();
	
    public static void main( String[] args ) throws Exception {
    	UDPClient client = new UDPClient(7777);
		client.setTimeout(0);
		
		do {
			// Receive bytes and init empty byte array for the actual message
			byte[] receivedBytes = client.receive();
			byte[] message;
			
			// Create new Header obj for this received message
			Header header = new Header();
			message = header.decodeAndGetMessage(receivedBytes);
			
			// Check if header already in our vector of previously received headers
			if (receivedUniqueHeaders.contains(header)) {
				// TODO: do something with the dupe request
			} else {
				receivedUniqueHeaders.add(header);
				// TODO: do something with this new request
			}
			
		} while(true);
    }
}
