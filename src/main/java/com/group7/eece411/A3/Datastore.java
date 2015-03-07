package com.group7.eece411.A3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.christianschenk.simplecache.SimpleCache;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Ehsan Added this so it might not be complete: 
 * This class holds the data that each node needs. That is: 
 * - list of (key, value) pairs taht is stored in node itself
 * - list of the (key, responsible node) pair -> which node has the value for the key
 * */
public class Datastore {
	public static int CIRCLE_SIZE = 256;
	public static final long TIMEOUT = 30;

	private static Datastore instance = null;
	private ConcurrentHashMap<Integer, NodeInfo> successors;
	private Integer self;
	private ArrayList<Packet> packetQueue;
	private SimpleCache<Packet> cache;
	private ArrayList<JSONObject> logs;

	private Datastore() {
		this.successors = new ConcurrentHashMap<Integer, NodeInfo>();
		this.packetQueue = new ArrayList<Packet>();
		this.logs = new ArrayList<JSONObject>();
		this.cache = new SimpleCache<Packet>(TIMEOUT);
		setupNodes();
	}

	// Why do we need this ? 
	// FYI: Please refer to this: http://en.wikipedia.org/wiki/Singleton_pattern
	public static Datastore getInstance() {
		if (instance == null) {
			instance = new Datastore();
		}
		return instance;
	}

	public void queue(Packet p) {
		synchronized(this.packetQueue) {
			this.packetQueue.add(p);
		}
	}
	
	public List<Packet> poll() {
		ArrayList<Packet> clone = null;
		synchronized(this.packetQueue) {	
			clone = this.packetQueue;
			this.packetQueue = new ArrayList<Packet>();
		}
		Collections.sort(clone, new Comparator<Packet>() {
			public int compare(Packet p1, Packet p2) {
		        return p1.getDate().compareTo(p2.getDate());
		    }
		});
		
		return clone;
	}
	
	// What does this location mean ?
	public NodeInfo find(int location) {
		NodeInfo found = this.successors.get(location);
		return found;
	}
 
	/*
	 * Return sorted list of locations
	 */
	public List<Integer> findAllLocations() {
		return asSortedList(this.successors.keySet());
	}
	
	public List<Integer> findAllActiveLocations() {
		Collection<NodeInfo> list = this.successors.values();
		List<Integer> locs = new ArrayList<Integer>();
		for(NodeInfo n : list) {
			if(n.isOnline()) {
				locs.add(n.getLocation());
			}
		}
		locs.sort(new IntComparable());
		return locs;
	}
	
	
	public NodeInfo findRandomNode() {
		return this.getResponsibleNode((int)Math.random()*CIRCLE_SIZE);
	}
	
	/*
	 * Get the node that responsible for the key, it can be the caller node or other remote nodes
	 */
	public NodeInfo getResponsibleNode(byte keyByte) {
		return getResponsibleNode(ByteOrder.ubyte2int(keyByte));
	}

	public NodeInfo getResponsibleNode(int key) {
		if(this.self == key) return this.findThisNode();
		List<Integer> allLocations = this.findAllActiveLocations();
		int closestLocation = booleanSearch(allLocations, key);
		if(closestLocation == this.self) { 
			closestLocation = booleanSearch(this.findAllLocations(), key);
		}
		return this.find(closestLocation);
	}
	
	private int booleanSearch(List<Integer> sortList, int searchNum) {
		int index = sortList.size()/2;
		while(true) {
			//System.out.println("searchNum : "+searchNum+", loop index : "+index+", val : "+sortList.get(index));
			if(sortList.get(index).equals(searchNum)) {
				return sortList.get(index);
			} else if(sortList.get(index) < searchNum ) {
				if(index == sortList.size() - 1 || sortList.get(index+1) > searchNum) {
					return sortList.get(index);
				}
				index += index/2;
			} else if(sortList.get(index) > searchNum) {
				if(index == 0) {
					return sortList.get(sortList.size() - 1);
				}
				index = index/2;
			}
		}
	}
	public void setNodeStatus(int id, boolean isOnline) {
		this.successors.get(id).setOnline(isOnline, new Date());
	}
	
