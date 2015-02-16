package com.group7.eece411.A3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

import com.group7.eece411.A3.UDPClient;

public class App {
	private static Vector<Node> nodes;
	private static Vector<Header> receivedUniqueHeaders = new Vector<Header>();
	
    public static void main( String[] args ) throws Exception {
    	System.setProperty("java.net.preferIPv4Stack", "true");
    	getNodesFromFile();
		getPortsForThisNode();
		UDPClient client = getClient();
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

    /*
	 * Creates a client based on the specified port for this node Allows
	 * multiple services to run on the same node w/ diff ports
	 */
	public static UDPClient getClient() throws UnknownHostException {
		Vector<Integer> myPorts = getPortsForThisNode();
		int portIndex = 0;
		boolean success = false;
		do {
			UDPClient client = new UDPClient(myPorts.get(portIndex++));
			try {
				client.createSocket();
				return client;
			} catch (SocketException e) {
				// Do nothing this port is taken
			}
		} while (success == false && portIndex < myPorts.size());
		return null;
	}

	private static Vector<Integer> getPortsForThisNode() throws NumberFormatException,
			UnknownHostException {
		Vector<Integer> myPorts = new Vector<Integer>();
		for (Node node : nodes) {
			if (node.hostName.equals("127.0.0.1")
					|| node.hostName.equals(InetAddress.getLocalHost()
							.getHostAddress())
					|| node.hostName.equals(InetAddress.getByName(InetAddress
							.getLocalHost().getHostAddress()))) {
				myPorts.add(Integer.valueOf(node.port));
			}
		}
		return myPorts;
	}

	private static void getNodesFromFile() throws IOException {
		nodes = new Vector<Node>();
		InputStream in = App.class.getClassLoader().getResourceAsStream(
				"file/hosts.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] lineArray = line.split(",");

			if (lineArray.length != 3) {
				System.out.println("Invalid Line Found!");
			} else {
				Node node = new Node();
				node.hostName = lineArray[0];
				node.port = lineArray[1];
				node.location = Integer.valueOf(lineArray[2]);
				nodes.add(node);
			}
		}
	}
}
