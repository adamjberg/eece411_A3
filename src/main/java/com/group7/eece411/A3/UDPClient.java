package com.group7.eece411.A3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.christianschenk.simplecache.SimpleCache;

/**
 * @author Danny Chih Yang Hsieh
 *
 */
public class UDPClient {
	private InetAddress source;
	private int listenPort;
	private int timeout;
	private DatagramSocket socket;
	private ConcurrentLinkedQueue<Packet> sendQueue;
	
	public UDPClient(int port)
			throws UnknownHostException {
		this();
		this.listenPort = port; 
		this.sendQueue = new ConcurrentLinkedQueue<Packet>();
	}
	
	public UDPClient() throws UnknownHostException {
		this.source = InetAddress.getByName(InetAddress.getLocalHost()
				.getHostAddress());
		this.setTimeout(100);
	}

	public void addPacketToSend(Packet p) {
		this.sendQueue.add(p);
	}
	
	public void setTimeout(int millseconds) {
		this.timeout = millseconds;
		if (millseconds < 0)
			this.timeout = 100;
	}

	public int getTimeout() {
		if (this.timeout <= 0) {
			this.timeout = 100;
		}
		return this.timeout;
	}

	public InetAddress getSource() {
		return this.source;
	}

	public void setListenPort(int port) {
		this.listenPort = port;
	}
	
	public void createSocket() throws SocketException {
		if (this.socket == null) {
			try {
				this.socket = new DatagramSocket(this.listenPort);
			} catch(SocketException se) {
				this.socket = new DatagramSocket();
			}
			this.socket.setReuseAddress(true);
			if(this.socket.getLocalPort() > 0) {
				this.listenPort = this.socket.getLocalPort();
			}
		}
	}

	public void closeSocket() {
		if (this.socket != null && !this.socket.isClosed()) {
			this.socket.close();
			this.socket = null;
		}
	}

	public void responseCache(Packet receivePacket, Packet cachePacket) throws IOException {
		responseCache(receivePacket.getDestinationIP(), receivePacket.getDestinationPort(), cachePacket);
	}
	
	public void responseCache(String ip, int port, Packet cachePacket) throws IOException {
		cachePacket.setDestinationIP(ip);
		cachePacket.setDestinationPort(port);
		this.addPacketToSend(cachePacket);
		//Datastore.getInstance().addLog("RESPOND Cache", cachePacket.toString());
	}
	
	public void responseTo(Packet sendPacket) throws IOException {
		this.send(sendPacket);
		Datastore.getInstance().storeCache(sendPacket.getUIDString(), sendPacket);
		//Datastore.getInstance().storeProcessCache(sendPacket.getUIDString(), false);		
		//Datastore.getInstance().addLog("RESPOND", sendPacket.toString());
	}
	
	public void sendQueue() throws IOException {
		while(!this.sendQueue.isEmpty()) {
			this.responseTo(this.sendQueue.poll());
		}
	}
	
	public void send(Packet p) throws IOException {
		if (p == null) {
			throw new IllegalArgumentException();
		}
		this.send(p.getDestinationIP(), p.getDestinationPort(), p);
	}
	
	public void send(String host, int port, Packet p) throws IOException {
		if (host == null || p == null) {
			throw new IllegalArgumentException();
		}
		this.createSocket();
		byte[] request = p.getBytes();
		NodeInfo n = Datastore.getInstance().getHost(host);
		InetAddress ip;
		if(n == null) {
			ip = InetAddress.getByName(host);
		} else {
			ip = n.getAddress();
		}
		DatagramPacket packet = new DatagramPacket(request, request.length,
				ip, port);
		socket.send(packet);
	}
	
	public void send(InetAddress ip, int port, byte[] data) throws IOException {
		if (ip == null || data == null) { 
			throw new IllegalArgumentException();
		}
		this.createSocket();
		DatagramPacket packet = new DatagramPacket(data, data.length,
				ip, port);
		socket.send(packet);
	}
	
	public Packet receive() throws IOException {
		byte buffer[] = new byte[16384];
		DatagramPacket packet = receive(buffer);
		Header header = new Header();
		Protocol.decodeUniqueId(Arrays.copyOfRange(buffer, 0, 16), header);
		Packet cachePacket = Datastore.getInstance().getCache(header.getUIDString());
		if(cachePacket == null) { 
			Datastore.getInstance().storeCache(header.getUIDString(), new Packet());
			//make sure we only process once
			
			return Protocol.receiveRequest(header, Arrays.copyOfRange(buffer, 16, packet.getLength()), packet.getAddress().getHostAddress(), packet.getPort());
		} else if(!cachePacket.isEmpty()) {
			this.responseCache(packet.getAddress().getHostAddress(), packet.getPort(), cachePacket);
		} 
		return null;
	}
	
	public Packet receiveResponse(String uid) throws IOException {
		byte buffer[] = new byte[16384];
		String uidString;
		DatagramPacket packet;
		do {
			packet = receive(buffer);
			uidString = StringUtils.byteArrayToHexString(Arrays.copyOfRange(buffer, 0, 4)) + ":" +
					StringUtils.byteArrayToHexString(Arrays.copyOfRange(buffer, 4, 6)) + ":" +
					StringUtils.byteArrayToHexString(Arrays.copyOfRange(buffer, 6, 8)) + ":" +
					StringUtils.byteArrayToHexString(Arrays.copyOfRange(buffer, 8, 16));
		} while(!uid.equals(uidString));
		return Protocol.receiveResponse(Arrays.copyOfRange(buffer, 0, packet.getLength()), packet.getAddress().getHostAddress(), packet.getPort());
	}

	public DatagramPacket receive(byte[] buffer) throws IOException {
		this.createSocket();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.setSoTimeout(this.timeout);
		socket.receive(packet);
		return packet;
	}

	public void forwardCopies(List<Packet> packets) throws IOException {
		for(Packet p : packets) {
			this.send(p);
		}
	}
}
