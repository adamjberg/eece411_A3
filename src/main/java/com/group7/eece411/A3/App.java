package com.group7.eece411.A3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

/**
 * Hello world!
 *
 */
public class App {
	private UDPClient client;
	private Vector<Node> nodes;

	public static void main(String[] args) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		new App();
	}

	public App() throws IOException {
		getNodesFromFile();
		getPortsForThisNode();
		this.client = getClient();
	}

	/*
	 * Creates a client based on the specified port for this node Allows
	 * multiple services to run on the same node w/ diff ports
	 */
	public UDPClient getClient() throws UnknownHostException {
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

	private Vector<Integer> getPortsForThisNode() throws NumberFormatException,
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

	private void getNodesFromFile() throws IOException {
		nodes = new Vector<Node>();
		InputStream in = getClass().getClassLoader().getResourceAsStream(
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
