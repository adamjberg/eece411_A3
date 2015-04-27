package com.group7.eece411.A3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
	public static final long TIMEOUT = 15;

	private static Datastore instance = null;
	private ConcurrentHashMap<Integer, Replicas> successors;
	private ConcurrentHashMap<String, NodeInfo> hosts;
	private Integer self;
	private ArrayList<Packet> packetQueue;
	private SimpleCache<Packet> cache;
	private ArrayList<JSONObject> logs;
	public static boolean breakerPoint = false;
	
	private Datastore() {
		this.successors = new ConcurrentHashMap<Integer, Replicas>();
		this.hosts = new ConcurrentHashMap<String, NodeInfo>();
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
		return clone;
	}
	
	// What does this location mean ?
	public Replicas find(int location) {
		return this.successors.get(location);
	}
 
	/*
	 * Return sorted list of locations
	 */
	public List<Integer> findAllLocations() {
		return asSortedList(this.successors.keySet());
	}
	
	public List<Integer> findAllActiveLocations() {
		Collection<Replicas> list = this.successors.values();
		List<Integer> locs = new ArrayList<Integer>();
		for(Replicas r : list) {
			if(r.isOnline()) {
				locs.add(r.getLocation());
			}
		}
		locs.sort(new IntComparable());
		return locs;
	}
	
	
	/*public NodeInfo findRandomNode() {
		return this.getResponsibleNode((int)(Math.random()*CIRCLE_SIZE));
	}*/
	 
	/*
	 * Get the node that responsible for the key, it can be the caller node or other remote nodes
	 */
	public Replicas getResponsibleNode(byte keyByte) {
		return getResponsibleNode(ByteOrder.ubyte2int(keyByte));
	}

	public Replicas getResponsibleNode(int key) {
		//System.out.println("key : "+key);
		if(this.self == key) return this.findThisNode();
		int closestLocation = binarySearch(this.findAllActiveLocations(), key);
		//System.out.println("loc "+closestLocation);
		return this.find(closestLocation);
	}
	
	public Replicas getInternalResponsibleNode(int key) {
		int closestLocation = binarySearch(this.findAllLocations(), key);
		return this.find(closestLocation);
	}
	
	public void forceTargetSelf(Replicas target) {
		int t = target.getLocation();
		if(this.self.intValue() != t) {
			Set<Integer> allLocations = this.successors.keySet();
			Iterator<Integer> itr = allLocations.iterator();
			while(itr.hasNext()) {
				int location = itr.next();
				if((location <= t && (t < this.self || location > this.self)) || (t < this.self && location > this.self)) {
					Replicas n =  this.find(location);
					n.setStatus(false);
				}
			}
		}
	}
	
	private int binarySearch(List<Integer> sortList, int searchNum) {
		int index = sortList.size()/2;
		int prev = sortList.size();		
		//System.out.println(Arrays.toString(sortList.toArray()));
		while(true) {
			int diff = (int) Math.ceil(Math.abs(prev - index)/2.0);
			//System.out.println("diff :"+diff+", prev : "+prev+", searchNum : "+searchNum+", index : "+index+", location : "+sortList.get(index));
			prev = index;
			if(sortList.get(index).equals(searchNum)) {
				return sortList.get(index);
			} else if(sortList.get(index) < searchNum ) {
				if(index == sortList.size() - 1 || sortList.get(index+1) > searchNum) {
					return sortList.get(index);
				}
				if(index+diff >= sortList.size()) {
					index++;
				} else {
					index += diff;
				}
			} else if(sortList.get(index) > searchNum) {
				if(index == 0) {
					return sortList.get(sortList.size() - 1);
				}
				if(diff > index) {
					index = 0;
				} else {
					index -= diff;
				}
			}
		}
	}
	
	public void setNodeStatus(int id, boolean isOnline) {
		synchronized(this.successors.get(id)) {
			this.successors.get(id).setStatus(isOnline);
		}
	}
	
	public Collection<Replicas> findAllKVStore() {
		//this.successors.get(this.self).setLastUpdateDate(new Date());
		return this.successors.values();
	}

	public Replicas findThisNode() {
		return this.successors.get(this.self);
	}

	private void setupNodes() {
		InputStream in = Datastore.class.getClassLoader().getResourceAsStream(
				"file/hosts.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		List<Integer> locs = new ArrayList<Integer>();
		HashMap<Integer, NodeInfo> map = new HashMap<Integer, NodeInfo>();
		String[] lineArray;
		try {
			while ((line = reader.readLine()) != null) {
				//System.out.println(line);
				lineArray = line.split(":");
				if (lineArray.length != 3) {
					this.addLog("ERROR","Invalid Line Found!");
				} else {
					NodeInfo n = new NodeInfo(lineArray[0],
							Integer.valueOf(lineArray[1]),
							Integer.valueOf(lineArray[2]));
					if (this.self == null && isNodeInfoMine(n)) {
						this.self = n.getLocation();
						System.out.println("self "+this.self);
						n.setSelf(true);
					} 
					if (n.getLocation() < CIRCLE_SIZE && n.getLocation() >= 0) {
						map.put(n.getLocation(), n);
						this.hosts.put(n.getHost(), n);
						locs.add(n.getLocation());
					}
				}
			}
			locs.sort(new IntComparable());
			int count = 0;
			int buf = 0;
			//System.out.println("Locs size "+locs.size());
			for(Integer i : locs) {
				count++;
				if(count > 3 || count == 1) {
					count = 1;
					this.successors.put(i, new Replicas(i, map.get(i)));
					buf = i;
				} else {
					this.successors.get(buf).add(map.get(i));
					if(i.intValue() == this.self) {
						this.successors.get(buf).setShortestPath(map.get(i), 0);
						this.self = buf;
						System.out.println("self "+this.self);
					}
				}
			}
		} catch (NumberFormatException e) {
			this.addException("NumberFormatException", e);
		} catch (UnknownHostException e) {
			this.addException("UnknownHostException", e);
		} catch (IOException e) {
			this.addException("IOException", e);
		}
	}
		
	public NodeInfo getHost(String hostname) {
		return this.hosts.get(hostname);
	}
		
	public boolean isThisNode(Replicas n) {	
		if(n == null) return true;
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
			e.printStackTrace();
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
		synchronized(this.cache) {
			return this.cache.get(uid);
		}
	}

	public void storeCache(String uid, Packet packet) {
		synchronized(this.cache) {
			this.cache.put(uid, packet);
		}
	}	
		/*public Boolean getProcessCache(String uid) {
		synchronized(this.processCache) {
			return this.processCache.get(uid);		
		}
	}
	
	public void storeProcessCache(String uid, boolean val) {
		synchronized(this.processCache) {
			this.processCache.put(uid, new Boolean(val));
		}
	}	*/
	
	@SuppressWarnings("unchecked")
	public void addLog(String type, String log) {
		JSONObject map=new JSONObject();
		map.put("type", type);
		map.put("time", (new Date()).getTime());
		map.put("log", log);
		synchronized(this.logs) {
			this.logs.add(map);
		}
		//System.out.println(map.toJSONString());
	}
	
	@SuppressWarnings("unchecked")
	public void addException(String type, Exception e) {
		e.printStackTrace();
		JSONObject map=new JSONObject();
		map.put("type", type);
		map.put("time", (new Date()).getTime());
		map.put("log", e.getMessage());
		map.put("trace", Arrays.toString(e.getStackTrace()));
		synchronized(this.logs) {
			this.logs.add(map);
		}
		//System.out.println(map.toJSONString());
	}
	
	public ArrayList<JSONObject> getLogs() {
		int count = 0;
		ArrayList<JSONObject> ret = new ArrayList<JSONObject>();
		synchronized(this.logs) {
			ListIterator<JSONObject> itr = this.logs.listIterator();
			while(itr.hasNext()) {
				ret.add(itr.next());
				itr.remove();
				count++;
				if(count >= 50) {
					break;
				}
			}
			return ret;
		}
	}
	/*
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
	}*/
	
	public class IntComparable implements Comparator<Integer>{
		 
		public int compare(Integer o1, Integer o2) {
			return (o1<o2 ? -1 : (o1==o2 ? 0 : 1));
		}
	}
	
}
