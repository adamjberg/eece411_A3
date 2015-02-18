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

	public void createSocket() throws SocketException {
		if (this.socket == null) {
			this.socket = new DatagramSocket(this.listenPort);
			this.socket.setReuseAddress(true);
		}
	}

	public void closeSocket() {
		if (this.socket != null && !this.socket.isClosed()) {
			this.socket.close();
		}
	}

	public void send(Packet p) throws IOException {
		if (p == null) {
			throw new IllegalArgumentException();
		}
		System.out.println("Sending to "+p.getSourceIp()+":"+p.getSourcePort());
		this.createSocket();
		byte[] request = p.getBytes();
		System.out.println("message going to be sent has size of "+request.length+" with response/command code : "+request[16]);
		DatagramPacket packet = new DatagramPacket(request, request.length,
				InetAddress.getByName(p.getSourceIp()), Integer.valueOf(p.getSourcePort()));
		socket.send(packet);
	}
	
	public Packet receive() throws IOException {
		this.createSocket();
		byte buffer[] = new byte[16384];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.setSoTimeout(this.timeout);
		socket.receive(packet);
		System.out.println("*************************************************");
		System.out.println("Receive packet length : "+packet.getLength());
		return Protocol.receiveRequest(Arrays.copyOfRange(buffer, 0, packet.getLength()));
	}

}
