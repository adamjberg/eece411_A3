package com.group7.eece411.A3;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;


/**
 * @author Ehsan
 *
 */
public class NodeInfo {
	private String hostName;
	private int port;
	private int location;
	private boolean isOnline;
	private Date lastUpdateDate;
	private InetAddress address;
	private String ip;
	private boolean isSelf;
	
	public NodeInfo (String host, int port, int location) {
		this.hostName = host;
		this.port = port;
		this.location = location;
		this.setOnline(true);
		this.setLastUpdateDate(new Date());
		this.setSelf(false);
		try {
			this.address = InetAddress.getByName(host);
			this.ip = this.address.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void setAddress(InetAddress ip) {
		this.address = ip;
	}
	public InetAddress getAddress() {
		if(this.address == null) {
			try {
				this.address = InetAddress.getByName(this.hostName);
				this.ip = this.address.getHostAddress();
			} catch (UnknownHostException e) {
				Datastore.getInstance().addException("UnknownHostException", e);
			}
		}
		return this.address;
	}
	
	public String getIP() {
		return this.ip;
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
		if(this.isOnline == isOnline) return;
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
	
	public void update() {
		this.lastUpdateDate = new Date();
	}
	@SuppressWarnings("unchecked")
	public String toString() {
		JSONObject map=new JSONObject();
		map.put("hostname", this.hostName);
		map.put("port", this.port);
		map.put("location", this.location);
		map.put("status", this.isOnline());
		map.put("lastUpdateDate", this.getLastUpdateDate().getTime());
		return map.toJSONString();
	}	

	/**
	 * @return the isPredecessor
	 */
	public boolean isSelf() {
		return isSelf;
	}

	/**
	 * @param isPredecessor the isPredecessor to set
	 */
	public void setSelf(boolean isSelf) {
		this.isSelf = isSelf;
	}
}

