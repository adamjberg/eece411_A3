package com.group7.eece411.A3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Datastore {
	public static int CIRCLE_SIZE = 256;

	private static Datastore instance = null;
	private ConcurrentHashMap<Integer, NodeInfo> successors;
	private NodeInfo self;

	private Datastore() throws IOException {
		this.successors = new ConcurrentHashMap<Integer, NodeInfo>();
		setupNodes();
	}

	public static Datastore getInstance() throws IOException {
		if (instance == null) {
			instance = new Datastore();
		}
		return instance;
	}

	public NodeInfo find(int location) {
		return this.successors.get(location);
	}
 
	/*
	 * Return sorted list of locations
	 */
	public List<Integer> findAllLocations() {
		return asSortedList(this.successors.keySet());
	}
	
	public Collection<NodeInfo> findAll() {
		return this.successors.values();
	}

	public NodeInfo findThisNode() {
		return this.self;
	}

	private void setupNodes() throws IOException {
		InputStream in = Datastore.class.getClassLoader().getResourceAsStream(
				"file/hosts.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] lineArray = line.split(":");
			if (lineArray.length != 3) {
				System.out.println("Invalid Line Found!");
			} else {
				NodeInfo n = new NodeInfo(lineArray[0],
						Integer.valueOf(lineArray[1]),
						Integer.valueOf(lineArray[2]));

				/*
				 * Find the first NodeInfo that matches my system. There may be
				 * multiple that match because we may want to run multiple
				 * services on the same machine i.e. testing
				 */
				/*
				 * Idea of testing with multiple threads can become very complex,
				 * I would suggest we do not go that way.
				 */
				if (self == null && isNodeInfoMine(n)) {
					this.self = n;
				} 
				if (n.getLocation() < CIRCLE_SIZE && n.getLocation() >= 0) {
					this.successors.put(n.getLocation(), n);
				}
			}
		}
		//fillEmptyLocations();
	}

	private boolean isNodeInfoMine(NodeInfo nodeInfo)
			throws UnknownHostException {
		boolean isMyHostname = InetAddress.getLocalHost().getHostName()
				.toLowerCase().equals(nodeInfo.getHost().toLowerCase())
				|| nodeInfo.getHost().equals("127.0.0.1");
		return isMyHostname && isPortFree(nodeInfo.getPort());
	}

	private boolean isPortFree(int port) {
		boolean portFree = true;
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(port);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			portFree = false;
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
		return portFree;
	}

	/*
	 * Copy the NodeInfo into the locations from the last real node To the
	 * location of the node. This allows constant time access for finding a
	 * node.
	 */
	private void fillEmptyLocations() {
		boolean done = false;
		int position = CIRCLE_SIZE - 1;
		NodeInfo nodeToCopy = null;
		int validNodeCount = 0;
		int emptyNodeCount = 0;
		while (done == false) {
			if (successors.get(position) != null) {
				nodeToCopy = successors.get(position);
				validNodeCount++;
			} else {
				if (nodeToCopy != null) {
					successors.put(position, nodeToCopy);
				}
				emptyNodeCount++;
			}
			position--;
			if (position < 0) {
				position = CIRCLE_SIZE - 1;
				if (validNodeCount >= CIRCLE_SIZE
						|| emptyNodeCount >= CIRCLE_SIZE) {
					done = true;
				} else {
					validNodeCount = 0;
					emptyNodeCount = 0;
				}
			}
		}
	}
	
	private List<Integer> asSortedList(Set<Integer> c) {
		List<Integer> list = new ArrayList<Integer>(c);
		Collections.sort(list);
		return list;
	}	
}
