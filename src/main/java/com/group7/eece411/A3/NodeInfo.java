package com.group7.eece411.A3;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;


/**
 * @author Ehsan
 *
 */
public class NodeInfo {
	public final static int SPACESIZE = 100000;
	private String hostName;
	private int port;
	private int location;
	private ConcurrentHashMap<String, String> kvStore;
	private long spaceAvailable = 64 * 1024 * 1024;
	private boolean isOnline;
	private Date lastUpdateDate;
	
	public NodeInfo (String host, int port, int location) {
		this.hostName = host;
		this.port = port;
		this.location = location;
		this.kvStore = new ConcurrentHashMap<String, String>();
		this.setOnline(false);
		this.setLastUpdateDate(new Date());
	}

	public byte[] get(String key) {
		if(kvStore.get(key) != null)
			return kvStore.get(key).getBytes(Charset.forName("UTF-8"));
		return null;
	}
	
	public boolean put(String key, byte[] value) throws UnsupportedEncodingException {
		if(this.kvStore.size() >= SPACESIZE || spaceAvailable - value.length < 0) {
			return false;
		}
		if(this.kvStore.get(key) != null) {
			spaceAvailable += this.kvStore.get(key).getBytes(Charset.forName("UTF-8")).length;
		}
		spaceAvailable -= value.length;
		this.kvStore.put(key, new String(value, "UTF-8"));
		return true;
	}
	
	public void remove(String key) {
		byte[] bytesRemoved = this.kvStore.remove(key).getBytes(Charset.forName("UTF-8"));
		spaceAvailable += bytesRemoved.length;
	}
	
	public String getHost() {
		return this.hostName;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public int getLocation() {
		return this.location;
	}

	/**
	 * @return the isOnline
	 */
	public boolean isOnline() {
		return isOnline;
	}

	/**
	 * @param isOnline the isOnline to set
	 */
	public void setOnline(boolean isOnline, Date lastUpdateDate) {
		this.isOnline = isOnline;
		this.setLastUpdateDate(lastUpdateDate);
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
		this.setLastUpdateDate(new Date());
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
	
	@SuppressWarnings("unchecked")
	public String toString() {
		JSONObject map=new JSONObject();
		map.put("hostname", this.hostName);
		map.put("port", this.port);
		map.put("location", this.location);
		map.put("status", this.isOnline());
		map.put("lastUpdateDate", this.getLastUpdateDate().getTime());
		map.put("spaceAvailable", this.spaceAvailable);
		map.put("kvstore", this.kvStore);
		return map.toJSONString();
	}
	
	public String getKVString() {
		return this.kvStore.toString();
	}
	

	public void sync(JSONObject obj) {
		if(this.lastUpdateDate.getTime() < Integer.valueOf((String)obj.get("lastUpdateDate"))) {
			
		}
	}
}