	public Collection<NodeInfo> findAll() {
		this.successors.get(this.self).setLastUpdateDate(new Date());
		return this.successors.values();
	}

	public NodeInfo findThisNode() {
		return this.successors.get(this.self);
	}

	private void setupNodes() {
		InputStream in = Datastore.class.getClassLoader().getResourceAsStream(
				"file/hosts.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String[] lineArray = line.split(":");
				if (lineArray.length != 3) {
					this.addLog("ERROR","Invalid Line Found!");
				} else {
					NodeInfo n = new NodeInfo(lineArray[0],
							Integer.valueOf(lineArray[1]),
							Integer.valueOf(lineArray[2]));

					n.setOnline(true);
					
					if (self == null && isNodeInfoMine(n)) {
						this.addLog("INFO", "Start machine : "+n.getHost()+" with location "+n.getLocation());
						this.self = n.getLocation();
					} 
					if (n.getLocation() < CIRCLE_SIZE && n.getLocation() >= 0) {
						this.successors.put(n.getLocation(), n);
					}
				}
			}
		} catch (NumberFormatException e) {
			this.addLog("EXCEPTION", Arrays.toString(e.getStackTrace()));
		} catch (UnknownHostException e) {
			this.addLog("EXCEPTION", Arrays.toString(e.getStackTrace()));
		} catch (IOException e) {
			this.addLog("EXCEPTION", Arrays.toString(e.getStackTrace()));
		}
		//fillEmptyLocations();
	}
	
	public boolean isThisNode(NodeInfo n) {	
		if(this.self != null) {
			//if the node is offline, the server is responsible
			return n.getLocation() == self || !n.isOnline();
		}
		return false;
	}
	
	private boolean isNodeInfoMine(NodeInfo nodeInfo)
			throws IOException {
		boolean isMyHostname = SystemCmd.getHostName()
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
	
	private List<Integer> asSortedList(Set<Integer> c) {
		List<Integer> list = new ArrayList<Integer>(c);
		Collections.sort(list);
		return list;
	}

	public Packet getCache(String uid) {
		return this.cache.get(uid);
	}

	public void storeCache(String uid, Packet packet) {
		this.cache.put(uid, packet);
	}	
	
	@SuppressWarnings("unchecked")
	public void addLog(String type, String log) {
		JSONObject map=new JSONObject();
		map.put("type", type);
		map.put("time", (new Date()).getTime());
		map.put("log", log);
		synchronized(this.logs) {
			this.logs.add(map);
		}
		System.out.println(map.toJSONString());
	}
	
	@SuppressWarnings("unchecked")
	public void addException(String type, Exception e) {
		JSONObject map=new JSONObject();
		map.put("type", type);
		map.put("time", (new Date()).getTime());
		map.put("log", e.getMessage());
		map.put("trace", Arrays.toString(e.getStackTrace()));
		synchronized(this.logs) {
			this.logs.add(map);
		}
		System.out.println(map.toJSONString());
	}
	
	public ArrayList<JSONObject> getLogs() {
		synchronized(this.logs) {
			ArrayList<JSONObject> ret = new ArrayList<JSONObject>();
			ListIterator<JSONObject> itr = this.logs.listIterator();
			while(itr.hasNext()) {
				ret.add(itr.next());
				itr.remove();
				if(itr.nextIndex() >= 100) {
					break;
				}
			}
			return ret;
		}
	}
	
	public void sync(String dataFeed) {
		JSONParser parser=new JSONParser();
		NodeInfo node = null;
		JSONObject jobj = null;
		try {
			JSONArray array=(JSONArray)parser.parse(dataFeed);
			for(Object obj : array.toArray()) {
				jobj = (JSONObject) obj;
				node = this.find(Integer.valueOf((String)jobj.get("index")));
				node.sync(jobj);				
			}
		} catch (ParseException e) {
			this.addLog("ParseException", Arrays.toString(e.getStackTrace()));
		}		  
	}
	
	public class IntComparable implements Comparator<Integer>{
		 
		public int compare(Integer o1, Integer o2) {
			return (o1<o2 ? -1 : (o1==o2 ? 0 : 1));
		}
	}
	
}
