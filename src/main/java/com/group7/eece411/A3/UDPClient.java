package com.group7.eece411.A3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * @author Danny Chih Yang Hsieh
 *
 */
public class UDPClient {
	private InetAddress source;
	private int listenPort;
	private int timeout;
	private DatagramSocket socket;
	
	public UDPClient(int port)
			throws UnknownHostException {
		this();
		this.listenPort = port; 
	}
	
	public UDPClient() throws UnknownHostException {
		this.source = InetAddress.getByName(InetAddress.getLocalHost()
				.getHostAddress());
		this.setTimeout(100);
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
		}
	}

	public void responseCache(Packet receivePacket, Packet cachePacket) throws IOException {
		cachePacket.setDestinationIP(receivePacket.getDestinationIP());
		cachePacket.setDestinationPort(receivePacket.getDestinationPort());
		this.send(cachePacket);
		Datastore.getInstance().addLog("RESPOND Cache", cachePacket.toString());
	}
	public void responseTo(Packet cachePacket, Packet sendPacket) throws IOException {
		this.send(sendPacket);
		Datastore.getInstance().storeCache(cachePacket.getUIDString(), sendPacket);
		Datastore.getInstance().storeProcessCache(cachePacket.getUIDString(), false);		
		Datastore.getInstance().addLog("RESPOND", sendPacket.toString());
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
		DatagramPacket packet = new DatagramPacket(request, request.length,
				InetAddress.getByName(host), port);
		socket.send(packet);
	}
	
	public void send(String host, int port, byte[] data) throws IOException {
		if (host == null || data == null) { 
			throw new IllegalArgumentException();
		}
		this.createSocket();
		DatagramPacket packet = new DatagramPacket(data, data.length,
				InetAddress.getByName(host), port);
		socket.send(packet);
	}
	
	public Packet receive() throws IOException {
		byte buffer[] = new byte[16384];
		DatagramPacket packet = receive(buffer);
		return Protocol.receiveRequest(Arrays.copyOfRange(buffer, 0, packet.getLength()), packet.getAddress().getHostAddress(), packet.getPort());
	}
	
	public Packet receiveResponse() throws IOException {
		byte buffer[] = new byte[16384];
		DatagramPacket packet = receive(buffer);
		return Protocol.receiveResponse(Arrays.copyOfRange(buffer, 0, packet.getLength()), packet.getAddress().getHostAddress(), packet.getPort());
	}

	public DatagramPacket receive(byte[] buffer) throws IOException {
		this.createSocket();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.setSoTimeout(this.timeout);
		socket.receive(packet);
		return packet;
	}

}
