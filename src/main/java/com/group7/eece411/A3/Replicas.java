package com.group7.eece411.A3;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Replicas {
	public final static int SPACESIZE = 100000;
	private NodeInfo shortestPath;
	private ConcurrentHashMap<String, NodeInfo> replicas;
	private int location;
	private ConcurrentHashMap<String, Long> responseTime;
	private long shortestTime;
	private boolean isOnline;
	private ConcurrentHashMap<String, String> kvStore;
	private long spaceAvailable = 64 * 1024 * 1024;
	private Date lastUpdateDate;
	
	public Replicas(int loc, NodeInfo n) {
		replicas = new ConcurrentHashMap<String, NodeInfo>();
		this.kvStore = new ConcurrentHashMap<String, String>();
		responseTime = new ConcurrentHashMap<String, Long>();
		shortestTime = 0;
		location = loc;
		isOnline = true;
		shortestPath = n;
		this.add(n);
		this.setLastUpdateDate(new Date());
	}

	
	public byte[] get(String key) {
		if(kvStore.get(key) != null)
			return StringUtils.hexStringToByteArray(kvStore.get(key));
		return null;
	}
	
	public boolean put(String key, byte[] value) throws UnsupportedEncodingException {
		if(this.kvStore.size() >= SPACESIZE || spaceAvailable - value.length < 0) {
			//Datastore.getInstance().addLog("DEBUG", "size : "+ this.kvStore.size()+"space available : " + spaceAvilable + ", value.length : "+value.length);
			return false;
		}
		if(this.kvStore.get(key) != null) {
			spaceAvailable += StringUtils.hexStringToByteArray(this.kvStore.get(key)).length;
		}
		spaceAvailable -= value.length;
		this.kvStore.put(key, StringUtils.byteArrayToHexString(value));
		return true;
	}
	
	public boolean remove(String key) {
		String val = this.kvStore.remove(key);
		if(val == null) {
			return false;
		} 
		byte[] bytesRemoved = val.getBytes(Charset.forName("UTF-8"));
		spaceAvailable += bytesRemoved.length;
		return true;
	}
	
	/**
	 * @return the lastUpdateDate
	 */
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	/**
	 * @param lastUpdateDate the lastUpdateDate to set
	 */
	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	
	public void update() {
		this.lastUpdateDate = new Date();
	}
	
	public void add(NodeInfo n) {
		this.replicas.put(n.getHost(), n);
	}
	
	public Collection<NodeInfo> getNodes() {
		return this.replicas.values();
	}
	public void recordResponseTime(long time) {
		//Datastore.getInstance().addLog("responseTime", String.valueOf(time));
		synchronized(this) {
			responseTime.put(this.shortestPath.getHost(), new Long(time));
			//Datastore.getInstance().addLog("record", "responseTime");
			Set<String> keys = replicas.keySet();
			for(String key : keys) {
				if(replicas.get(key).isOnline()) {
					//Datastore.getInstance().addLog(key, String.valueOf(responseTime.get(key)));
					if(responseTime.get(key) == null) {
						shortestPath = replicas.get(key);
						shortestTime = 0;
						break;
					} else if(responseTime.get(key).longValue() < time) {
						shortestPath = replicas.get(key);
						shortestTime = responseTime.get(key);
					}
				}
			}
		}
		//Datastore.getInstance().addLog("shortestPath", this.shortestPath.getHost());
	}
	
	public void setShortestPath(NodeInfo n, long t) {
		this.shortestPath = n;
		this.shortestTime = t;
	}
	
	public NodeInfo getShortestPath() {
		return this.shortestPath;
	}
	
	public int getLocation() {
		return location;
	}
	
	public void setStatus(boolean isOnline) {
	//	Datastore.getInstance().addLog("setStatus", String.valueOf(isOnline));
		synchronized(this) {
			this.shortestPath.setOnline(isOnline);
			Long time;
			if(!isOnline) {
				Collection<NodeInfo> values = replicas.values();
				
				for(NodeInfo n : values) {
					//Datastore.getInstance().addLog("setStatus node", n.toString());
					if(n.isOnline()) {
						time = this.responseTime.get(n.getHost());
						//Datastore.getInstance().addLog(n.getHost(), String.valueOf(time));
						if(!this.shortestPath.isOnline() || time == null || 
								this.shortestTime > time.longValue()) {
							this.setShortestPath(n, time == null ? 0 : time.longValue());
							//Datastore.getInstance().addLog("shortestPath", this.shortestPath.getHost());
						}
					}
				}
				this.isOnline = this.shortestPath.isOnline();
			} else {
				this.isOnline = isOnline;
			}
		}
	}
	
	public boolean isOnline() {
		return this.isOnline;
	}
}
