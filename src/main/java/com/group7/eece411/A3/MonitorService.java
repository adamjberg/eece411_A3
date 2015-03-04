package com.group7.eece411.A3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

/**
 * 
 *
 */
public class MonitorService extends Service
{	
	public static final String host = "127.0.0.1";//"54.68.197.12";
	public static final int port = 41170;
	private String loc;
	
	public MonitorService(int period) throws MalformedURLException, IOException {
		super(period, 41171);
		this.loc = request("http://ip-api.com/json/"+InetAddress.getLocalHost().getHostAddress(), "GET");
	}
	
    public void run()
    {	
		try {
			System.out.println(new String(getData()));
			client.send(host, port, getData());
		} catch (Exception e) {
			Datastore.getInstance().addException("EXCEPTION", e);
		}
    }
    
    public String request(String url, String method) throws MalformedURLException, IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod(method);
		if(connection.getResponseCode() == 200) {
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			return in.readLine();
		}
		return null;
	}
    
	public byte[] getData() throws IOException, ParseException {
    	String data = "{hostname:\""+SystemCmd.getHostName()+"\","
				+ "systemUptime:\""+SystemCmd.uptime()+"\","
						+ "spaceAvailable:\""+SystemCmd.getDiskAvailableSize()+"\","
								+ "averageLoads:\""+SystemCmd.getLoad()+"\","
										+ "serviceUptime:\""+ManagementFactory.getRuntimeMXBean().getUptime()+"\","
												+ "loc:\""+this.loc+"\","
														+ "logs:\""+Datastore.getInstance().getLogs()+"\"}"	;
    	
		return data.getBytes();
    }
}
