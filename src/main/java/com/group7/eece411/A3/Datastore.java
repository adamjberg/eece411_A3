package com.group7.eece411.A3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class Datastore {
	private static Datastore instance = null;
	private ConcurrentHashMap<Integer, NodeInfo> successors;
	private NodeInfo self;
	
	protected Datastore() throws IOException {
    	this.successors = new ConcurrentHashMap<Integer, NodeInfo>();
    	setupNodes();
	}
	
	public static Datastore getInstance() throws IOException {
      if(instance == null) {
         instance = new Datastore();
      }
      return instance;
	}
	
	public NodeInfo find(int key) {
		return this.successors.get(key);
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
				if(InetAddress.getLocalHost().getHostName().toLowerCase().equals(lineArray[0].toLowerCase())) {
					this.self = n;
				} else {
					this.successors.put(Integer.valueOf(lineArray[2]), n);
				}
			}
		}
	}
}
